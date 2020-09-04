package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.FootTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.ServiceTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check flags pedestrian areas that are not properly snapped to its valid
 * intersecting/overlapping edges. A pedestrian area is an {@link Area} with {@link HighwayTag} =
 * "pedestrian" tag. Valid intersecting edges are edges with the same elevation(same
 * {@link LayerTag}, {@link LevelTag}) as the area, and highway tag value not equal to foot way,
 * pedestrian, steps and path. The pedestrian area and any valid intersecting/ overlapping edge that
 * is not snapped to the area are flagged along with its connected edges that are within the
 * pedestrian area.
 *
 * @author sayas01
 */
public class PedestrianAreaOverlappingEdgeCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 1861527706740836635L;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "Pedestrian area {0,number,#} is overlapping way id(s) {1} and is not snapped at all points of intersections.");
    private static final String ZERO = "0";
    private static final Long ZERO_LONG = 0L;
    // Default minimum distance between two points to be considered not overlapping.
    private static final double DISTANCE_MINIMUM_METERS_DEFAULT = 1.0;
    private final Distance minimumDistance;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public PedestrianAreaOverlappingEdgeCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumDistance = this.configurationValue(configuration, "distance.minimum.meters",
                DISTANCE_MINIMUM_METERS_DEFAULT, Distance::meters);
    }

    /**
     * Checks to see whether the supplied object class type is valid for this particular check
     *
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return true if it is
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Valid object for the check is a pedestrian area that has not been flagged
        return object instanceof Area && this.isPedestrianArea(object)
                && !isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area area = (Area) object;
        final Polygon areaPolygon = area.asPolygon();
        // All segments of the area
        final List<Segment> segments = areaPolygon.segments();
        // Get all start and end locations of all segments in the area
        final List<Location> locationsInSegments = Segment.asList(segments);
        // Filter and collect all valid intersecting edges
        final Set<Edge> filteredIntersectingEdges = Iterables
                .stream(area.getAtlas().edgesIntersecting(areaPolygon))
                .filter(edge -> this.isValidIntersectingEdge(edge, area)).collectToSet();
        final Set<AtlasObject> overlappingEdges = new HashSet<>();
        for (final Edge edge : filteredIntersectingEdges)
        {
            edge.asPolyLine().segments().forEach(segment ->
            {
                final Location edgeSegmentStartLocation = segment.start();
                final Location edgeSegmentEndLocation = segment.end();
                // Snapped start location
                final SnappedLocation snappedStartLoc = edgeSegmentStartLocation
                        .snapTo(areaPolygon);
                // Snapped end location
                final SnappedLocation snappedEndLoc = edgeSegmentEndLocation.snapTo(areaPolygon);
                // Distance of original start location to the snapped location
                final Distance startLocDistance = snappedStartLoc.getDistance();
                // Distance of original end location to the snapped location
                final Distance endLocDistance = snappedEndLoc.getDistance();
                // Checks if intersection is at start of the edge
                final boolean intersectsAtStart = locationsInSegments
                        .contains(edgeSegmentStartLocation);
                // Checks if intersection is at end of the edge
                final boolean intersectsAtEnd = locationsInSegments
                        .contains(edgeSegmentEndLocation);
                // If both ends of the edge intersects the area, check if it is properly snapped by
                // checking if both the intersecting points are on the same segment.
                // If none of the filtered segments have both the start and end nodes, then add the
                // edge and all its connected edges that are within the area to flag them.
                if ((intersectsAtStart && intersectsAtEnd
                        && this.isNotSnappedToSegments(segments, segment))
                        // If any one of the end of the connected edge is fully enclosed within the
                        // area, flag the edge and all its connected edges that are within the area.
                        || (intersectsAtStart && !intersectsAtEnd
                                && endLocDistance.isGreaterThanOrEqualTo(this.minimumDistance)
                                && areaPolygon.fullyGeometricallyEncloses(edgeSegmentEndLocation))
                        || (intersectsAtEnd && !intersectsAtStart
                                && startLocDistance.isGreaterThanOrEqualTo(this.minimumDistance)
                                && areaPolygon
                                        .fullyGeometricallyEncloses(edgeSegmentStartLocation)))
                {
                    // Collect all connected edges that are within the pedestrian area
                    edge.connectedEdges().stream().filter(connectedEdge -> this
                            .isValidIntersectingEdge(connectedEdge, area)
                            && areaPolygon.fullyGeometricallyEncloses(connectedEdge.asPolyLine()))
                            .forEach(overlappingEdges::add);
                    // Add the intersecting edge as well to the set
                    overlappingEdges.add(edge);
                }
            });
        }
        if (!overlappingEdges.isEmpty())
        {
            this.markAsFlagged(object.getOsmIdentifier());
            final CheckFlag flag = this.createFlag(overlappingEdges,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                            new StringList(getIdentifiers(overlappingEdges)).join(", ")));
            flag.addObject(object);
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
     * Collects all atlas identifiers of given set of {@link AtlasObject}s
     *
     * @param objects
     *            set of {@link AtlasObject}s
     * @return {@link Iterable<String>} containing the atlas identifiers of input objects
     */
    private Iterable<String> getIdentifiers(final Set<AtlasObject> objects)
    {
        return Iterables.stream(objects).map(AtlasObject::getIdentifier).map(String::valueOf)
                .collectToList();
    }

    /**
     * Checks if the given segment overlaps any of the segments in the given set
     *
     * @param segments
     *            set of segments
     * @param segment
     *            any given segment
     * @return true if the given segment overlaps any of the segments in the set
     */
    private boolean isNotSnappedToSegments(final List<Segment> segments, final Segment segment)
    {
        return segments.stream().noneMatch(areaSegment -> areaSegment.overlapsShapeOf(segment));
    }

    /**
     * Checks if the given edge and area have the same elevation or not.
     *
     * @param edge
     *            any given edge
     * @param area
     *            any given area
     * @return true if the edge and area have the same Layer and Level tag values
     */
    private boolean isOfSameElevation(final Edge edge, final Area area)
    {
        return LevelTag.getTaggedOrImpliedValue(area, ZERO)
                .equals(LevelTag.getTaggedOrImpliedValue(edge, ZERO))
                && LayerTag.getTaggedOrImpliedValue(area, ZERO_LONG)
                        .equals(LayerTag.getTaggedOrImpliedValue(edge, ZERO_LONG));
    }

    /**
     * Checks if given {@link AtlasObject} is a pedestrian area or not. A pedestrian area is an
     * {@link AtlasObject} with "area=yes" and "highway=pedestrian" tags.
     *
     * @param object
     *            any {@link AtlasObject}
     * @return true if the object is a pedestrian area
     */
    private boolean isPedestrianArea(final AtlasObject object)
    {
        return Validators.isOfType(object, AreaTag.class, AreaTag.YES)
                && Validators.isOfType(object, HighwayTag.class, HighwayTag.PEDESTRIAN);
    }

    /**
     * Checks if an intersecting edge has a different osm id than the area, does not have pedestrian
     * highway tags, is car navigable and is of the same level or layer as the area.
     *
     * @param edge
     *            any intersecting edge
     * @param area
     *            Area
     * @return true if the edge is a valid intersecting edge
     */
    private boolean isValidIntersectingEdge(final Edge edge, final Area area)
    {
        return
        // We do not want any pedestrian edges that are also pedestrian areas.
        // Currently, pedestrian ways are ingested as both edges and as areas.
        edge.getOsmIdentifier() != area.getOsmIdentifier() && edge.isMainEdge()
                && !HighwayTag.isPedestrianNavigableHighway(edge)
                // Valid edge should be a car navigable highway
                && (HighwayTag.isCarNavigableHighway(edge)
                        // or a cycleway with no foot access
                        || (Validators.isOfType(edge, HighwayTag.class, HighwayTag.CYCLEWAY)
                                && Validators.isOfType(edge, FootTag.class, FootTag.NO))
                        // or a service road that is an alley, parking aisle, driveway, emergency
                        // access or drive through
                        || Validators.isOfType(edge, ServiceTag.class, ServiceTag.ALLEY,
                                ServiceTag.PARKING_AISLE, ServiceTag.DRIVEWAY,
                                ServiceTag.EMERGENCY_ACCESS, ServiceTag.DRIVE_THROUGH))
                // and should have the same layer or level tag if any, as the area
                && this.isOfSameElevation(edge, area);
    }
}
