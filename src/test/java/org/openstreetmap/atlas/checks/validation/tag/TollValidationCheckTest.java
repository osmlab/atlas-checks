package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link TollValidationCheck}
 * 
 * @author v-garei
 */
public class TollValidationCheckTest
{

    @Rule
    public TollValidationCheckTestRule setup = new TollValidationCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final TollValidationCheck check = new TollValidationCheck(
            ConfigurationResolver.inlineConfiguration("{\"TollValidationCheck\": " + "{"
                    + "\"minimumHighwayType\": \"tertiary\","
                    + "\"maxAngleDiffForContiguousWays\": 40.0," + "\"minInAndOutEdges\": 1.0,"
                    + "\"maxIterationForNearbySearch\": 15.0}}"));

    private static void verifyFixSuggestions(final CheckFlag flag, final int count)
    {
        Assert.assertEquals(count, flag.getFixSuggestions().size());
    }

    @Test
    public void escapableWayNeedsTollTagRemoved()
    {
        System.out.println("printing");
        this.verifier.actual(this.setup.escapableWayNeedsTollTagRemoved(), this.check);
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void inconsistentTollTags()
    {
        this.verifier.actual(this.setup.inconsistentTollTags(), this.check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyFixSuggestions(flag, 1));
    }

    @Test
    public void intersectingTollFeatureWithoutTag()
    {
        this.verifier.actual(this.setup.intersectingTollFeatureWithoutTag(), this.check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyFixSuggestions(flag, 1));
    }
}
