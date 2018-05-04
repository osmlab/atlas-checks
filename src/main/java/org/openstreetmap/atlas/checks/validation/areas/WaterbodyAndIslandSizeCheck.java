package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Auto generated Check template
 *
 * @author danielbaah
 */
public class WaterbodyAndIslandSizeCheck extends BaseCheck
{

    private static final long serialVersionUID = 1L;
    private static String WATERBODY_MIN_INSTRUCTION = "Waterbody {0,number,#} has an area {1,number,#.##}m^2 which is too small.";
    private static String WATERBODY_MAX_INSTRUCTION = "Waterbody {0,number,#} has an area {1,number,#.##}km^2 which is too large.";
    private static String ISLAND_MIN_INSTRUCTION = "Island {0,number,#} has an area {1,number,#.##}m^2 which is too small.";
    private static String ISLAND_MAX_INSTRUCTION = "Island {0,number,#} has an area {1,number,#.##}km^2 which is too large.";
    private static String ISLET_MAX_INSTRUCTION = "Islet {0,number,#} has an area > 1km^2 and should likely be tagged with place=ISLAND.";

    private static final Predicate<AtlasObject> IS_ARCHIPELAGO = object -> Validators
            .isOfType(object, PlaceTag.class, PlaceTag.ARCHIPELAGO);
    private static final Predicate<AtlasObject> IS_ISLET = object -> Validators.isOfType(object,
            PlaceTag.class, PlaceTag.ISLET);
    private static final Predicate<AtlasObject> IS_ISLAND = object -> Validators.isOfType(object,
            PlaceTag.class, PlaceTag.ISLAND);

    // Multipolygon Relations and natural=water OR water=*
    private static final Predicate<AtlasObject> IS_MULTIPOLYGON_WATER_RELATION = object -> Validators
            .isOfType(object, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON)
            && (Validators.isOfType(object, NaturalTag.class, NaturalTag.WATER));

    private static final double WATERBODY_MIN_AREA_DEFAULT = 10.0;
    private static final double WATERBODY_MAX_AREA_DEFAULT = 337000.0;
    private static final double ISLAND_MIN_AREA_DEFAULT = 10.0;
    private static final double ISLAND_MAX_AREA_DEFAULT = 2170000.0;
    private static final double ISLET_MAX_AREA_DEFAULT = 1.0;
    private static final int THREE = 3;
    private static final int FOUR = 4;

