package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
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
            ConfigurationResolver.inlineConfiguration(
                    "{\"TollValidationCheck\": " + "{\"minimumHighwayType\": \"tertiary\"" + ","
                            + "\"maxAngleDiffForContiguousWays\": 40.0}}"));

    @Test
    public void escapableWayNeedsTollTagRemoved()
    {
        this.verifier.actual(this.setup.escapableWayNeedsTollTagRemoved(), this.check);
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void inconsistentTollTags()
    {
        this.verifier.actual(this.setup.inconsistentTollTags(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void intersectingTollFeatureWithoutTag()
    {
        this.verifier.actual(this.setup.intersectingTollFeatureWithoutTag(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
