package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Predicate;
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

    private static final String ALPHANUMERIC_IDENTIFIER_STRING_REGEX = ".*[0-9]+.*";
    private static final String WHITESPACE_REGEX = "\\s+";

    /**
     * Evaluate the {@link org.openstreetmap.atlas.tags.names.NameTag}s of the startingEdge and an
     * incomingEdge to see if their spellings are inconsistent with one another.
     *
     * @param startEdge
     *            the edge from which the search started
     * @param maximumAllowedDifferences
     *            the number of Levenshtein edits allowed
     * @return true if incomingEdge's name is at most the desired Levenshtein distance from the
     *         start edge's name
     */
    static Predicate<Edge> isEdgeWithInconsistentSpelling(final Edge startEdge,
            final int maximumAllowedDifferences)
    {
        return incomingEdge ->
        {
            final int similarityIndex = similarityIndex(incomingEdge, startEdge);
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
                        ? incomingEdge.connectedEdges().stream().filter(Edge::isMasterEdge)
                        : Stream.empty();
    }

    /**
     * Check two characters to see if they're equal.
     *
     * @param evaluateEdgeCharacter
     *            the character being checked for equality with the lambda parameter
     * @return true if the incoming character is the same as the parameter character
     */
    private static Predicate<String> equalCharacter(final char evaluateEdgeCharacter)
    {
        return directionCharacter -> evaluateEdgeCharacter == getCharacter(directionCharacter);
    }

    /**
     * Get the character representation of a one-character String.
     *
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

        // Roads differ by one directional character (N,S,E, or W) in their name strings
        boolean possibleDirectionalDifference = false;

        final Stream<String> incomingEdgeNameAlphanumericIdentifierStrings = Arrays
                .stream(incomingEdgeName.split(WHITESPACE_REGEX))
                .filter(substring -> substring.matches(ALPHANUMERIC_IDENTIFIER_STRING_REGEX));
        final Stream<String> startingEdgeNameAlphanumericIdentifierStrings = Arrays
                .stream(startingEdgeName.split(WHITESPACE_REGEX))
                .filter(substring -> substring.matches(ALPHANUMERIC_IDENTIFIER_STRING_REGEX));

        // If the two street names have different alphanumeric identifier strings anywhere in their
        // names, they're classified as being from different roads.
        if (Stream.concat(incomingEdgeNameAlphanumericIdentifierStrings,
                startingEdgeNameAlphanumericIdentifierStrings).distinct().count() > 1)
        {
            return -1;
        }

        // We now know that the street names have the same numbers, or no numbers at all
        for (int incomingEdgeNameIndex = 0; incomingEdgeNameIndex <= incomingEdgeName
                .length(); incomingEdgeNameIndex++)
        {
            for (int startingEdgeNameIndex = 0; startingEdgeNameIndex <= startingEdgeName
                    .length(); startingEdgeNameIndex++)
            {
                // Handles one directional character differences between roads. Meant to capture
                // differences in directionality; e.g. in Pie St. N vs. Pie St. S, neither should be
                // flagged as being inconsistent with the other.
                if (!possibleDirectionalDifference && incomingEdgeNameIndex == startingEdgeNameIndex
                        && incomingEdgeNameIndex < incomingEdgeName.length()
                        && startingEdgeNameIndex < startingEdgeName.length())
                {
                    possibleDirectionalDifference = hasDirectionalCharacterDifference(
                            incomingEdgeName.charAt(incomingEdgeNameIndex),
                            startingEdgeName.charAt(startingEdgeNameIndex));
                }

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
                    results[incomingEdgeNameIndex][startingEdgeNameIndex] = min(
                            results[incomingEdgeNameIndex - 1][startingEdgeNameIndex - 1]
                                    + costOfSubstitution(
                                            incomingEdgeName.charAt(incomingEdgeNameIndex - 1),
                                            startingEdgeName.charAt(startingEdgeNameIndex - 1)),
                            results[incomingEdgeNameIndex - 1][startingEdgeNameIndex] + 1,
                            results[incomingEdgeNameIndex][startingEdgeNameIndex - 1] + 1);
                }
            }
        }

        // If there's only a single character difference and that character is a directional
        // character, we consider both roads to be different and so we don't flag them. Else we
        // return the Levenshtein distance as usual.
        return possibleDirectionalDifference
                && results[incomingEdgeName.length()][startingEdgeName.length()] == 1 ? -1
                        : results[incomingEdgeName.length()][startingEdgeName.length()];
    }

    /**
     * Check if the parameter characters are different directional characters.
     *
     * @param incomingEdgeCharacter
     *            the incoming Edge's character
     * @param startingEdgeCharacter
     *            the starting Edge's character
     * @return true if the parameter characters are both directional (members of Direction enum) AND
     *         they are different from one another, false otherwise
     */
    private static boolean hasDirectionalCharacterDifference(final char incomingEdgeCharacter,
            final char startingEdgeCharacter)
    {
        return DIRECTIONS.stream().map(Enum::toString).filter(
                equalCharacter(incomingEdgeCharacter).or(equalCharacter(startingEdgeCharacter)))
                .count() >= 2;
    }

    /**
     * Retrieve the minimum value out of parameter numbers.
     *
     * @param numbers
     *            the numbers on which to operate
     * @return the minimum of those numbers
     */
    private static int min(final int... numbers)
    {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    /**
     * Wrapper for getLevenshteinDistance(). Handles cases where the incomingEdge doesn't have a
     * name and where the incomingEdge has the same name as the startingEdge before computing the
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
