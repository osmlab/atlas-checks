package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RoadNameSpellingConsistencyCheckWalker extends EdgeWalker
{

    public RoadNameSpellingConsistencyCheckWalker(final Edge startEdge, final Distance maximumSearchDistance, final int maximumAllowedDifferences)
    {
        super(startEdge, getCandidateEdges(startEdge, maximumAllowedDifferences), isWithinMaximumSearchDistance(startEdge, maximumSearchDistance));
    }

    private static Predicate<Edge> getCandidateEdges(final Edge startEdge, final int maximumAllowedDifferences)
    {
        //evaluate name tag of startEdge vs incoming Edge
        return incomingEdge -> {
            int similarityIndex = similarityIndex(incomingEdge, startEdge);
            return similarityIndex >= maximumAllowedDifferences && similarityIndex < 0;
        };
    }

    private static Function<Edge, Stream<Edge>> isWithinMaximumSearchDistance(final Edge startEdge, final Distance maximumSearchDistance)
    {
        return incomingEdge -> incomingEdge.end().getLocation().distanceTo(startEdge.start().getLocation()).isLessThanOrEqualTo(maximumSearchDistance) ? incomingEdge.connectedEdges().stream() : null;
    }

    @SuppressWarnings("squid:S3655")
    private static int similarityIndex(final Edge incomingEdge, final Edge startEdge)
    {
        if(!incomingEdge.getName().isPresent())
        {
            return Integer.MIN_VALUE; //always skipped
        }
        String startEdgeName = startEdge.getName().get();  //NOSONAR because isPresent() was checked in the calling class
        String incomingEdgeName = incomingEdge.getName().get();
        if(startEdgeName.equals(incomingEdgeName))
        {
            return 0;
        }
        if(startEdgeName.length() == incomingEdgeName.length())
        {

        }
    }

}
