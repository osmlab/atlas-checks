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
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.FootTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.LocationTag;
import org.openstreetmap.atlas.tags.ServiceTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags pedestrian areas that are not properly snapped to its valid
 * intersecting/overlapping edges. A pedestrian area is an {@link Area} with {@link HighwayTag} =
 * "pedestrian" tag. Valid intersecting edges are edges with the same elevation(same
 * {@link LayerTag}, {@link LocationTag}, {@link LevelTag}) as the area, and highway tag value not
 * equal to foot way, pedestrian, steps and path. The pedestrian area and any valid intersecting/
 * overlapping edge that is not snapped to the area are flagged along with its connected edges that
 * are within the pedestrian area.
 *
 * @author sayas01
 */
public class PedestrianAreaOverlappingEdgeCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 1861527706740836635L;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList("Pedestrian area {0,number,#} is overlapping way id(s) {1}.");

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public PedestrianAreaOverlappingEdgeCheck(final Configuration configuration)
    {
        super(configuration);
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
            final Location edgeStartLocation = edge.start().getLocation();
            final Location edgeEndLocation = edge.end().getLocation();
            // Checks if intersection is at start of the edge
            final boolean intersectsAtStart = locationsInSegments.contains(edgeStartLocation);
            // Checks if intersection is at end of the edge
            final boolean intersectsAtEnd = locationsInSegments.contains(edgeEndLocation);
            // If both ends of the edge intersects the area, check if it is properly snapped by
            // checking if both the intersecting points are on the same segment.
            // If none of the filtered segments have both the start and end nodes, then add the
            // edge and all its connected edges that are within the area to flag them.
            if ((intersectsAtStart && intersectsAtEnd
                    && this.isNotSnappedToSegments(segments, edgeStartLocation, edgeEndLocation))
                    // If any one of the end of the connected edge is fully enclosed within the
                    // area, flag the edge and all its connected edges that are within the area.
                    || (intersectsAtStart && !intersectsAtEnd
                            && areaPolygon.fullyGeometricallyEncloses(edgeEndLocation))
                    || (intersectsAtEnd && !intersectsAtStart
                            && areaPolygon.fullyGeometricallyEncloses(edgeStartLocation)))
            {
                // Collect all connected edges that are within the pedestrian area
                edge.connectedEdges().stream()
                        .filter(connectedEdge -> this.isValidIntersectingEdge(connectedEdge, area)
                                && areaPolygon
                                        .fullyGeometricallyEncloses(connectedEdge.asPolyLine()))
                        .forEach(overlappingEdges::add);
                // Add the intersecting edge as well to the set
                overlappingEdges.add(edge);
            }

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
     * Checks if the locations are not snapped to the edges or not. If snapped, both the locations
     * should lie on the same segment.
     *
     * @param segments
     *            Set of segments
     * @param locationOne
     *            any location
     * @param locationTwo
     *            any location
     * @return true if none of the segments have both the locations in it.
     */
    private boolean isNotSnappedToSegments(final List<Segment> segments, final Location locationOne,
            final Location locationTwo)
    {
        return segments.stream().noneMatch(segment -> (locationOne.equals(segment.start())
                || locationOne.equals(segment.end()))
                && (locationTwo.equals(segment.start()) || locationTwo.equals(segment.end())));
    }

    /**
     * Checks if the given edge and area have the same elevation or not.
     *
     * @param edge
     *            any given edge
     * @param area
     *            any given area
     * @return true if the edge and area have the same Layer and Location tag values
     */
    private boolean isOfSameElevation(final Edge edge, final Area area)
    {
        return area.getTag(LayerTag.KEY).orElse("").equals(edge.getTag(LayerTag.KEY).orElse(""))
                && area.getTag(LocationTag.KEY).orElse("")
                        .equals(edge.getTag(LocationTag.KEY).orElse(""))
                && area.getTag(LevelTag.KEY).orElse("")
                        .equals(edge.getTag(LevelTag.KEY).orElse(""));
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
        edge.getOsmIdentifier() != area.getOsmIdentifier() && edge.isMasterEdge()
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
