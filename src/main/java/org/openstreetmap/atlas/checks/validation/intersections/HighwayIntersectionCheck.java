package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.FordTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LeisureTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags waterway and power line edge items that are crossed by navigable edges (having way specific
 * highway tag). If the way is a waterway and the crossing way has {@code ford=yes} or
 * {@code leisure=slipway} tags, then the crossing is accepted. {@code dam} and {@code weir}
 * waterways are not checked, those type of ways can cross other highways.
 *
 * @author pako.todea
 * @author brianjor
 */
public class HighwayIntersectionCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -2100623356724302728L;
    private static final String INSTRUCTION_FORMAT = "The way with id {0,number,#} has invalid intersections with {1}. A navigable way should not share nodes with non-navigable features. Either the way is inproperly tagged or is a combination of what should be two separate ways (highway and the other non-navigable feature).";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(INSTRUCTION_FORMAT);

    public HighwayIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return !this.isFlagged(object)
                && TypePredicates.IS_EDGE.test(object)
                && ((Edge) object).isMainEdge()
                && HighwayTag.isCarNavigableHighway(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        System.out.println("Testing edge: " + edge);
        final OsmWayWalker wayWalker = new OsmWayWalker(edge);
        final Set<Edge> wayEdges = wayWalker.collectEdges();
        final Stream<Edge> edgesConnectedToWay = wayEdges.stream().map(Edge::connectedEdges)
                .flatMap(Set::stream)
                .filter(connectedEdge -> !this.hasSameOSMId(connectedEdge, edge));
        
        System.out.println("Connected edges");
        wayEdges.stream().map(Edge::connectedEdges)
                .flatMap(Set::stream)
                .filter(connectedEdge -> !this.hasSameOSMId(connectedEdge, edge))
                .forEach(System.out::println);;

        final Set<Edge> invalidconnectingEdges = edgesConnectedToWay
                .filter(connectedEdge -> TagPredicates.IS_POWER_LINE.test(connectedEdge)
                        || !FordTag.isYes(edge))
                .filter(connectedEdge -> TagPredicates.IS_POWER_LINE.test(connectedEdge)
                        || this.isWaterwayToCheck(connectedEdge))
                .filter(connectedEdge -> TagPredicates.IS_POWER_LINE.test(connectedEdge)
                        || !this.isSlipway(edge))
                // .filter(connectedEdge -> TagPredicates.IS_POWER_LINE.test(connectedEdge)
                //         || this.isWaterwayToCheck(connectedEdge))
                // .filter(connectedEdge -> !(FordTag.isYes(edge) && this.isWaterWay(connectedEdge)))
                // .filter(connectedEdge -> !(this.isSlipway(edge) && this.isWaterWay(connectedEdge)))
                .collect(Collectors.toSet());
        System.out.println("Invalid connected edges");
        invalidconnectingEdges.forEach(System.out::println);

        if (!invalidconnectingEdges.isEmpty())
        {
            return this.createFlag(edge, wayEdges, invalidconnectingEdges);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Function that creates highway intersection check flag.
     *
     * @param edge
     *            Atlas object.
     * @param crossingEdges
     *            collected edges for a given atlas object.
     * @return newly created highway intersection check flag including crossing edges locations.
     */
    private Optional<CheckFlag> createFlag(final Edge edge, final Set<Edge> wayEdges,
            final Set<Edge> crossingEdges)
    {
        final List<Location> points = wayEdges.stream()
                .map(wayEdge -> crossingEdges.stream()
                        .map(crossEdge -> this.getIntersections(wayEdge, crossEdge))
                        .flatMap(Set::stream).map(Node::getLocation).collect(Collectors.toList()))
                .flatMap(List::stream).distinct().collect(Collectors.toList());

        this.markAsFlagged(edge.getOsmIdentifier());
        final Set<Long> crossingIds = crossingEdges.stream().map(AtlasObject::getOsmIdentifier)
                .collect(Collectors.toSet());
        final String instruction = this.getLocalizedInstruction(0, edge.getOsmIdentifier(),
                crossingIds);
        return Optional.of(this.createFlag(wayEdges, instruction, points));
    }

    /**
     * This function returns the set of intersection locations for the given edges.
     *
     * @param firstEdge
     *            the first Edge
     * @param secondEdge
     *            the second edge
     * @return set of intersection locations.
     */
    private Set<Node> getIntersections(final Edge firstEdge, final Edge secondEdge)
    {
        final Set<Node> locations = firstEdge.connectedNodes();
        locations.retainAll(secondEdge.connectedNodes());
        return locations;
    }

    /**
     * Checks if two AtlasObjects have the same OSM Identifier.
     * 
     * @param first
     *            the first AtlasObject to check
     * @param second
     *            the second AtlasObject to check
     * @return true if both have the same OSM Identifier, false otherwise.
     */
    private boolean hasSameOSMId(final AtlasObject first, final AtlasObject second)
    {
        return first.getOsmIdentifier() == second.getOsmIdentifier();
    }

    /**
     * Check if the AtlasObject is flagged, uses OSM Identifier.
     * 
     * @param object
     *            the AtlasObject to check if flagged
     * @return true if flagged, false otherwise.
     */
    private boolean isFlagged(final AtlasObject object)
    {
        return this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * Check if AtlasObject contains "leisure=slipway" tag.
     * 
     * @param object
     *            the object to check the tags for
     * @return true if object has tag "leisure=slipway", false otherwise.
     */
    private boolean isSlipway(final AtlasObject object)
    {
        return Validators.isOfType(object, LeisureTag.class, LeisureTag.SLIPWAY);
    }

    private boolean isWaterWay(final Taggable taggable)
    {
        return Validators.hasValuesFor(taggable, WaterwayTag.class);
    }

    /**
     * Checks whether the given {@link AtlasObject} is a waterway to check.
     *
     * @param object
     *            the {@link AtlasObject} to check
     * @return true if the given {@link AtlasObject} should be ckecked
     */
    private boolean isWaterwayToCheck(final AtlasObject object)
    {
        boolean validForCheck = false;
        if (this.isWaterWay(object))
        {
            System.out.println("is waterway");
            final Optional<WaterwayTag> waterwayTagValue = WaterwayTag.get(object);
            if (waterwayTagValue.isPresent())
            {
                final String waterwayTag = waterwayTagValue.get().name();
                validForCheck = !(HighwayTag.highwayTag(object).isPresent()
                        && (WaterwayTag.DAM.toString().equalsIgnoreCase(waterwayTag)
                                || WaterwayTag.WEIR.toString().equalsIgnoreCase(waterwayTag)));
                System.out.println("is valid for check? " + validForCheck);
            }
        }
        return validForCheck;
    }
}
