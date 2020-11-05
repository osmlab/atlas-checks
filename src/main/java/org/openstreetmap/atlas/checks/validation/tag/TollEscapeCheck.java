package org.openstreetmap.atlas.checks.validation.tag;

import java.util.*;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.*;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

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

    public TollEscapeCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        if (TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMainEdge()
                && HighwayTag.isCarNavigableHighway(object)
                && !isFlagged(object.getOsmIdentifier()))
//                && object.getOsmIdentifier() == 55467435)
        {
            //
            Edge edgeInQuestion = ((Edge) object).getMainEdge();
            final Map<String, String> keySet = edgeInQuestion.getOsmTags();
            return hasTollTag(keySet) && !isPrivateAccess(keySet) && !edgeIntersectsTollFeature(edgeInQuestion);
        }
        return false;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        markAsFlagged(object.getOsmIdentifier());

        Edge edgeInQuestion = ((Edge) object).getMainEdge();
        Set<Long> abtForwardObjectIds = new HashSet<>();
        Set<Long> abtbackwardObjectIds = new HashSet<>();

        System.out.println("edge in question: " + edgeInQuestion.getOsmIdentifier());

        if (forwardIsEscapable(edgeInQuestion, abtForwardObjectIds))
        {
            if (backwardsIsEscapable(edgeInQuestion, abtbackwardObjectIds))
            {
                System.out.println("remove toll tag for: " + edgeInQuestion.getOsmIdentifier());
            } else
            {
                System.out.println("correct tag, backwards not escapable: " + edgeInQuestion.getOsmIdentifier());
            }
        } else
        {
            System.out.println("correct toll, forward is not escapable: " + edgeInQuestion.getOsmIdentifier());
        }


        return Optional.empty();
    }

    private boolean edgeIntersectsTollFeature(Edge edge)
    {
        Iterable<Area> intersectingAreas = edge.getAtlas().areasIntersecting(edge.bounds());
        Iterable<Node> edgeNodes = edge.connectedNodes();
        for (Area area : intersectingAreas)
        {
            Map<String, String> areaTags = area.getOsmTags();
            if(areaTags.containsKey("barrier") && (areaTags.get("barrier").contains("toll")))
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

    private boolean backwardsIsEscapable(Edge edge, Set<Long> abtObjectIds)
    {
        Set<Edge> inEdges = getInEdges(edge);
        if (hasAtLeastOneInEdge(edge))
        {
            for (Edge inEdge : inEdges)
            {
                if (!abtObjectIds.contains(inEdge.getIdentifier())
                        && inEdge.highwayTag().isMoreImportantThan(HighwayTag.SERVICE))
                {
                    abtObjectIds.add(inEdge.getIdentifier());
                    final Map<String, String> keySet = inEdge.getOsmTags();
                    if (inEdges.size() == 1 && edgeIntersectsTollFeature(inEdge))
                    {
                        System.out.println("single inEdge and intersects toll feature: " + inEdge.getOsmIdentifier());
                        return false;
                    }
                    if ((!keySet.containsKey("toll")) || (keySet.containsKey("toll") && keySet.get("toll").equals("no")))
                    {
                        System.out.println("inEdge has toll=no or no toll tag on this way: " + inEdge.getOsmIdentifier());
                        return true;
                    }
                    if (!edgeIntersectsTollFeature(inEdge)
                            && keySet.containsKey("toll") && keySet.get("toll").equals("yes"))
                    {
                        System.out.println("inEdge recursing: " + inEdge.getOsmIdentifier());
                        System.out.println("inEdge end node: " + inEdge.end().getOsmIdentifier());
                        return backwardsIsEscapable(inEdge, abtObjectIds);
                    }
                }
            }
        }
        System.out.println("no inEdges.");
        return false;
    }

    private boolean forwardIsEscapable(Edge edge, Set<Long> abtObjectIds)
    {
        Set<Edge> outEdges = getOutEdges(edge);
        if (hasAtLeastOneOutEdge(edge))
        {
            for (Edge outEdge : outEdges)
            {
                if (!abtObjectIds.contains(outEdge.getIdentifier())
                        && outEdge.highwayTag().isMoreImportantThan(HighwayTag.SERVICE))
                {
                    abtObjectIds.add(outEdge.getIdentifier());
    //                System.out.println("outEdge checked: " + outEdge);
                    final Map<String, String> keySet = outEdge.getOsmTags();
                    if (outEdges.size() == 1 && edgeIntersectsTollFeature(outEdge))
                    {
                        System.out.println("single outEdge and intersects toll feature: " + outEdge.getOsmIdentifier());
                        return false;
                    }
                    if (!keySet.containsKey("toll") || (keySet.containsKey("toll") && keySet.get("toll").equals("no")))
                    {
                        System.out.println("outEdge has toll=no or no toll tag on this way: " + outEdge.getOsmIdentifier());
                        return true;
                    }
                    if (!edgeIntersectsTollFeature(outEdge)
                            && keySet.containsKey("toll") && keySet.get("toll").equals("yes"))
                    {
                        System.out.println("outEdge recursing.");
                        System.out.println("outEdge end node: " + outEdge.end().getOsmIdentifier());
                        return forwardIsEscapable(outEdge, abtObjectIds);
                    }
                }
            }
        }
        System.out.println("no outEdges.");
        return false;
    }

    private Set<Edge> getInEdges(Edge edge)
    {
        return edge.inEdges().stream().filter(someEdge ->
                someEdge.getIdentifier() > 0
                        && HighwayTag.isCarNavigableHighway(someEdge)).collect(Collectors.toSet());
    }

    private Set<Edge> getOutEdges(Edge edge)
    {
        return edge.outEdges().stream().filter(someEdge ->
                someEdge.getIdentifier() > 0
                        && HighwayTag.isCarNavigableHighway(someEdge)).collect(Collectors.toSet());
    }

    private boolean isPrivateAccess(final Map<String, String> tags)
    {
        if(tags.containsKey("access"))
        {
            return tags.get("access").equals("private");
        }
        return false;
    }
    private boolean hasAtLeastOneInEdge(Edge edge)
    {
        return getInEdges(edge).size() >= 1;
    }

    private boolean hasAtLeastOneOutEdge(Edge edge)
    {
        return getOutEdges(edge).size() >= 1;
    }
    private boolean hasTollTag(final Map<String, String> tags)
    {
        return tags.keySet().stream()
                .anyMatch(tag -> tag.equals(TollTag.KEY));
    }
}
