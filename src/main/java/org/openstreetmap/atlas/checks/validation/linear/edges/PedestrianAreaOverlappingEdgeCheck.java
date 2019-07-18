package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.LocationTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sayana_saithu
 */
public class PedestrianAreaOverlappingEdgeCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "Pedestrian area {0,number,#} is overlapping way id(s) {1}.");
    /**
     * Default constructor
     *
     * @param configuration {@link Configuration} required to construct any Check
     */
    public PedestrianAreaOverlappingEdgeCheck(
            Configuration configuration)
    {
        super(configuration);
    }

    /**
     * Checks to see whether the supplied object class type is valid for this particular check
     *
     * @param object The {@link AtlasObject} you are checking
     * @return true if it is
     */
    @Override public boolean validCheckForObject(AtlasObject object)
    {
        // Valid object for the check is a pedestrian area that has not been flagged
        return object instanceof Area &&
                this.isPedestrianArea(object)
                && !isFlagged(object.getOsmIdentifier());
    }

    @Override protected Optional<CheckFlag> flag(AtlasObject object)
    {
        final Area area = (Area) object;
        // All segments of the area
        final List<Segment> segments = area.asPolygon().segments();
        // Get all start and end locations of all segments in the area
        final Set<Location> locationsInSegments = new HashSet<>();
        segments.forEach(segment-> {
            locationsInSegments.add(segment.start());
            locationsInSegments.add(segment.end());});
        // Filter and collect all valid intersecting edges
        final Set<Edge> filteredIntersectingEdges = Iterables
                .stream(area.getAtlas().edgesIntersecting(area.asPolygon()))
                .filter(edge -> this.isValidIntersectingEdge(edge, area)).collectToSet();
        Set<AtlasObject> overlappingEdges = new HashSet<>();
        final Polygon areaPolygon = area.asPolygon();
        for(Edge edge: filteredIntersectingEdges)
        {
            final Location edgeStartLocation = edge.start().getLocation();
            final Location edgeEndLocation = edge.end().getLocation();
            // Checks if intersection is at start of the edge
            final boolean intersectsAtStart = locationsInSegments.contains(edgeStartLocation);
            // Checks if intersection is at end of the edge
            final boolean intersectsAtEnd = locationsInSegments.contains(edgeEndLocation);
            // Filtered segments based on intersection points
            final Set<Segment> filteredSegments = segments.stream()
                    .filter(segment -> edgeStartLocation.equals(segment.start())||edgeStartLocation.equals(segment.end())
                                    || edgeEndLocation.equals(segment.start())|| edgeEndLocation.equals(segment.end())
                    )
                    .collect(Collectors.toSet());
            // If both ends of the edge intersects the area, check if it is properly snapped
            if(intersectsAtStart && intersectsAtEnd)
            {
                // If none of the filtered segments have both the start and end nodes, then add the
                // edge and all connected edges within the area to a set to flag them.
                if(filteredSegments.stream().noneMatch(segment ->
                        (edgeStartLocation.equals(segment.start())  ||
                                edgeStartLocation.equals(segment.end()))
                                && (edgeEndLocation.equals(segment.start()) || edgeEndLocation.equals(segment
                                .end()) )))
                {
                    // Collect all connected edges that are within the pedestrian area
                    edge.connectedEdges().stream().filter(connectedEdge-> connectedEdge.isMasterEdge() &&
                            areaPolygon.fullyGeometricallyEncloses(connectedEdge.asPolyLine())).forEach(
                            overlappingEdges::add);
                    // Add the intersecting edge as well to the set
                    overlappingEdges.add(edge);
                }
            }

            // If any one of the end of the connected edge is fully enclosed within the area,
            // flag the edge and all its connected edges that are within the area.
            else if( (intersectsAtStart && areaPolygon
                    .fullyGeometricallyEncloses(edgeEndLocation)) || (!intersectsAtStart && intersectsAtEnd &&areaPolygon.
                    fullyGeometricallyEncloses(edgeStartLocation)))
            {
                edge.connectedEdges().stream().filter(connectedEdge-> connectedEdge.isMasterEdge() &&
                        areaPolygon.fullyGeometricallyEncloses(connectedEdge.asPolyLine())).forEach(
                        overlappingEdges::add);
                overlappingEdges.add(edge);
            }
        }
        if(!overlappingEdges.isEmpty())
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

    /**
     * Collects all atlas identifiers of given set of {@link AtlasObject}s
     *
     * @param objects set of {@link AtlasObject}s
     * @return {@link Iterable<String>} containing the atlas identifiers of input objects
     */
    private Iterable<String> getIdentifiers(final Set<AtlasObject> objects)
    {
        return Iterables.stream(objects).map(AtlasObject::getIdentifier).map(String::valueOf)
                .collectToList();
    }

    /**
     * Checks if an intersecting edge has a different osm id than the area, does not have pedestrian
     * highway tags and is of the same level or layer as the area.
     *
     * @param intersectingEdge any intersecting edge
     * @param area Area
     * @return true if the edge is a valid intersecting edge
     */
    private boolean isValidIntersectingEdge(final Edge intersectingEdge, final Area area)
    {
        return // We do not want any pedestrian edges that are also pedestrian areas. Currently,
                // pedestrian ways are ingested as both edges and as areas.
                intersectingEdge.getOsmIdentifier()!=area.getOsmIdentifier()
                && intersectingEdge.isMasterEdge()
                // Valid edge should have a highway class other than foot way, path, pedestrian or steps
                        && Validators.hasValuesFor(intersectingEdge,HighwayTag.class) &&
                        Validators.isNotOfType(intersectingEdge,HighwayTag.class,HighwayTag.FOOTWAY,
                                HighwayTag.PATH, HighwayTag.PEDESTRIAN, HighwayTag.STEPS)
                        // and should have the same layer or level tag if any, as the area
                        && area.getTag(LayerTag.KEY).orElse("").equals(intersectingEdge.getTag(LayerTag.KEY).orElse(""))
                        && area.getTag(
                        LocationTag.KEY).orElse("").equals(intersectingEdge.getTag(LocationTag.KEY).orElse(""));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if given {@link AtlasObject} is a pedestrian area or not. A pedestrian area is an
     * {@link AtlasObject} with "area=yes" and "highway=pedestrian" tags.
     *
     * @param object any {@link AtlasObject}
     * @return true if the object is a pedestrian area
     */
    private boolean isPedestrianArea(final AtlasObject object)
    {
        return Validators.isOfType(object,
                AreaTag.class, AreaTag.YES) &&
                Validators.isOfType(object, HighwayTag.class, HighwayTag.PEDESTRIAN);
    }
}