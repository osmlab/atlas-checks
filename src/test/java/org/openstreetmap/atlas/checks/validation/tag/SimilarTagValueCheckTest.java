package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link SimilarTagValueCheck}
 *
 * @author v-brjor
 */
public class SimilarTagValueCheckTest
{
    @Rule
    public SimilarTagValueCheckTestRule setup = new SimilarTagValueCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final SimilarTagValueCheck check = new SimilarTagValueCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"SimilarTagValueCheck\":{" + "\"similarity.threshold\":{\"min\":" + 0.0 + ","
                            + "\"max\":" + 1.0 + "},\"value.length.min\":" + 4.0 + "}}"));

    @Test
    public void testDuplicateCommonSimilars()
    {
        this.verifier.actual(this.setup.getDuplicateCommonSimilarsTest(), this.check);
        this.verifier.verify(flag -> Assert.assertEquals(
                "1. The tag \"cuisine\" contains duplicate values: [(cake,cake,0)]",
                flag.getInstructions()));
    }

    @Test
    public void testHasDuplicateTagValue()
    {
        this.verifier.actual(this.setup.getHasDuplicateTagTest(), this.check);
        this.verifier.verify(flag -> Assert.assertEquals(
                "1. The tag \"hasDupe\" contains duplicate values: [(dupe,dupe,0)]",
                flag.getInstructions()));
    }

    @Test
    public void testHasSimilarTagValue()
    {
        this.verifier.actual(this.setup.getHasSimilarTagTest(), this.check);
        this.verifier.verify(flag -> Assert.assertEquals(
                "1. The tag \"hasSimilar\" contains similar values: [(similar,similer,1)]",
                flag.getInstructions()));
    }

    @Test
    public void testCommonSimilars()
    {
        this.verifier.actual(this.setup.getIgnoreCommonSimilarsTest(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testIgnoreTag()
    {
        this.verifier.actual(this.setup.getIgnoreTagTest(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testIgnoreTagSubclass()
    {
        this.verifier.actual(this.setup.getIgnoreTagSubclassTest(), this.check);
        this.verifier.verifyEmpty();
    }
}
