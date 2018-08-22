package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

public class InvalidMiniRoundaboutCheckTest
{
    @Rule public InvalidMiniRoundaboutCheckTestRule setup = new InvalidMiniRoundaboutCheckTestRule();

    @Rule public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test public void configurableDoesNotFlagLowValence()
    {
        this.verifier.actual(this.setup.getNotEnoughValence(), new InvalidMiniRoundaboutCheck(
                ConfigurationResolver
                        .inlineConfiguration("{\"InvalidMiniRoundaboutCheck.minimumValence\":1}")));
        this.verifier.verifyEmpty();
    }

    @Test public void configurableFlagsHighValence()
    {
        this.verifier.actual(this.setup.getValidRoundabout(), new InvalidMiniRoundaboutCheck(
                ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidMiniRoundaboutCheck.minimumValence\":10}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test public void regularIntersectionHighValence()
    {
        this.verifier.actual(this.setup.getValidRoundabout(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test public void regularIntersectionLowValence()
    {
        this.verifier.actual(this.setup.getNotEnoughValence(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test public void twoOneWayEdges()
    {
        this.verifier.actual(this.setup.getNoTurns(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(this::verifyMultipleEdgesFlag);
    }

    @Test public void twoWayDeadEnd()
    {
        this.verifier.actual(this.setup.getTurningCircle(),
                new InvalidMiniRoundaboutCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(this::verifyTwoEdgesFlag);
    }

    private void verifyMultipleEdgesFlag(CheckFlag flag)
    {
        flag.getFlaggedObjects().forEach(obj -> Assert.assertTrue(
                obj.getProperties().get(this.setup.ITEM_TYPE_TAG).equals(this.setup.NODE_TAG)));
        Assert.assertTrue(
                flag.getInstructions().contains("connecting edges. Consider changing this."));
    }

    private void verifyTwoEdgesFlag(CheckFlag flag)
    {
        flag.getFlaggedObjects().forEach(obj -> Assert.assertTrue(
                obj.getProperties().get(this.setup.ITEM_TYPE_TAG).equals(this.setup.NODE_TAG)));
        Assert.assertTrue(flag.getInstructions().contains(
                "has 2 connecting edges. Consider changing this to highway=TURNING_LOOP or "
                        + "highway=TURNING_CIRCLE."));
    }

}