    private final double waterbodyMinimumArea;
    private final double waterbodyMaximumArea;
    private final double islandMinimumArea;
    private final double islandMaximumArea;
    private final double isletMaximumArea;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            WATERBODY_MIN_INSTRUCTION, WATERBODY_MAX_INSTRUCTION, ISLAND_MIN_INSTRUCTION,
            ISLAND_MAX_INSTRUCTION, ISLET_MAX_INSTRUCTION);

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
                "area.waterbody.minimum.meters", WATERBODY_MIN_AREA_DEFAULT);
        this.waterbodyMaximumArea = (double) configurationValue(configuration,
                "area.waterbody.maximum.kilometers", WATERBODY_MAX_AREA_DEFAULT);
        this.islandMinimumArea = (double) configurationValue(configuration,
                "area.island.minimum.meters", ISLAND_MIN_AREA_DEFAULT);
        this.islandMaximumArea = (double) configurationValue(configuration,
                "area.island.maximum.kilometers", ISLAND_MAX_AREA_DEFAULT);
        this.isletMaximumArea = (double) configurationValue(configuration,
                "area.island.maximum.kilometers", ISLET_MAX_AREA_DEFAULT);

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
        // Object must be a MultiPolygon Relation with at least one member
        return (object instanceof Relation && IS_MULTIPOLYGON_WATER_RELATION.test(object)
                && !((Relation) object).members().isEmpty())
                // An Area with place=islet, place=island, place=archipelago, or natural=water tag
                || (object instanceof Area
                        && (TagPredicates.IS_WATER_BODY.test(object) || IS_ARCHIPELAGO.test(object)
                                || IS_ISLET.test(object) || IS_ISLAND.test(object)))
                        // And has yet to be examined
                        && !this.isFlagged(object.getOsmIdentifier());
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
            // passed in a Areas
            this.markAsFlagged(area.getOsmIdentifier());

            if (TagPredicates.IS_WATER_BODY.test(object))
            {
                if (surfaceAreaMeters < this.waterbodyMinimumArea)
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                            object.getOsmIdentifier(), surfaceAreaMeters)));
                }
                else if (surfaceAreaKilometers > this.waterbodyMaximumArea)
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1,
                            object.getOsmIdentifier(), surfaceAreaKilometers)));
                }
            }

            if (IS_ISLAND.test(object))
            {
                if (surfaceAreaMeters < this.islandMinimumArea)
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(2,
                            object.getOsmIdentifier(), surfaceAreaMeters)));
                }
                else if (surfaceAreaKilometers > this.islandMaximumArea)
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(THREE,
                            object.getOsmIdentifier(), surfaceAreaKilometers)));
                }
            }

            if (IS_ISLET.test(object))
            {
                if (surfaceAreaMeters > this.isletMaximumArea)
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(FOUR,
                            object.getOsmIdentifier())));
                }
            }
        }
        // There are two Relations to examine:
        // 1. Islands included as members of a MultiPolygon
        // 2. Water bodies included as members of a Multipolygon
        // Each Relation will have n numbers of instructions, dependent on the amount of members
        // that fail the size check
        else if (object instanceof Relation)
        {
            final Relation relation = (Relation) object;
            // Remove non-Area members && entities that have already been examined
            final Set<RelationMember> relationMembers = relation.members().stream()
                    .filter(member -> member.getEntity() instanceof Area
                            && this.isFlagged(member.getEntity().getOsmIdentifier()))
                    .collect(Collectors.toSet());

            if (!relationMembers.isEmpty())
            {
                // Get valid members that fail the area size check
                final Set<RelationMember> invalidPolygonMembers = this
                        .invalidPolygonMembers(relationMembers);

                if (!invalidPolygonMembers.isEmpty())
                {
                    // Get instructions for each member
                    final List<String> instructions = this
                            .getMemberInstructions(invalidPolygonMembers);

                    if (!invalidPolygonMembers.isEmpty())
                    {
                        // Create a single flag with each member entity and its given instruction
                        return Optional.of(instructions).map(instruction -> new CheckFlag(
                                this.getTaskIdentifier(relation), invalidPolygonMembers.stream()
                                        .map(RelationMember::getEntity).collect(Collectors.toSet()),
                                instruction));
                    }

                }
                else
                {
                    return Optional.empty();
                }
            }
            else
            {
                return Optional.empty();
            }

        }

        return Optional.empty();
    }

    /**
     * Returns a set of valid Multipolygon Relation inner and outer members that fail the area size
     * check
     *
     * @param relationPolygonMembers
     *            - relation members
     * @return - a Set of Relation Members
     */
    private Set<RelationMember> invalidPolygonMembers(
            final Set<RelationMember> relationPolygonMembers)
    {
        return relationPolygonMembers.stream().filter(member ->
        {
            // A MultiPolygon member must have an inter or outer role
            return (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER) || (member
                    .getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)
                    // Inner member cannot have natural=rock tag
                    && Validators.isOfType(member.getEntity(), NaturalTag.class, NaturalTag.ROCK)))
                    // Member must be larger or smaller than island/water body area values
                    && this.failsAreaTest(member);
        }).collect(Collectors.toSet());
    }

    /**
     * Returns a list on instruction for inner and outer MultiPolygons that fail the size check
     *
     * @param relationMembers
     *            - Members of a Relations
     * @return List of instructions
     */
    private List<String> getMemberInstructions(final Set<RelationMember> relationMembers)
    {

        final List<String> instructions = new ArrayList<>();

        relationMembers.forEach(member ->
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
                    instructions.add(this.getLocalizedInstruction(2, memberOsmId, areaInMeters));
                }
                else if (areaInKilometers > this.islandMaximumArea)
                {
                    instructions.add(
                            this.getLocalizedInstruction(THREE, memberOsmId, areaInKilometers));
                }
            }

            // Multipolygon water body Relations
            if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER))
            {

                if (areaInMeters < this.waterbodyMinimumArea)
                {
                    instructions.add(this.getLocalizedInstruction(0, memberOsmId, areaInMeters));
                }
                else if (areaInKilometers > this.waterbodyMaximumArea)
                {
                    instructions
                            .add(this.getLocalizedInstruction(1, memberOsmId, areaInKilometers));
                }
            }

        });

        return instructions;

    }

    /**
     * Returns true if Relation members fails min/max area test
     * 
     * @param member
     *            - relation member
     * @return true if member is too large or too small
     */
    private boolean failsAreaTest(RelationMember member)
    {
        final double areaInMeters = ((Area) member.getEntity()).asPolygon().surface()
                .asMeterSquared();
        final double areaInKilometers = ((Area) member.getEntity()).asPolygon().surface()
                .asKilometerSquared();

        return areaInMeters < this.islandMinimumArea || areaInMeters < this.waterbodyMinimumArea
                || areaInKilometers > this.islandMaximumArea
                || areaInKilometers > this.waterbodyMaximumArea;
    }
}
