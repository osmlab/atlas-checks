package org.openstreetmap.atlas.checks.validation.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.MultiplePolyLineToPolygonsConverter;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check designed to scan through MultiPolygon relations and flag them for any and all reasons they
 * are invalid:
 * <ul>
 * <li>The multipolygon must be closed.</li>
 * <li>There must one or more outer members</li>
 * <li>Each member must have a role</li>
 * <li>There should be more than one member</li>
 * </ul>
 *
 * @author jklamer
 * @author sid
 */
public class InvalidMultiPolygonRelationCheck extends BaseCheck<Long>
{

    public static final int CLOSED_LOOP_INSTRUCTION_FORMAT_INDEX;
    public static final int INVALID_OSM_TYPE_INSTRUCTION_FORMAT_INDEX;
    public static final int INVALID_ROLE_INSTRUCTION_FORMAT_INDEX;
    public static final int MISSING_OUTER_INSTRUCTION_FORMAT_INDEX;
    public static final int SINGLE_MEMBER_RELATION_INSTRUCTION_FORMAT_INDEX;
    private static final String CLOSED_LOOP_INSTRUCTION_FORMAT = "The Multipolygon relation {0,number,#} with members : {1} is not closed at some locations : {2}";
    private static final String INVALID_OSM_TYPE_INSTRUCTION_FORMAT = "{0} relation member(s) are an invalid type in relation {1,number,#}. Multipolygon relations can only have ways as members. The first {2} object(s) are {3}";
    private static final String INVALID_ROLE_INSTRUCTION_FORMAT = "{0} ways have an invalid or missing role in multipolygon relation {1,number,#}. The role must be either outer or inner. The first {2} id(s) are {3}";
    // taken from the maximum recommended for relations based on OSM WIKI
    // https://wiki.openstreetmap.org/wiki/Relation:multipolygon
    private static final int MAX_FLAGGED_ENTITIES = 300;
    private static final int MAX_NUMBER_ROLE_INSTRUCTION_IDS = 10;
    private static final String MISSING_OUTER_INSTRUCTION_FORMAT = "Multipolygon relation {0,number,#} has no outer member(s). Must have 1 or more.";
    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final String SINGLE_MEMBER_RELATION_INSTRUCTION_FORMAT = "Multipolygon relation {0,number,#} has only one member.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            CLOSED_LOOP_INSTRUCTION_FORMAT, SINGLE_MEMBER_RELATION_INSTRUCTION_FORMAT,
            MISSING_OUTER_INSTRUCTION_FORMAT, INVALID_ROLE_INSTRUCTION_FORMAT,
            INVALID_OSM_TYPE_INSTRUCTION_FORMAT);
    private static final EnumMap<ItemType, String> atlasToOsmType = new EnumMap<>(ItemType.class);
    private static final Logger logger = LoggerFactory
            .getLogger(InvalidMultiPolygonRelationCheck.class);

    static
    {
        INVALID_ROLE_INSTRUCTION_FORMAT_INDEX = FALLBACK_INSTRUCTIONS
                .indexOf(INVALID_ROLE_INSTRUCTION_FORMAT);
        MISSING_OUTER_INSTRUCTION_FORMAT_INDEX = FALLBACK_INSTRUCTIONS
                .indexOf(MISSING_OUTER_INSTRUCTION_FORMAT);
        SINGLE_MEMBER_RELATION_INSTRUCTION_FORMAT_INDEX = FALLBACK_INSTRUCTIONS
                .indexOf(SINGLE_MEMBER_RELATION_INSTRUCTION_FORMAT);
        CLOSED_LOOP_INSTRUCTION_FORMAT_INDEX = FALLBACK_INSTRUCTIONS
                .indexOf(CLOSED_LOOP_INSTRUCTION_FORMAT);
        INVALID_OSM_TYPE_INSTRUCTION_FORMAT_INDEX = FALLBACK_INSTRUCTIONS
                .indexOf(INVALID_OSM_TYPE_INSTRUCTION_FORMAT);
        atlasToOsmType.put(ItemType.EDGE, "way");
        atlasToOsmType.put(ItemType.AREA, "way");
        atlasToOsmType.put(ItemType.LINE, "way");
        atlasToOsmType.put(ItemType.NODE, "node");
        atlasToOsmType.put(ItemType.POINT, "node");
        atlasToOsmType.put(ItemType.RELATION, "relation");
    }

    public InvalidMultiPolygonRelationCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation
                && Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation multipolygonRelation = (Relation) object;
        final List<String> instructions = new ArrayList<>();

        this.checkClosedLoop(multipolygonRelation).ifPresent(instructions::add);
        if (multipolygonRelation.members().size() <= 1)
        {
            instructions.add(
                    this.getLocalizedInstruction(SINGLE_MEMBER_RELATION_INSTRUCTION_FORMAT_INDEX,
                            multipolygonRelation.getOsmIdentifier()));
        }
        boolean atLeastOneOuter = false;
        int numberInvalidTypes = 0;
        int numberInvalidRoles = 0;
        final LinkedHashSet<Long> invalidRoleIDs = new LinkedHashSet<>(
                MAX_NUMBER_ROLE_INSTRUCTION_IDS);
        final LinkedHashSet<Tuple<String, Long>> invalidTypeIDs = new LinkedHashSet<>(
                MAX_NUMBER_ROLE_INSTRUCTION_IDS);
        for (final RelationMember relationMember : multipolygonRelation.members())
        {
            if (!atlasToOsmType.get(relationMember.getEntity().getType()).equals("way"))
            {
                if (!invalidTypeIDs.contains(
                        Tuple.createTuple(atlasToOsmType.get(relationMember.getEntity().getType()),
                                relationMember.getEntity().getOsmIdentifier())))
                {
                    numberInvalidTypes += 1;
                }
                if (numberInvalidTypes <= MAX_NUMBER_ROLE_INSTRUCTION_IDS)
                {
                    invalidTypeIDs.add(Tuple.createTuple(
                            atlasToOsmType.get(relationMember.getEntity().getType()),
                            relationMember.getEntity().getOsmIdentifier()));
                }
            }
            else if (relationMember.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER))
            {
                atLeastOneOuter = true;
            }
            else if (!relationMember.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER))
            {
                if (!invalidRoleIDs.contains(relationMember.getEntity().getOsmIdentifier()))
                {
                    numberInvalidRoles += 1;
                }
                if (numberInvalidRoles <= MAX_NUMBER_ROLE_INSTRUCTION_IDS)
                {
                    invalidRoleIDs.add(relationMember.getEntity().getOsmIdentifier());
                }
            }
        }

        if (numberInvalidRoles > 0)
        {
            instructions.add(this.getLocalizedInstruction(INVALID_ROLE_INSTRUCTION_FORMAT_INDEX,
                    numberInvalidRoles, multipolygonRelation.getOsmIdentifier(),
                    Integer.min(numberInvalidRoles, MAX_NUMBER_ROLE_INSTRUCTION_IDS),
                    invalidRoleIDs));
        }
        if (numberInvalidTypes > 0)
        {
            instructions.add(this.getLocalizedInstruction(INVALID_OSM_TYPE_INSTRUCTION_FORMAT_INDEX,
                    numberInvalidTypes, multipolygonRelation.getOsmIdentifier(),
                    Integer.min(numberInvalidTypes, MAX_NUMBER_ROLE_INSTRUCTION_IDS),
                    invalidTypeIDs));
        }
        if (!atLeastOneOuter)
        {
            instructions.add(this.getLocalizedInstruction(MISSING_OUTER_INSTRUCTION_FORMAT_INDEX,
                    multipolygonRelation.getOsmIdentifier()));
        }

        return Optional.of(instructions).filter(instructionList -> !instructionList.isEmpty())
                .map(instructionList -> new CheckFlag(this.getTaskIdentifier(object),
                        multipolygonRelation.members().stream().map(RelationMember::getEntity)
                                .limit(MAX_FLAGGED_ENTITIES).collect(Collectors.toSet()),
                        instructionList));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Optional<String> checkClosedLoop(final Relation relation)
    {
        try
        {
            RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(relation);
        }
        catch (final MultiplePolyLineToPolygonsConverter.OpenPolygonException exception)
        {
            final List<Location> openLocations = exception.getOpenLocations();
            final Set<AtlasObject> objects = openLocations.stream()
                    .flatMap(location -> this.filterMembers(relation, location))
                    .collect(Collectors.toSet());
            final Set<Long> memberIds = relation.members().stream()
                    .map(member -> member.getEntity().getOsmIdentifier())
                    .collect(Collectors.toSet());

            if (!objects.isEmpty() && !memberIds.isEmpty())
            {
                return Optional
                        .of(this.getLocalizedInstruction(CLOSED_LOOP_INSTRUCTION_FORMAT_INDEX,
                                relation.getOsmIdentifier(), memberIds, openLocations));
            }
            else
            {
                logger.warn("Unable to find members in relation {} containing the locations : {}",
                        relation, openLocations);
            }
        }
        catch (final Exception exception)
        {
            logger.warn("Unable to convert relation {}. {}", relation.getOsmIdentifier(),
                    exception.getMessage());
        }

        return Optional.empty();
    }

    private Stream<Line> filterMembers(final Relation relation, final Location location)
    {
        return relation.members().stream().map(RelationMember::getEntity)
                .filter(entity -> entity instanceof Line).map(entity -> (Line) entity)
                .filter(line -> line.asPolyLine().contains(location));
    }
}
