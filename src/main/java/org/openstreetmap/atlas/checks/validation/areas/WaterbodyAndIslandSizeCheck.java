package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.PlaceTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check identifies waterbodies and islands that are either too small, or too large in size.
 * Minimum surface area values are measured in meters squared; while maximum values are measured in
 * kilometers squared. We use two units of conversion to avoid very small/large values. For example,
 * 10 meters squared is .00001 kilometers, and 2,000,000 kilometers squared is 2,000,000,000 meters
 * squared. An island can either be tagged as place=island, place=islet, place=archipelago, or a
 * MultiPolygon Relation with the type=multipolygon and natural=water tags.
 *
 * @author danielbaah
 */
public class WaterbodyAndIslandSizeCheck extends BaseCheck
{

    private static final long serialVersionUID = 4105386144665109331L;
    private static String WATERBODY_MIN_INSTRUCTION = "Waterbody with OSM ID {0,number,#}, has an surface area of {1,number,#.##} meters squared which is smaller than the expected minimum of {2} meters squared.";
    private static String WATERBODY_MAX_INSTRUCTION = "Waterbody with OSM ID {0,number,#}, has an surface area of {1,number,#.##} kilometers squared which is larger than the expected maximum of {2} kilometers squared.";
    private static String ISLAND_MIN_INSTRUCTION = "Island with OSM ID {0,number,#}, has an surface area of {1,number,#.##} meters squared which is smaller than the expected minimum of {2} meters squared.";
    private static String ISLAND_MAX_INSTRUCTION = "Island with OSM ID {0,number,#}, has an surface area of {1,number,#.##} kilometers squared which is larger than the expected maximum of {2} kilometers squared.";
    private static String ISLET_MIN_INSTRUCTION = "Islet with OSM ID {0,number,#}, has an surface area of {1,number,#.##} meters squared which is smaller than the expected minimum of {2} meters squared.";
    private static String ISLET_MAX_INSTRUCTION = "Islet with OSM ID {0,number,#}, has an surface area of {1,number,#.##} kilometers squared which is greater than the expected maximum of {2} kilometers squared. This islet should likely be tagged as place=ISLAND.";
    private static final double WATERBODY_MIN_AREA_DEFAULT = 10;
    // Waterbody maximum is based on the Caspian Sea surface area, the largest island waterbody in
    // OSM that is tagged with natural=water.
    private static final double WATERBODY_MAX_AREA_DEFAULT = 337000;
    private static final double ISLAND_MIN_AREA_DEFAULT = 10;
    // Island maximum is based on Greenland surface area, the largest island in OSM tagged as
    // place=island. The next largest landmass is Australia which is a continent and not an island.
    private static final double ISLAND_MAX_AREA_DEFAULT = 2170000;
    private static final double ISLET_MIN_AREA_DEFAULT = 10;
    // Islet maximum is specified by OSM Wiki (https://wiki.openstreetmap.org/wiki/Tag:place=islet)
    private static final double ISLET_MAX_AREA_DEFAULT = 1;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final Predicate<AtlasObject> IS_ISLET = object -> Validators.isOfType(object,
            PlaceTag.class, PlaceTag.ISLET);
    private static final Predicate<AtlasObject> IS_ISLAND = object -> Validators.isOfType(object,
            PlaceTag.class, PlaceTag.ISLAND, PlaceTag.ARCHIPELAGO);
    // Multipolygon Relation must have type=multipolygon and natural=water tags
    private static final Predicate<AtlasObject> IS_MULTIPOLYGON_WATER_RELATION = object -> Validators
            .isOfType(object, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON)
            && (Validators.isOfType(object, NaturalTag.class, NaturalTag.WATER));
    private final double waterbodyMinimumArea;
    private final double waterbodyMaximumArea;
    private final double islandMinimumArea;
    private final double islandMaximumArea;
    private final double isletMinimumArea;
    private final double isletMaximumArea;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            WATERBODY_MIN_INSTRUCTION, WATERBODY_MAX_INSTRUCTION, ISLAND_MIN_INSTRUCTION,
            ISLAND_MAX_INSTRUCTION, ISLET_MIN_INSTRUCTION, ISLET_MAX_INSTRUCTION);

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public WaterbodyAndIslandSizeCheck(final Configuration configuration)
    {
        super(configuration);
        this.waterbodyMinimumArea = (double) configurationValue(configuration,
                "surface.waterbody.minimum.meters", WATERBODY_MIN_AREA_DEFAULT);
        this.waterbodyMaximumArea = (double) configurationValue(configuration,
                "surface.waterbody.maximum.kilometers", WATERBODY_MAX_AREA_DEFAULT);
        this.islandMinimumArea = (double) configurationValue(configuration,
                "surface.island.minimum.meters", ISLAND_MIN_AREA_DEFAULT);
        this.islandMaximumArea = (double) configurationValue(configuration,
                "surface.island.maximum.kilometers", ISLAND_MAX_AREA_DEFAULT);
        this.isletMinimumArea = (double) configurationValue(configuration,
                "surface.islet.minimum.meters", ISLET_MIN_AREA_DEFAULT);
        this.isletMaximumArea = (double) configurationValue(configuration,
                "surface.islet.maximum.kilometers", ISLET_MAX_AREA_DEFAULT);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Object must be either a MultiPolygon Relation with at least one member, or an Area with
        // a place=islet, place=island, place=archipelago, or natural=water tag
        return (object instanceof Relation && IS_MULTIPOLYGON_WATER_RELATION.test(object)
                && ((Relation) object).members().size() >= 2)
                || (object instanceof Area
                        && (TagPredicates.IS_WATER_BODY.test(object) || IS_ISLET.test(object)
                                || IS_ISLAND.test(object))
                        && !this.isFlagged(object.getIdentifier()));
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (object instanceof Area)
        {
            final Area area = (Area) object;
            final double surfaceAreaMeters = area.asPolygon().surface().asMeterSquared();
            final double surfaceAreaKilometers = area.asPolygon().surface().asKilometerSquared();
            // Mark each object because we could potentially run into Multipolygon entities that get
            // passed in as Areas
            this.markAsFlagged(area.getIdentifier());

            // natural=water tag
            if (TagPredicates.IS_WATER_BODY.test(object))
            {
                if (surfaceAreaMeters < this.waterbodyMinimumArea)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                                    surfaceAreaMeters, this.waterbodyMinimumArea)));
                }
                else if (surfaceAreaKilometers > this.waterbodyMaximumArea)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(1, object.getOsmIdentifier(),
                                    surfaceAreaKilometers, this.waterbodyMaximumArea)));
                }
            }
            // place=island or place=archipelago tags
            if (IS_ISLAND.test(object))
            {
                if (surfaceAreaMeters < this.islandMinimumArea)
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(2,
                            object.getOsmIdentifier(), surfaceAreaMeters, this.islandMinimumArea)));
                }
                else if (surfaceAreaKilometers > this.islandMaximumArea)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(THREE, object.getOsmIdentifier(),
                                    surfaceAreaKilometers, this.islandMaximumArea)));
                }
            }
            // place=islet tag
            if (IS_ISLET.test(object))
            {
                if (surfaceAreaMeters < this.isletMinimumArea)
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(FOUR,
                            object.getOsmIdentifier(), surfaceAreaMeters, this.isletMinimumArea)));
                }
                else if (surfaceAreaKilometers > this.isletMaximumArea)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(FIVE, object.getOsmIdentifier(),
                                    surfaceAreaKilometers, this.isletMaximumArea)));
                }
            }
        }
        // There are two Relations to examine:
        // 1. Islands included as members of a MultiPolygon (not otherwise tagged as place=island or
        // place=islet)
        // 2. Water bodies included as members of a Multipolygon (not otherwise tagged as
        // natural=water)
        // Each Relation will have n numbers of instructions, dependent on the amount of members
        // that fail the size check
        else if (object instanceof Relation)
        {
            final Relation relation = (Relation) object;
            // Get Relation members that are an Area, have either an inner or outer member,
            // and have yet to be examined
            final Set<RelationMember> relationMembers = relation.members().stream()
                    .filter(member -> member.getEntity() instanceof Area)
                    .filter(member -> !this.isFlagged(member.getEntity().getIdentifier()))
                    .filter(this::isInnerOrOuterMember).collect(Collectors.toSet());

            if (!relationMembers.isEmpty())
            {
                final List<String> flagInstructions = new ArrayList<>();
                final Set<RelationMember> invalidRelationMembers = new HashSet<>();

                // Append member and given instruction for Relation members that fail the surface
                // area size test.
                this.getMemberInstructions(relationMembers, invalidRelationMembers,
                        flagInstructions);

                if (!flagInstructions.isEmpty())
                {
                    // Create a single flag with each member entity and its given instruction
                    return Optional.of(flagInstructions).map(instruction -> new CheckFlag(
                            this.getTaskIdentifier(relation), invalidRelationMembers.stream()
                                    .map(RelationMember::getEntity).collect(Collectors.toSet()),
                            instruction));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Helper function for filtering invalid Relation members. Each Relation member have either an
     * inner or outer role, and the inner role must not contain the natural=rock tag.
     *
     * @param member
     *            - RelationMember
     * @return true if member is valid
     */
    private boolean isInnerOrOuterMember(final RelationMember member)
    {
        return member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER) || (member.getRole()
                .equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)
                && !Validators.isOfType(member.getEntity(), NaturalTag.class, NaturalTag.ROCK));
    }

    /**
     * This function calculates the surface area of an individual Relation Member & generates an
     * instruction of it fails the surface area test.
     *
     * @param relationMembers
     *            - Set of valid RelationMembers
     * @param invalidRelationMembers
     *            - New Set of RelationMembers that fail surface area test
     * @param instructions
     *            - Flag Instructions for RelationMembers that fail surface area test
     */
    private void getMemberInstructions(final Set<RelationMember> relationMembers,
            final Set<RelationMember> invalidRelationMembers, final List<String> instructions)
    {

        for (final RelationMember member : relationMembers)
        {
            final double areaInMeters = ((Area) member.getEntity()).asPolygon().surface()
                    .asMeterSquared();
            final double areaInKilometers = ((Area) member.getEntity()).asPolygon().surface()
                    .asKilometerSquared();
            final long memberOsmId = member.getEntity().getOsmIdentifier();

            // Multipolygon Island Relations
            if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER))
            {
                if (areaInMeters < this.islandMinimumArea)
                {
                    instructions.add(this.getLocalizedInstruction(2, memberOsmId, areaInMeters,
                            this.islandMinimumArea));
                    invalidRelationMembers.add(member);
                }
                else if (areaInKilometers > this.islandMaximumArea)
                {
                    instructions.add(this.getLocalizedInstruction(THREE, memberOsmId,
                            areaInKilometers, this.islandMaximumArea));
                    invalidRelationMembers.add(member);
                }
            }

            // Multipolygon water body Relations
            if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER))
            {
                if (areaInMeters < this.waterbodyMinimumArea)
                {
                    instructions.add(this.getLocalizedInstruction(0, memberOsmId, areaInMeters,
                            this.waterbodyMinimumArea));
                    invalidRelationMembers.add(member);
                }
                else if (areaInKilometers > this.waterbodyMaximumArea)
                {
                    instructions.add(this.getLocalizedInstruction(1, memberOsmId, areaInKilometers,
                            this.waterbodyMaximumArea));
                    invalidRelationMembers.add(member);
                }
            }
        }

    }
}
