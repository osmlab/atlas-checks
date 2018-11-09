package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.index.PackedSpatialIndex;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.geography.index.SpatialIndex;
import org.openstreetmap.atlas.tags.BuildingLevelsTag;
import org.openstreetmap.atlas.tags.BuildingMinLevelTag;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HeightTag;
import org.openstreetmap.atlas.tags.MinHeightTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import com.google.common.collect.Range;

/**
 * This flags buildings that are floating in 3D, thus casting a shadow on the base map when rendered
 * in 3D. Buildings are defined as Areas with a building or building:part tag or are part of a
 * building relation, or a relation of type multipolygon with a building tag.
 *
 * @author bbreithaupt
 */
public class ShadowDetectionCheck extends BaseCheck
{

    private static final long serialVersionUID = -6968080042879358551L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The building(s) and/or building part(s) float(s) above the ground. Please check the height/building:levels "
                    + "and min_height/building:min_level tags for all of the buildings parts.",
            "Relation {0,number,#} is floating.");

    // OSM standard level conversion factor
    private static final double LEVEL_TO_METERS_CONVERSION = 3.5;
    private static final String ZERO_STRING = "0";
    private static final RelationOrAreaToMultiPolygonConverter MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();

    private final Map<Atlas, SpatialIndex<Relation>> relationSpatialIndices = new HashMap<>();

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public ShadowDetectionCheck(final Configuration configuration)
    {
        super(configuration);
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
        return !this.isFlagged(object.getIdentifier())
                && (object instanceof Area
                        || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && this.hasMinKey(object)
                && (this.isBuildingOrPart(object) || this.isBuildingRelationMember(object));
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
        // Gather connected building parts and check for a connection to the ground
        final Set<AtlasObject> floatingParts = this.getFloatingParts(object);
        if (!floatingParts.isEmpty())
        {
            final CheckFlag flag;
            // If object is a relation, flatten it and add a relation instruction
            if (object instanceof Relation)
            {
                flag = this.createFlag(((Relation) object).flatten(),
                        this.getLocalizedInstruction(0));
                flag.addInstruction(this.getLocalizedInstruction(1, object.getOsmIdentifier()));
            }
            else
            {
                flag = this.createFlag(object, this.getLocalizedInstruction(0));
            }
            // Flag all the connected floating parts together
            for (final AtlasObject part : floatingParts)
            {
                this.markAsFlagged(part.getIdentifier());
                if (!part.equals(object))
                {
                    if (part instanceof Relation)
                    {
                        flag.addObjects(((Relation) part).flatten());
                        flag.addInstruction(
                                this.getLocalizedInstruction(1, part.getOsmIdentifier()));
                    }
                    else
                    {
                        flag.addObject(part);
                    }
                }
            }
            return Optional.of(flag);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Uses a BFS to gather all connected building parts. If a part is found that is on the ground,
     * an empty {@link Set} is returned because the parts are not floating.
     *
     * @param startingPart
     *            {@link AtlasObject} to start the walker from
     * @return a {@link Set} of {@link AtlasObject}s that are all floating
     */
    private Set<AtlasObject> getFloatingParts(final AtlasObject startingPart)
    {
        final Set<AtlasObject> connectedParts = new HashSet<>();
        final ArrayDeque<AtlasObject> toCheck = new ArrayDeque<>();
        connectedParts.add(startingPart);
        toCheck.add(startingPart);

        while (!toCheck.isEmpty())
        {
            final AtlasObject checking = toCheck.poll();
            // If a connection to the ground is found the parts are not floating
            if (!isOffGround(checking))
            {
                return new HashSet<>();
            }
            // Get parts connected in 3D
            final Set<AtlasObject> neighboringParts = new HashSet<>();
            final Rectangle checkingBounds = checking.bounds();
            // Get Areas
            neighboringParts
                    .addAll(Iterables.asSet(checking.getAtlas().areasIntersecting(checkingBounds,
                            area -> this.neighboringPart(area, checking, connectedParts))));
            // Get Relations
            if (!this.relationSpatialIndices.containsKey(checking.getAtlas()))
            {
                this.relationSpatialIndices.put(checking.getAtlas(),
                        this.buildRelationSpatialIndex(checking.getAtlas()));
            }
            neighboringParts.addAll(Iterables
                    .asSet(this.relationSpatialIndices.get(checking.getAtlas()).get(checkingBounds,
                            relation -> this.neighboringPart(relation, checking, connectedParts))));
            // Add the parts to the Set and Queue
            connectedParts.addAll(neighboringParts);
            toCheck.addAll(neighboringParts);
        }
        return connectedParts;
    }

    /**
     * Checks if two {@link AtlasObject}s are building parts and overlap each other.
     *
     * @param part
     *            a known building part to check against
     * @return true if {@code object} is a building part and overlaps {@code part}
     */
    private boolean neighboringPart(final AtlasObject object, final AtlasObject part,
            final Set<AtlasObject> checked)
    {
        try
        {
            // Get the polygons of the parts, either single or multi
            final GeometricSurface partPolygon = part instanceof Area ? ((Area) part).asPolygon()
                    : MULTI_POLYGON_CONVERTER.convert((Relation) part);
            final GeometricSurface objectPolygon = object instanceof Area
                    ? ((Area) object).asPolygon()
                    : MULTI_POLYGON_CONVERTER.convert((Relation) object);
            // Check if it is a building part, and overlaps.
            return !checked.contains(object) && !this.isFlagged(object.getIdentifier())
                    && (this.isBuildingOrPart(object) || this.isBuildingRelationMember(object))
                    && (partPolygon instanceof Polygon
                            ? objectPolygon.overlaps((Polygon) partPolygon)
                            : objectPolygon.overlaps((MultiPolygon) partPolygon))
                    && neighborsHeightContains(part, object);
        }
        // Ignore malformed MultiPolygons
        catch (final CoreException invalidMultiPolygon)
        {
            return false;
        }
    }

    /**
     * Given two {@link AtlasObject}s, checks that they have any intersecting or touching height
     * values. The range of height values for the {@link AtlasObject}s are calculated using height
     * and layer tags. A {@code min_height} or {@code building:min_layer} tag must exist for
     * {@code part}. All other tags will use defaults if not found.
     *
     * @param part
     *            {@link AtlasObject} being checked
     * @param neighbor
     *            {@link AtlasObject} being checked against
     * @return true if {@code part} intersects or touches {@code neighbor}, by default neighbor is
     *         flat on the ground.
     */
    private boolean neighborsHeightContains(final AtlasObject part, final AtlasObject neighbor)
    {
        final Map<String, String> neighborTags = neighbor.getOsmTags();
        final Map<String, String> partTags = part.getOsmTags();
        final double partMinHeight;
        final double partMaxHeight;
        double neighborMinHeight = 0;
        double neighborMaxHeight = 0;

        try
        {
            // Set partMinHeight
            partMinHeight = partTags.containsKey(MinHeightTag.KEY)
                    ? Double.parseDouble(partTags.get(MinHeightTag.KEY))
                    : Double.parseDouble(partTags.get(BuildingMinLevelTag.KEY))
                            * LEVEL_TO_METERS_CONVERSION;
            // Set partMaxHeight
            if (partTags.containsKey(HeightTag.KEY))
            {
                partMaxHeight = Double.parseDouble(partTags.get(HeightTag.KEY));
            }
            else if (partTags.containsKey(BuildingLevelsTag.KEY))
            {
                partMaxHeight = Double.parseDouble(partTags.get(BuildingLevelsTag.KEY))
                        * LEVEL_TO_METERS_CONVERSION;
            }
            else
            {
                partMaxHeight = partMinHeight;
            }
            // Set neighborMinHeight
            if (neighborTags.containsKey(MinHeightTag.KEY))
            {
                neighborMinHeight = Double.parseDouble(neighborTags.get(MinHeightTag.KEY));
            }
            else if (neighborTags.containsKey(BuildingMinLevelTag.KEY))
            {
                neighborMinHeight = Double.parseDouble(neighborTags.get(BuildingMinLevelTag.KEY))
                        * LEVEL_TO_METERS_CONVERSION;
            }
            // Set neighborMaxHeight
            if (neighborTags.containsKey(HeightTag.KEY))
            {
                neighborMaxHeight = Double.parseDouble(neighborTags.get(HeightTag.KEY));
            }
            else if (neighborTags.containsKey(BuildingLevelsTag.KEY))
            {
                neighborMaxHeight = Double.parseDouble(neighborTags.get(BuildingLevelsTag.KEY))
                        * LEVEL_TO_METERS_CONVERSION;
            }

            // Check the range of heights for overlap.
            try
            {
                return Range.closed(partMinHeight, partMaxHeight)
                        .isConnected(Range.closed(neighborMinHeight, neighborMaxHeight));
            }
            // Ignore buildings with a min value larger than its height
            catch (final IllegalArgumentException exc)
            {
                return false;
            }
        }
        // Ignore features with bad tags (like 2;10)
        catch (final NumberFormatException badTagValue)
        {
            return false;
        }
    }

    /**
     * Checks if an {@link AtlasObject} is a building or building:part that is valid for this check.
     *
     * @param object
     *            {@link AtlasObject} to check
     * @return true if {@code object} has a {@code building:part=yes} tag
     */
    private boolean isBuildingOrPart(final AtlasObject object)
    {
        return (BuildingTag.isBuilding(object)
                // Ignore roofs, as the are often used for items that have supports that are too
                // small to effectively map (such as a carport)
                && Validators.isNotOfType(object, BuildingTag.class, BuildingTag.ROOF))
                || Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO);
    }

    /**
     * Checks if an {@link AtlasObject} is a outline or part member of a building relation. This is
     * an equivalent tagging to building=* or building:part=yes.
     *
     * @param object
     *            {@link AtlasObject} to check
     * @return true if the object is part of any relation where it has role outline or part
     */
    private boolean isBuildingRelationMember(final AtlasObject object)
    {
        return object instanceof AtlasEntity && ((AtlasEntity) object).relations().stream()
                .anyMatch(relation -> Validators.isOfType(relation, RelationTypeTag.class,
                        RelationTypeTag.BUILDING)
                        && relation.members().stream()
                                .anyMatch(member -> member.getEntity().equals(object)
                                        && (member.getRole().equals("outline"))
                                        || member.getRole().equals("part")));
    }

    /**
     * Checks if an {@link AtlasObject} has a tag defining the minimum height of a building.
     *
     * @param object
     *            {@link AtlasObject} to check
     * @return true if {@code object} has a tag defining the minimum height of a building
     */
    private boolean hasMinKey(final AtlasObject object)
    {
        return Validators.hasValuesFor(object, BuildingMinLevelTag.class)
                || Validators.hasValuesFor(object, MinHeightTag.class);
    }

    /**
     * Checks if an {@link AtlasObject} has tags indicating it is off the ground.
     *
     * @param object
     *            {@link AtlasObject} to check
     * @return true if the area is off the ground
     */
    private boolean isOffGround(final AtlasObject object)
    {
        Double minHeight;
        Double minLevel;
        try
        {
            minHeight = Double
                    .parseDouble(object.getOsmTags().getOrDefault(MinHeightTag.KEY, ZERO_STRING));
            minLevel = Double.parseDouble(
                    object.getOsmTags().getOrDefault(BuildingMinLevelTag.KEY, ZERO_STRING));
        }
        // We want to flag if there is a bad value
        catch (final NumberFormatException badTagValue)
        {
            minHeight = 1.0;
            minLevel = 1.0;
        }
        return minHeight > 0 || minLevel > 0;
    }

    /**
     * Create a new spatial index that pre filters building relations. Pre-filtering drastically
     * decreases runtime by eliminating very large non-building relations. Copied from
     * {@link org.openstreetmap.atlas.geography.atlas.AbstractAtlas}.
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Relation> buildRelationSpatialIndex(final Atlas atlas)
    {
        final SpatialIndex<Relation> index = new PackedSpatialIndex<Relation, Long>(new RTree<>())
        {
            @Override
            protected Long compress(final Relation item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Relation item, final Rectangle bounds)
            {
                return item.intersects(bounds);
            }

            @Override
            protected Relation restore(final Long packed)
            {
                return atlas.relation(packed);
            }
        };
        atlas.relations(relation -> relation.isMultiPolygon() && BuildingTag.isBuilding(relation))
                .forEach(index::add);
        return index;
    }
}
