package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.spark_project.guava.primitives.Ints;

/**
 * A RoadNameSpellingConsistencyCheckWalker can be used to collect all edges that have NameTag
 * values that are at some Levenshtein distance (configurable) from the starting edge's NameTag
 * value. Collected edges are within some linear search area (configurable).
 *
 * @author seancoulter
 */
class RoadNameSpellingConsistencyCheckWalker extends EdgeWalker
{

    // Matches identifiers sometimes found in road names. E.g. the 'A' in Road A, the "12c" in 12c
    // Street, and the "Y6" in Y6 Drive.
    // Identifiers are defined to be any space-delimited string that either contains at least one
    // digit OR contains a single character which may be preceded or followed by a single
    // punctuation character.
    // We consider names with different identifiers to be from different roads, and as such we don't
    // flag for spelling inconsistencies between identifiers.
    private static final String ALPHANUMERIC_IDENTIFIER_STRING_REGEX = ".*\\p{Nd}+.*";
    private static final String CHARACTER_IDENTIFIER_STRING_REGEX = "\\p{P}.\\p{P}|.\\p{P}|\\p{P}.|^.$";

    private static final String WHITESPACE_REGEX = "\\s+";

    /**
     * Evaluate the {@link NameTag}s of the startingEdge and an incomingEdge to see if their
     * spellings are inconsistent with one another.
     *
     * @param startEdge
     *            the edge from which the search started
     * @param maximumAllowedDifferences
     *            the number of Levenshtein edits allowed
     * @return true if incomingEdge's name is at most the desired Levenshtein distance from the
     *         start edge's name
     */
    @SuppressWarnings("squid:S3655")
    static Predicate<Edge> isEdgeWithInconsistentSpelling(final Edge startEdge,
            final int maximumAllowedDifferences)
    {
        return incomingEdge ->
        {
            // NOSONAR because we've filtered out Edges without names
            final String startEdgeName = startEdge.getName().get();
            final String incomingEdgeName = incomingEdge.getName().get();
            final int similarityIndex = similarityIndex(incomingEdgeName, startEdgeName);
            return similarityIndex <= maximumAllowedDifferences && similarityIndex > 0;
        };
    }

    /**
     * Determine the cost of substituting two characters in the Levenshtein distance calculation.
     *
     * @param first
     *            a character to examine for equality with second
     * @param second
     *            a character to examine for equality with first
     * @return 0 if first & second are equal, 1 otherwise
     */
    private static int costOfSubstitution(final char first, final char second)
    {
        return first == second ? 0 : 1;
    }

    /**
     * The function used to collect {@link Edge}s that fall within the search area.
     *
     * @param startEdge
     *            the edge from which the search started
     * @param maximumSearchDistance
     *            the maximum distance from the end of the incoming edge to the start of the
     *            starting edge
     * @return A stream of edges that fall in the search area
     */
    private static Function<Edge, Stream<Edge>> edgesWithinMaximumSearchDistance(
            final Edge startEdge, final Distance maximumSearchDistance)
    {
        return incomingEdge -> incomingEdge.end().getLocation()
                .distanceTo(startEdge.start().getLocation())
                .isLessThanOrEqualTo(maximumSearchDistance)
                        ? incomingEdge.connectedEdges().stream()
                                .filter(edge -> edge.isMasterEdge() && edge.getName().isPresent())
                        : Stream.empty();
    }

