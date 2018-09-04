package org.openstreetmap.atlas.checks.validation.points;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests the {@link InvalidMiniRoundaboutCheck} for each use case.
 *
 * @author nachtm
 */
public class InvalidMiniRoundaboutCheckTest
{
    @Rule
    public InvalidMiniRoundaboutCheckTestRule setup = new InvalidMiniRoundaboutCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testConfigurableDoesNotFlagLowValence()
    {
        this.verifier.actual(this.setup.getNotEnoughValenceAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidMiniRoundaboutCheck\":{\"valence.minimum\":1}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testConfigurableFlagsHighValence()
    {
        this.verifier.actual(this.setup.getValidRoundaboutAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidMiniRoundaboutCheck.valence.minimum\":10}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 6, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void testRegularIntersectionHighValence()
    {
        this.verifier.actual(this.setup.getValidRoundaboutAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testRegularIntersectionLowValence()
    {
        this.verifier.actual(this.setup.getNotEnoughValenceAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 4, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void testTwoOneWayEdges()
    {
        this.verifier.actual(this.setup.getNoTurnsAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 2, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void testTwoWayDeadEnd()
    {
        this.verifier.actual(this.setup.getTurningCircleAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 2, 1));
        this.verifier.verify(this::verifyTwoEdgesFlag);
    }

    @Test
    public void testPedestrianHighway()
    {
        this.verifier.actual(this.setup.getPedestrianRoundaboutAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 1, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void testNoEdgesNoFlags()
    {
        this.verifier.actual(this.setup.getNoRoadsAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTurningCircleWithDirectionFlagged()
    {
        this.verifier.actual(this.setup.getTurningCircleWithDirectionAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 2, 1));
        this.verifier.verify(this::verifyTwoEdgesFlag);
    }

    @Test
    public void testLowValenceWithDirectionNotFlagged()
    {
        this.verifier.actual(this.setup.getNoTurnsWithDirectionAtlas(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Asserts that the flag contains the number of Edges and Nodes that are expected.
     *
     * @param flag
     *            A CheckFlag object.
     * @param expectedEdges
     *            The number of Edges that are expected to be in this CheckFlag.
     * @param expectedNodes
     *            The number of Nodes that are expected to be in this CheckFlag.
     */
    private void verifyNumberNodesAndEdges(final CheckFlag flag, final long expectedEdges,
            final long expectedNodes)
    {
        final Map<String, Long> flagCounts = flag.getFlaggedObjects().stream()
                .collect(Collectors.groupingBy(
                        obj -> obj.getProperties().get(this.setup.ITEM_TYPE_TAG),
                        Collectors.counting()));
        Assert.assertEquals(expectedEdges,
                (long) flagCounts.getOrDefault(this.setup.EDGE_TAG, -1L));
        Assert.assertEquals(expectedNodes,
                (long) flagCounts.getOrDefault(this.setup.NODE_TAG, -1L));
    }

    /**
     * Asserts that a flag contains an instruction describing there being a suspiciously small
     * number of car-navigable Edges.
     *
     * @param flag
     *            The flag to check.
     */
    private void verifyMultipleEdgesFlag(final CheckFlag flag)
    {
        Assert.assertTrue(flag.getInstructions()
                .contains("connecting car-navigable Edges. Consider changing this."));
    }

    /**
     * Asserts that a flag contains an instruction suggesting a conversion from
     * highway=MINI_ROUNDABOUT to highway=TURNING_LOOP or highway=TURNING_CIRCLE.
     *
     * @param flag
     *            The flag to check.
     */
    private void verifyTwoEdgesFlag(final CheckFlag flag)
    {
        Assert.assertTrue(flag.getInstructions().contains(
                "has 2 connecting car-navigable Edges. Consider changing this to highway=TURNING_LOOP or "
                        + "highway=TURNING_CIRCLE."));
    }

}
