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
    public void configurableDoesNotFlagLowValence()
    {
        this.verifier.actual(this.setup.getNotEnoughValence(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidMiniRoundaboutCheck\":{\"minimumValence\":1}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void configurableFlagsHighValence()
    {
        this.verifier.actual(this.setup.getValidRoundabout(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidMiniRoundaboutCheck.minimumValence\":10}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 6, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void regularIntersectionHighValence()
    {
        this.verifier.actual(this.setup.getValidRoundabout(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void regularIntersectionLowValence()
    {
        this.verifier.actual(this.setup.getNotEnoughValence(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 4, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void twoOneWayEdges()
    {
        this.verifier.actual(this.setup.getNoTurns(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 2, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void twoWayDeadEnd()
    {
        this.verifier.actual(this.setup.getTurningCircle(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 2, 1));
        this.verifier.verify(this::verifyTwoEdgesFlag);
    }

    @Test
    public void pedestrianHighway()
    {
        this.verifier.actual(this.setup.getPedestrianRoundabout(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 1, 1));
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test
    public void noEdgesNoFlags()
    {
        this.verifier.actual(this.setup.getNoRoads(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void turningCircleWithDirectionFlagged()
    {
        this.verifier.actual(this.setup.getTurningCircleWithDirection(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag -> this.verifyNumberNodesAndEdges(flag, 2, 1));
        this.verifier.verify(this::verifyTwoEdgesFlag);
    }

    @Test
    public void lowValenceWithDirectionNotFlagged()
    {
        this.verifier.actual(this.setup.getNoTurnsWithDirection(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Asserts that the flag contains the number of edges and nodes that are expected.
     *
     * @param flag A CheckFlag object.
     * @param expectedEdges The number of edges that are expected to be in this CheckFlag.
     * @param expectedNodes The number of nodes that are expected to be in this CheckFlag.
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
     * number of car-navigable edges.
     *
     * @param flag The flag to check.
     */
    private void verifyMultipleEdgesFlag(final CheckFlag flag)
    {
        Assert.assertTrue(flag.getInstructions()
                .contains("connecting car-navigable edges. Consider changing this."));
    }

    /**
     * Asserts that a flag contains an instruction suggesting a conversion from highway=MINI_ROUNDABOUT
     * to highway=TURNING_LOOP or highway=TURNING_CIRCLE.
     *
     * @param flag The flag to check.
     */
    private void verifyTwoEdgesFlag(final CheckFlag flag)
    {
        Assert.assertTrue(flag.getInstructions().contains(
                "has 2 connecting car-navigable edges. Consider changing this to highway=TURNING_LOOP or "
                        + "highway=TURNING_CIRCLE."));
    }

}