    /**
     * Compute the Levenshtein distance between two road names. Code used to calculate Levenshtein
     * distance is adapted from https://www.baeldung.com/java-levenshtein-distance.
     *
     * @param incomingEdgeName
     *            the name of the next edge in the search area
     * @param startingEdgeName
     *            the name of the edge from which the search started
     * @return the Levenshtein distance between two edge's names
     */
    private static int getLevenshteinDistance(final String incomingEdgeName,
            final String startingEdgeName)
    {
        final int[][] results = new int[incomingEdgeName.length() + 1][startingEdgeName.length()
                + 1];

        final List<String> incomingEdgeNameAlphanumericIdentifierStrings = Arrays
                .stream(incomingEdgeName.split(WHITESPACE_REGEX))
                .filter(substring -> substring.matches(ALPHANUMERIC_IDENTIFIER_STRING_REGEX)
                        || substring.matches(CHARACTER_IDENTIFIER_STRING_REGEX))
                .collect(Collectors.toList());
        final List<String> startingEdgeNameAlphanumericIdentifierStrings = Arrays
                .stream(startingEdgeName.split(WHITESPACE_REGEX))
                .filter(substring -> substring.matches(ALPHANUMERIC_IDENTIFIER_STRING_REGEX)
                        || substring.matches(CHARACTER_IDENTIFIER_STRING_REGEX))
                .collect(Collectors.toList());

        // If the two street names have different alphanumeric identifier strings anywhere in their
        // names, they're classified as being from different roads.
        final long incomingEdgeNameIdentifierCount = incomingEdgeNameAlphanumericIdentifierStrings
                .size();
        final long startingEdgeNameIdentifierCount = startingEdgeNameAlphanumericIdentifierStrings
                .size();
        final long combinedIdentifierCount = Stream
                .concat(incomingEdgeNameAlphanumericIdentifierStrings.stream(),
                        startingEdgeNameAlphanumericIdentifierStrings.stream())
                .distinct().count();
        if (combinedIdentifierCount > incomingEdgeNameIdentifierCount
                || combinedIdentifierCount > startingEdgeNameIdentifierCount)
        {
            return -1;
        }

        // We now know that the street names have the same identifiers or no identifiers at all.
        // Compute Levenshtein distance as usual
        for (int incomingEdgeNameIndex = 0; incomingEdgeNameIndex <= incomingEdgeName
                .length(); incomingEdgeNameIndex++)
        {
            for (int startingEdgeNameIndex = 0; startingEdgeNameIndex <= startingEdgeName
                    .length(); startingEdgeNameIndex++)
            {

                if (incomingEdgeNameIndex == 0)
                {
                    results[incomingEdgeNameIndex][startingEdgeNameIndex] = startingEdgeNameIndex;
                }

                else if (startingEdgeNameIndex == 0)
                {
                    results[incomingEdgeNameIndex][startingEdgeNameIndex] = incomingEdgeNameIndex;
                }

                else
                {
                    results[incomingEdgeNameIndex][startingEdgeNameIndex] = Ints.min(
                            results[incomingEdgeNameIndex - 1][startingEdgeNameIndex - 1]
                                    + costOfSubstitution(
                                            incomingEdgeName.charAt(incomingEdgeNameIndex - 1),
                                            startingEdgeName.charAt(startingEdgeNameIndex - 1)),
                            results[incomingEdgeNameIndex - 1][startingEdgeNameIndex] + 1,
                            results[incomingEdgeNameIndex][startingEdgeNameIndex - 1] + 1);
                }
            }
        }
        return results[incomingEdgeName.length()][startingEdgeName.length()];
    }

    /**
     * Wrapper for getLevenshteinDistance(). Handles case where the incomingEdge has the same name
     * as the startingEdge before computing the Levenshtein distance.
     *
     * @param incomingEdgeName
     *            the next edge in the search area
     * @param startEdgeName
     *            the edge from which the search started
     * @return the Levenshtein distance between two edge's names, or -1 if the incoming edge doesn't
     *         have a name, or 0 if the names are the same
     */

    private static int similarityIndex(final String incomingEdgeName, final String startEdgeName)
    {
        return startEdgeName.equals(incomingEdgeName) ? 0
                : getLevenshteinDistance(incomingEdgeName, startEdgeName);
    }

    /**
     * Walker for {@link RoadNameSpellingConsistencyCheck}.
     *
     * @param startEdge
     *            the edge from which the search started
     * @param maximumSearchDistance
     *            the maximum distance from the end of the incoming edge to the start of the
     *            starting edge
     */
    RoadNameSpellingConsistencyCheckWalker(final Edge startEdge,
            final Distance maximumSearchDistance)
    {
        super(startEdge, edgesWithinMaximumSearchDistance(startEdge, maximumSearchDistance));
    }

}
