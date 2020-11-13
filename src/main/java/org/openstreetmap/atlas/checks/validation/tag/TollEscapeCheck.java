package org.openstreetmap.atlas.checks.validation.tag;

import java.util.*;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.*;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import static java.lang.Math.pow;

/**
 * Auto generated Check template
 *
 * @author greichenberger
 */
public class TollEscapeCheck extends BaseCheck<Long>
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    private static final String EDGE_DEVIATION_INSTRUCTION = "Way {0,number,#} is crude. Please add more nodes/rearrange current nodes to more closely match the road from imagery";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(EDGE_DEVIATION_INSTRUCTION);
    private final Set<Long> markedInconsistentToll = new HashSet<>();

    /**
     *
     * @param configuration config file params if any.
     */
    public TollEscapeCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
    }

    /**
     *
     * @param object The {@link AtlasObject} you are checking
     * @return validation check
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        if (TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMainEdge()
                && HighwayTag.isCarNavigableHighway(object)
                && !isFlagged(object.getOsmIdentifier()))
//                && object.getOsmIdentifier() == 439101933)
        {
            //
            Edge edgeInQuestion = ((Edge) object).getMainEdge();
            final Map<String, String> keySet = edgeInQuestion.getOsmTags();
            return !isPrivateAccess(keySet);
//            return hasTollYesTag(keySet) && !isPrivateAccess(keySet) && !edgeIntersectsTollFeature(edgeInQuestion);
        }
        return false;
    }

    /**
     *
     * @param object object in question
     * @return flag
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        markAsFlagged(object.getOsmIdentifier());

        Edge edgeInQuestion = ((Edge) object).getMainEdge();
        Long edgeInQuestionId = edgeInQuestion.getOsmIdentifier();
        Map<String, String> edgeInQuestionTags = edgeInQuestion.getOsmTags();
        Set<Long> abtObjectIds = new HashSet<>();
        int recurseCounter = 0;

        if (!hasTollYesTag(edgeInQuestionTags) && edgeIntersectsTollFeature(edgeInQuestion))
        {
            System.out.println("intersects without toll tag: " + edgeInQuestion.getOsmIdentifier());
        }

        if (hasTollYesTag(edgeInQuestionTags))
        {
            Edge escapableInEdge = edgeProvingBackwardsIsEscapable(edgeInQuestion, abtObjectIds, recurseCounter, edgeInQuestionId);
            Edge escapableOutEdge = edgeProvingForwardIsEscapable(edgeInQuestion, abtObjectIds, recurseCounter);

            if (escapableInEdge != null && escapableOutEdge != null)
            {
                if (!hasInconsistentTollTag(escapableOutEdge) && !hasInconsistentTollTag(escapableInEdge))
                {
                    Long nearbyTollFeatureInSide = getNearbyTollFeatureInEdgeSide(edgeInQuestion).orElse(null);
                    Long nearbyTollFeatureOutSide = getNearbyTollFeatureOutEdgeSide(edgeInQuestion).orElse(null);

                    System.out.println("--------------------------------------------------------");
                    System.out.println("edge in question: " + edgeInQuestion.getOsmIdentifier());
                    System.out.println("toll tag needs to be investigated for removal from: " + edgeInQuestionId + ". Check ways below for proper toll modeling based on nearby toll features. Adjust if needed.");
                    System.out.println("nearest toll feature on inEdge side: " + nearbyTollFeatureInSide);
                    System.out.println("Nearest toll feature on outEdge side: " + nearbyTollFeatureOutSide);
//                    System.out.println("edge in question highway tag: " + edgeInQuestion.highwayTag());
                    System.out.println("escapable inEdge: " + escapableInEdge.getOsmIdentifier());
                    System.out.println("escapable outEdge: " + escapableOutEdge.getOsmIdentifier());
                    System.out.println("--------------------------------------------------------");
                }

                if (!markedInconsistentToll.contains(escapableOutEdge.getOsmIdentifier()) && hasInconsistentTollTag(escapableOutEdge))
                {
                    markedInconsistentToll.add(escapableOutEdge.getOsmIdentifier());
                    System.out.println("inconsistent toll tag on proven outEdge: " + escapableOutEdge.getOsmIdentifier());
                }

                if (!markedInconsistentToll.contains(escapableInEdge.getOsmIdentifier()) && hasInconsistentTollTag(escapableInEdge))
                {
                    markAsFlagged(escapableInEdge.getOsmIdentifier());
                    System.out.println("inconsistent toll tag on proven inEdge: " + escapableInEdge.getOsmIdentifier());
                }
            }
        }
        return Optional.empty();
    }

    /**
     *
     * @param edge1 just an edge
     * @param edge2 just another edge
     * @return angle in degrees between edges (segments)
     */
    private double angleBetweenEdges(Edge edge1, Edge edge2)
    {
        final List<Segment> edge1Segments = edge1.asPolyLine().segments();
        final List<Segment> edge2Segments = edge2.asPolyLine().segments();
        Segment edge1EndSeg = edge1Segments.get(edge1Segments.size() - 1);
        Segment edge2StartSeg = edge2Segments.get(0);
        final double aLength = edge1EndSeg.length().asMeters();
        final double bLength = edge2StartSeg.length().asMeters();
        final double cLength = new Segment(edge1EndSeg.start(), edge2StartSeg.end()).length().asMeters();
        return Math.toDegrees(Math.acos(
                (pow(aLength, 2) + pow(bLength, 2) - pow(cLength, 2)) / (2 * aLength * bLength)));
    }

    /**
     *
     * @param edge edge in question
     * @param abtObjectIds already been touched IDs
     * @param recurseCounter how many times this function has recursed.
     * @param edgeInQuestionId straight forward
     * @return edge that proves backwards is escapable
     */
    private Edge edgeProvingBackwardsIsEscapable(Edge edge, Set<Long> abtObjectIds, int recurseCounter, Long edgeInQuestionId)
    {
        Set<Edge> inEdges = getInEdges(edge);

        if (hasAtLeastOneInEdge(edge))
        {
            for (Edge inEdge : inEdges)
            {
                if (!abtObjectIds.contains(inEdge.getIdentifier())
                        && inEdge.highwayTag().isMoreImportantThan(HighwayTag.SERVICE)
                        && hasSameHighwayTag(edge, inEdge)
                        && angleBetweenEdges(inEdge, edge) >= 140.0)
                {
                    abtObjectIds.add(inEdge.getIdentifier());
                    final Map<String, String> keySet = inEdge.getOsmTags();

                    if ((!keySet.containsKey("toll")) || (keySet.containsKey("toll") && keySet.get("toll").equals("no")))
                    {
                        return inEdge;
                    }
                    if (!edgeIntersectsTollFeature(inEdge)
                            && keySet.containsKey("toll") && keySet.get("toll").equals("yes"))
                    {
                        recurseCounter += 1;
                        return edgeProvingBackwardsIsEscapable(inEdge, abtObjectIds, recurseCounter, edgeInQuestionId);
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * @param edge some edge
     * @return boolean if edge intersects toll feature
     */
    private boolean edgeIntersectsTollFeature(Edge edge)
    {

        Iterable<Area> intersectingAreas = edge.getAtlas().areasIntersecting(edge.bounds());
        Iterable<Node> edgeNodes = edge.connectedNodes();
        for (Area area : intersectingAreas)
        {
            boolean areaContainsPolyline = area.asPolygon().overlaps(edge.asPolyLine());
            Map<String, String> areaTags = area.getOsmTags();
            if(areaContainsPolyline && areaTags.containsKey("barrier") && (areaTags.get("barrier").contains("toll")))
            {
                return true;
            }
        }

        for (Node node : edgeNodes)
        {
            Map<String, String> nodeTags = node.getOsmTags();
            if (nodeTags.containsKey("highway") && (nodeTags.get("highway").contains("toll"))
                    || nodeTags.containsKey("barrier") && nodeTags.get("barrier").contains("toll"))
            {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param edge edge in qustion
     * @param abtObjectIds already been touched Ids
     * @param recurseCounter how many times the function has recursed
     * @return edge proving forward is escapable.
     */
    private Edge edgeProvingForwardIsEscapable(Edge edge, Set<Long> abtObjectIds, int recurseCounter)
    {
        Set<Edge> outEdges = getOutEdges(edge);
        if (hasAtLeastOneOutEdge(edge))
        {
            for (Edge outEdge : outEdges)
            {
                if (!abtObjectIds.contains(outEdge.getIdentifier())
                        && outEdge.highwayTag().isMoreImportantThan(HighwayTag.SERVICE)
                        && hasSameHighwayTag(edge, outEdge)
                        && angleBetweenEdges(edge, outEdge) >= 140.0)
                {
                    abtObjectIds.add(outEdge.getIdentifier());
                    final Map<String, String> keySet = outEdge.getOsmTags();

                    if (!keySet.containsKey("toll") || (keySet.containsKey("toll") && keySet.get("toll").equals("no")))
                    {
                        return outEdge;
                    }
                    if (!edgeIntersectsTollFeature(outEdge)
                            && keySet.containsKey("toll") && keySet.get("toll").equals("yes"))
                    {
                        recurseCounter += 1;
                        return edgeProvingForwardIsEscapable(outEdge, abtObjectIds, recurseCounter);
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * @param edge some edge
     * @return in edges that are car navigable and positive (eliminates reverse edges)
     */
    private Set<Edge> getInEdges(Edge edge)
    {
        return edge.inEdges().stream().filter(someEdge ->
                someEdge.getIdentifier() > 0
                        && HighwayTag.isCarNavigableHighway(someEdge)).collect(Collectors.toSet());
    }

    /**
     *
     * @param edge some edge
     * @return out edges that are car navigable and positive (eliminates reverse edges)
     */
    private Set<Edge> getOutEdges(Edge edge)
    {
        return edge.outEdges().stream().filter(someEdge ->
                someEdge.getIdentifier() > 0
                        && HighwayTag.isCarNavigableHighway(someEdge)).collect(Collectors.toSet());
    }

    /**
     *
     * @param edge edge in question
     * @return nearby toll feature id on the in edge side of the edge in question (upstream)
     */
    private Optional<Long> getNearbyTollFeatureInEdgeSide(Edge edge)
    {
        Set<Edge> inEdges = getInEdges(edge);

        for (Edge inEdge : inEdges)
        {
            if (edgeIntersectsTollFeature(inEdge) && angleBetweenEdges(inEdge, edge) >= 140)
            {
                Iterable<Area> intersectingAreas = edge.getAtlas().areasIntersecting(edge.bounds());
                Iterable<Node> edgeNodes = edge.connectedNodes();
                for (Area area : intersectingAreas)
                {
                    boolean areaContainsPolyline = area.asPolygon().overlaps(edge.asPolyLine());
                    Map<String, String> areaTags = area.getOsmTags();
                    if (areaContainsPolyline && areaTags.containsKey("barrier") && (areaTags.get("barrier").contains("toll")))
                    {
                        return Optional.of(area.getOsmIdentifier());
                    }
                }

                for (Node node : edgeNodes) {
                    Map<String, String> nodeTags = node.getOsmTags();
                    if (nodeTags.containsKey("highway") && (nodeTags.get("highway").contains("toll"))
                            || nodeTags.containsKey("barrier") && nodeTags.get("barrier").contains("toll")) {
                        return Optional.of(node.getOsmIdentifier());
                    }
                }
            }
            if (!edgeIntersectsTollFeature(inEdge) && angleBetweenEdges(inEdge, edge) >= 140)
            {
                return getNearbyTollFeatureInEdgeSide(inEdge);
            }
        }
        return Optional.empty();
    }

    /**
     *
     * @param edge edge in question
     * @return nearby toll feature id on the out edge side of the edge in question (downstream)
     */
    private Optional<Long> getNearbyTollFeatureOutEdgeSide(Edge edge)
    {
        Set<Edge> outEdges = getOutEdges(edge);

        for (Edge outEdge : outEdges) {
            if (edgeIntersectsTollFeature(outEdge) && angleBetweenEdges(outEdge, edge) >= 140)
            {
                Iterable<Area> intersectingAreas = edge.getAtlas().areasIntersecting(edge.bounds());
                Iterable<Node> edgeNodes = edge.connectedNodes();
                for (Area area : intersectingAreas)
                {
                    boolean areaContainsPolyline = area.asPolygon().overlaps(edge.asPolyLine());
                    Map<String, String> areaTags = area.getOsmTags();
                    if (areaContainsPolyline && areaTags.containsKey("barrier") && (areaTags.get("barrier").contains("toll")))
                    {
                        return Optional.of(area.getOsmIdentifier());
                    }
                }

                for (Node node : edgeNodes)
                {
                    Map<String, String> nodeTags = node.getOsmTags();
                    if (nodeTags.containsKey("highway") && (nodeTags.get("highway").contains("toll"))
                            || nodeTags.containsKey("barrier") && nodeTags.get("barrier").contains("toll"))
                    {
                        return Optional.of(node.getOsmIdentifier());
                    }
                }
            }
            if (!edgeIntersectsTollFeature(outEdge))
            {
                return getNearbyTollFeatureOutEdgeSide(outEdge);
            }
        }
        return Optional.empty();
    }

    /**
     *
     * @param edge some edge
     * @return tag inconsistencies between 3 consecutive edges.
     */
    private boolean hasInconsistentTollTag(Edge edge)
    {
        Set<Edge> inEdges = edge.inEdges().stream().filter(inEdge ->
                inEdge.getOsmIdentifier() != edge.getOsmIdentifier()
                        && inEdge.getIdentifier() > 0
                        && HighwayTag.isCarNavigableHighway(inEdge)).collect(Collectors.toSet());
        Set<Edge> outEdges = edge.outEdges().stream().filter(outEdge ->
                outEdge.getOsmIdentifier() != edge.getOsmIdentifier()
                        && outEdge.getIdentifier() > 0
                        && HighwayTag.isCarNavigableHighway(outEdge)).collect(Collectors.toSet());
        if (inEdges.size() == 1 && outEdges.size() == 1)
        {
            for (Edge inEdge : inEdges)
            {
                for (Edge outEdge : outEdges)
                {
                    if (hasSameHighwayTag(edge, inEdge) && hasSameHighwayTag(edge, outEdge)
                            && angleBetweenEdges(edge, outEdge) >= 140.0 && angleBetweenEdges(inEdge, edge) >= 140.0)
                    {
                        final Map<String, String> inEdgeOsmTags = inEdge.getOsmTags();
                        final Map<String, String> outEdgeOsmTags = outEdge.getOsmTags();
                        if (bothTollYesTag(inEdgeOsmTags, outEdgeOsmTags))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param edge some edge
     * @return boolean about in edge count
     */
    private boolean hasAtLeastOneInEdge(Edge edge)
    {
        return getInEdges(edge).size() >= 1;
    }

    /**
     *
     * @param edge some edge
     * @return boolean about out edge count
     */
    private boolean hasAtLeastOneOutEdge(Edge edge)
    {
        return getOutEdges(edge).size() >= 1;
    }

    /**
     *
     * @param tags some edge tags
     * @param tags2 some other edge tags
     * @return both sets of tags have toll=yes
     */
    private boolean bothTollYesTag(final Map<String, String> tags, final Map<String, String> tags2)
    {
        return hasTollYesTag(tags) && hasTollYesTag(tags2);

    }

    /**
     *
     * @param tags some edge tags
     * @return if tags contain toll tag.
     */
    private boolean hasTollTag(final Map<String, String> tags)
    {
        return tags.keySet().stream()
                .anyMatch(tag -> tag.equals(TollTag.KEY));
    }

    /**
     *
     * @param tags some edge tags
     * @return if tags contains toll=yes
     */
    private boolean hasTollYesTag(final Map<String, String> tags)
    {
        return tags.keySet().stream()
                .anyMatch(tag -> tag.equals(TollTag.KEY)) && tags.get("toll").equals("yes");
    }

    /**
     *
     * @param edge1 some edge
     * @param edge2 some other edge
     * @return boolean regarding if they have same highway tag?\
     */
    private boolean hasSameHighwayTag(Edge edge1, Edge edge2)
    {
        if (HighwayTag.highwayTag(edge1).isPresent() && HighwayTag.highwayTag(edge2).isPresent())
        {
            return edge1.highwayTag().equals(edge2.highwayTag());
        }
        return false;
    }

    /**
     *
     * @param tags some edge tags
     * @return boolean regarding access=private tags.
     */
    private boolean isPrivateAccess(final Map<String, String> tags)
    {
        if(tags.containsKey("access"))
        {
            return tags.get("access").equals("private");
        }
        return false;
    }

}
