package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link MixedCaseNameCheck}
 *
 * @author bbreithaupt
 */
public class MixedCaseNameCheckTest
{
    @Rule
    public MixedCaseNameCheckTestRule setup = new MixedCaseNameCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"MixedCaseNameCheck\":{\"check_name.countries\":[\"USA\",\"GRC\"],\"name.language.keys\":[\"name:en\",\"name:el\"],\"lower_case\":{\"prepositions\":[\"and\", \"to\", \"of\"],\"articles\":[\"a\", \"an\", \"the\"]},\"words.split.characters\":\" -/&@\",\"name_affixes\":[\"Mc\", \"Mac\", \"Mck\",\"Mhic\", \"Mic\"],\"units.mixed_case\":[\"kV\"]}}");

    @Test
    public void invalidNameAreaTest()
    {
        this.verifier.actual(this.setup.invalidNameAreaAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameEdgeTest()
    {
        this.verifier.actual(this.setup.invalidNameEdgeAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameICase()
    {
        this.verifier.actual(this.setup.invalidNameICase(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameICaseMiddle()
    {
        this.verifier.actual(this.setup.invalidNameICaseMiddle(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameLineTest()
    {
        this.verifier.actual(this.setup.invalidNameLineAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameNodeTest()
    {
        this.verifier.actual(this.setup.invalidNameNodeAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointAffixTest()
    {
        this.verifier.actual(this.setup.invalidNamePointAffixAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointApostropheTest()
    {
        this.verifier.actual(this.setup.invalidNamePointApostropheAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointChnTest()
    {
        this.verifier.actual(this.setup.invalidNamePointChnAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointEnTest()
    {
        this.verifier.actual(this.setup.invalidNamePointEnAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointGreekElTest()
    {
        this.verifier.actual(this.setup.invalidNamePointGreekElAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointGreekTest()
    {
        this.verifier.actual(this.setup.invalidNamePointGreekAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointHyphenTest()
    {
        this.verifier.actual(this.setup.invalidNamePointHyphenAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointLowerCaseArticleStartTest()
    {
        this.verifier.actual(this.setup.invalidNamePointLowerCaseArticleStartAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointOneWordTest()
    {
        this.verifier.actual(this.setup.invalidNamePointOneWordAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointTest()
    {
        this.verifier.actual(this.setup.invalidNamePointAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNameICase()
    {
        // Example: iRobot
        this.verifier.actual(this.setup.validNameICase(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointAffixTest()
    {
        this.verifier.actual(this.setup.validNamePointAffixAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointAllCapsTest()
    {
        this.verifier.actual(this.setup.validNamePointAllCapsAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointApostropheAllCapsTest()
    {
        this.verifier.actual(this.setup.validNamePointApostropheAllCapsAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointApostropheLowerTest()
    {
        this.verifier.actual(this.setup.validNamePointApostropheLowerAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointApostropheTest()
    {
        this.verifier.actual(this.setup.validNamePointApostropheAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointCapsApostropheTest()
    {
        this.verifier.actual(this.setup.validNamePointCapsApostropheAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointCapsLowerApostropheTest()
    {
        this.verifier.actual(this.setup.validNamePointCapsLowerApostropheAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointChnTest()
    {
        this.verifier.actual(this.setup.validNamePointChnAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointDashAndDashTest()
    {
        // example: Rock-n-Roll
        this.verifier.actual(this.setup.validNamePointDashAndDash(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointGreekElTest()
    {
        this.verifier.actual(this.setup.validNamePointGreekElAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointGreekTest()
    {
        this.verifier.actual(this.setup.validNamePointGreekAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointHyphenTest()
    {
        this.verifier.actual(this.setup.validNamePointHyphenAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointLowerCaseArticleStartTest()
    {
        this.verifier.actual(this.setup.validNamePointLowerCaseArticleStartAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointLowerCaseArticleTest()
    {
        this.verifier.actual(this.setup.validNamePointLowerCaseArticleAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointLowerCasePrepositionTest()
    {
        this.verifier.actual(this.setup.validNamePointLowerCasePrepositionAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointMixedCaseUnitTest()
    {
        this.verifier.actual(this.setup.validNamePointMixedCaseUnitAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointNoCapsTest()
    {
        this.verifier.actual(this.setup.validNamePointNoCapsAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointNumberTest()
    {
        this.verifier.actual(this.setup.validNamePointNumberAtlas(),
                new MixedCaseNameCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
