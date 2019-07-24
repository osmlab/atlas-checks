package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * A RoadNameSpellingConsistencyCheckWalker can be used to collect all edges that have NameTag
 * values that are at some Levenshtein distance (configurable) from the starting edge's NameTag
 * value. Collected edges are within some linear search area (configurable).
 *
 * @author seancoulter
 */
class RoadNameSpellingConsistencyCheckWalker extends EdgeWalker
{

    /**
     * Directional character, sometimes appended to start/end of road name (e.g. Banana Ave W)
     */
    enum Direction
    {
        N,
        S,
        E,
        W
    }

    private static final EnumSet<Direction> DIRECTIONS = EnumSet.of(Direction.N, Direction.S,
            Direction.E, Direction.W);

    private static final int NINE = 9;

    private static final int ZERO = 0;

    // ASCII character to digit offset
    private static final int ASCII_OFFSET = 48;

    /**
     * @param startEdge
     *            the edge from which the search started
     * @param maximumAllowedDifferences
     *            the number of Levenshtein edits allowed
     * @return edges that are in the search area and whose names are at most the desired Levenshtein
     *         distance from the start edge's
     */
    static Predicate<Edge> getCandidateEdges(final Edge startEdge,
            final int maximumAllowedDifferences)
    {
        // evaluate name tag of startEdge vs incoming Edge
        return incomingEdge ->
        {
            final int similarityIndex = similarityIndex(incomingEdge, startEdge);
            return similarityIndex <= maximumAllowedDifferences && similarityIndex > 0;
        };
    }

    /**
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
                        ? incomingEdge.connectedEdges().stream().filter(Edge::isMasterEdge)
                        : Stream.empty();
    }

    /**
     * @param evaluateEdgeCharacter
     *            the character being checked for equality with the lambda parameter
     * @return true if the incoming character is the same as the parameter character
     */
    private static Predicate<String> equalCharacter(final char evaluateEdgeCharacter)
    {
        return directionCharacter -> evaluateEdgeCharacter == getCharacter(directionCharacter);
    }

    /**
     * @param oneCharacterString
     *            the string to be converted to a character
     * @return a char representation of the parameter String
     */
    private static char getCharacter(final String oneCharacterString)
    {
        return oneCharacterString.charAt(0);
    }

    /**
     * Compute the Levenshtein distance between two road names. Code used to calculate Levenshtein
     * distance is adapted from https://www.baeldung.com/java-levenshtein-distance
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

        // Roads differ by one directional character (N,S,E, or W) in their name strings
        boolean possibleDirectionalDifference = false;

        // Roads differ by one number character (1-9) in their name strings
        boolean singleNumericalDifference = false;

        for (int i = 0; i <= incomingEdgeName.length(); i++)
        {
            for (int j = 0; j <= startingEdgeName.length(); j++)
            {
                // Handles one directional character differences between roads. Meant to capture
                // differences in directionality; e.g. in Pie St. N vs. Pie St. S, neither should be
                // flagged as being inconsistent with one another.
                if (!possibleDirectionalDifference && i == j && i < incomingEdgeName.length()
                        && j < startingEdgeName.length())
                {
                    possibleDirectionalDifference = hasDirectionalCharacterDifference(
                            incomingEdgeName.charAt(i), startingEdgeName.charAt(j));
                }

                // Handles one number character differences between roads. Records if the
                // startingEdgeName and incomingEdgeName differ by one number in their name strings.
                if (!singleNumericalDifference && i == j && i < incomingEdgeName.length()
                        && j < startingEdgeName.length())
                {
                    singleNumericalDifference = hasNumericalCharacterDifference(
                            incomingEdgeName.charAt(i), startingEdgeName.charAt(j));
                }

                if (i == 0)
                {
                    results[i][j] = j;
                }

                else if (j == 0)
                {
                    results[i][j] = i;
                }

                else
                {
                    results[i][j] = min(
                            results[i - 1][j - 1] + costOfSubstitution(
                                    incomingEdgeName.charAt(i - 1), startingEdgeName.charAt(j - 1)),
                            results[i - 1][j] + 1, results[i][j - 1] + 1);
                }
            }
        }
        // If there's only a single character difference and that character is a directional OR
        // numerical character, we consider both roads to be different and so we don't flag them.
        // Else we return the Levenshtein distance as usual.
        return (possibleDirectionalDifference || singleNumericalDifference)
                && results[incomingEdgeName.length()][startingEdgeName.length()] == 1 ? -1
                        : results[incomingEdgeName.length()][startingEdgeName.length()];
    }

    /**
     * @param incomingEdgeCharacter
     *            the incoming Edge's character
     * @param startingEdgeCharacter
     *            the starting Edge's character
     * @return true if the parameter characters are both directional (members of Direction enum) AND
     *         they are different from one another
     */
    private static boolean hasDirectionalCharacterDifference(final char incomingEdgeCharacter,
            final char startingEdgeCharacter)
    {
        return DIRECTIONS.stream().map(Enum::toString).filter(
                equalCharacter(incomingEdgeCharacter).or(equalCharacter(startingEdgeCharacter)))
                .count() >= 2;
    }

    /**
     * @param incomingEdgeCharacter
     *            the incoming Edge's character
     * @param startingEdgeCharacter
     *            the starting Edge's character
     * @return true if both parameter characters are different numbers in [0,9], false otherwise
     */
    private static boolean hasNumericalCharacterDifference(final char incomingEdgeCharacter,
            final char startingEdgeCharacter)
    {
        return Character.isDigit(incomingEdgeCharacter) && Character.isDigit(startingEdgeCharacter)
                && IntStream.range(ZERO, NINE)
                        .filter(number -> number == (incomingEdgeCharacter - ASCII_OFFSET)
                                || number == (startingEdgeCharacter - ASCII_OFFSET))
                        .count() >= 2;
    }

    /**
     * @param numbers
     *            the numbers on which to operate
     * @return the minimum of those numbers
     */
    private static int min(final int... numbers)
    {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    /**
     * Wrapper for getLevenshteinDistance(). Handles cases where the incoming Edge doesn't have a
     * name and where the incoming edge has the same name as the starting Edge before computing the
     * Levenshtein distance.
     *
     * @param incomingEdge
     *            the next edge in the search area
     * @param startEdge
     *            the edge from which the search started
     * @return the Levenshtein distance between two edge's names, or -1 if the incoming edge doesn't
     *         have a name, or 0 if the names are the same
     */
    @SuppressWarnings("squid:S3655")
    private static int similarityIndex(final Edge incomingEdge, final Edge startEdge)
    {
        if (!incomingEdge.getName().isPresent())
        {
            // Always skipped
            return -1;
        }
        // NOSONAR because isPresent() was checked in the calling class
        final String startEdgeName = startEdge.getName().get();
        final String incomingEdgeName = incomingEdge.getName().get();
        if (startEdgeName.equals(incomingEdgeName))
        {
            return 0;
        }
        return getLevenshteinDistance(incomingEdgeName, startEdgeName);
    }

    /**
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
