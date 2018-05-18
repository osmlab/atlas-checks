package org.openstreetmap.atlas.checks.validation.areas;

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
import org.openstreetmap.atlas.utilities.scalars.Surface;

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
    private static String WATERBODY_INSTRUCTION = "Waterbody with OSM ID {0,number,#} has a surface"
            + " area of {1,number,#.##} meters squared, which is outside of the expected surface area"
            + " range of {2} square meters to {3} square kilometers.";
    private static String ISLAND_INSTRUCTION = "Island with OSM ID {0,number,#} has a surface area"
            + " of {1,number,#.##} meters squared, which is outside of the expected surface area "
            + "range of {2} square meters to {3} square kilometers.";
    private static String ISLET_INSTRUCTION = "Islet with OSM ID {0,number,#} has an surface area "
            + "of {1,number,#.##} meters squared, which is outside of the expected surface area range"
            + " of {2} square meters to {3} square kilometers. Islets greater than {3} square "
            + "kilometers should likely be tagged as place=ISLAND.";
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
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(WATERBODY_INSTRUCTION,
            ISLAND_INSTRUCTION, ISLET_INSTRUCTION);

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
            final Surface surfaceArea = area.asPolygon().surface();
            final double surfaceAreaMeters = surfaceArea.asMeterSquared();
            final double surfaceAreaKilometers = surfaceArea.asKilometerSquared();
            // Mark each object because we could potentially run into Multipolygon entities that get
            // passed in as Areas
            this.markAsFlagged(area.getIdentifier());

            // natural=water tag
            if (TagPredicates.IS_WATER_BODY.test(object))
            {
                if (surfaceAreaMeters < this.waterbodyMinimumArea
                        || surfaceAreaKilometers > this.waterbodyMaximumArea)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                                    surfaceAreaMeters, this.waterbodyMinimumArea,
                                    this.waterbodyMaximumArea)));
                }
            }
            // place=island or place=archipelago tags
            if (IS_ISLAND.test(object))
            {
                if (surfaceAreaMeters < this.islandMinimumArea
                        || surfaceAreaKilometers > this.islandMaximumArea)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(1, object.getOsmIdentifier(),
                                    surfaceAreaMeters, this.islandMinimumArea,
                                    this.islandMaximumArea)));
                }
            }
            // place=islet tag
            if (IS_ISLET.test(object))
            {
                if (surfaceAreaMeters < this.isletMinimumArea
                        || surfaceAreaKilometers > this.isletMaximumArea)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(2, object.getOsmIdentifier(),
                                    surfaceAreaMeters, this.isletMinimumArea,
                                    this.isletMaximumArea)));
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
                    .filter(this::isValidMultiPolygonRelationMember).collect(Collectors.toSet());

            if (!relationMembers.isEmpty())
            {
                return this.getMultiPolygonRelationFlags(relation, relationMembers);
            }
        }

        return Optional.empty();
    }

    /**
     * Helper function for filtering invalid Relation members. Each Relation member must be an Area,
     * have either an inner or outer role, and the inner role must not contain the natural=rock tag.
     *
     * @param member
     *            - RelationMember
     * @return true if member is valid
     */
    private boolean isValidMultiPolygonRelationMember(final RelationMember member)
    {
        return member.getEntity() instanceof Area
                && !this.isFlagged(member.getEntity().getIdentifier())
                && (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER)
                        || (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)
                                && !Validators.isOfType(member.getEntity(), NaturalTag.class,
                                        NaturalTag.ROCK)));
    }

    /**
     * This function calculates the surface area of an individual Relation Member & creates an
     * CheckFlag Optional with instruction and member entities it fails the surface area test.
     *
     * @param relation
     *            - Relation
     * @param relationMembers
     *            - Valid Relation members
     * @return CheckFlag Optional
     */
    private Optional<CheckFlag> getMultiPolygonRelationFlags(final Relation relation,
            final Set<RelationMember> relationMembers)
    {
        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(relation));

        for (final RelationMember member : relationMembers)
        {
            final Surface surfaceArea = ((Area) member.getEntity()).asPolygon().surface();
            final double surfaceAreaMeters = surfaceArea.asMeterSquared();
            final double surfaceAreaKilometers = surfaceArea.asKilometerSquared();
            final long memberOsmId = member.getEntity().getOsmIdentifier();

            // Multipolygon Island Relations
            if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER))
            {
                if (surfaceAreaMeters < this.islandMinimumArea
                        || surfaceAreaKilometers > this.islandMaximumArea)
                {
                    flag.addInstruction(this.getLocalizedInstruction(1, memberOsmId,
                            surfaceAreaMeters, this.islandMinimumArea, this.islandMaximumArea));
                    flag.addObject(member.getEntity());
                }
            }
            // Multipolygon water body Relations
            if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER))
            {
                if (surfaceAreaMeters < this.waterbodyMinimumArea
                        || surfaceAreaKilometers > this.waterbodyMaximumArea)
                {
                    flag.addInstruction(
                            this.getLocalizedInstruction(0, memberOsmId, surfaceAreaMeters,
                                    this.waterbodyMinimumArea, this.waterbodyMaximumArea));
                    flag.addObject(member.getEntity());
                }
            }
        }

        return !flag.getFlaggedObjects().isEmpty() ? Optional.of(flag) : Optional.empty();
    }
}
