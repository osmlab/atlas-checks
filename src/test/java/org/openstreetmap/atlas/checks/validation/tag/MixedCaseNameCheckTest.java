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
            "{\"MixedCaseNameCheck\":{\"check_name.countries\":[\"USA\",\"GRC\"],\"name.language.keys\":[\"name:en\",\"name:el\"],\"lower_case\":{\"prepositions\":[\"and\", \"to\", \"of\"],\"articles\":[\"a\", \"an\", \"the\"]},\"words.split.characters\":\" -/(&@\",\"name_affixes\":[\"Mc\", \"Mac\", \"Mck\",\"Mhic\", \"Mic\"]}}");

    @Test
    public void invalidNamePoint()
    {
        this.verifier.actual(this.setup.invalidNamePoint(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameNode()
    {
        this.verifier.actual(this.setup.invalidNameNode(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameLine()
    {
        this.verifier.actual(this.setup.invalidNameLine(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameArea()
    {
        this.verifier.actual(this.setup.invalidNameArea(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNameEdge()
    {
        this.verifier.actual(this.setup.invalidNameEdge(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointOneWord()
    {
        this.verifier.actual(this.setup.invalidNamePointOneWord(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNamePointHyphen()
    {
        this.verifier.actual(this.setup.validNamePointHyphen(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointNumber()
    {
        this.verifier.actual(this.setup.validNamePointNumber(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidNamePointHyphen()
    {
        this.verifier.actual(this.setup.invalidNamePointHyphen(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNamePointAffix()
    {
        this.verifier.actual(this.setup.validNamePointAffix(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidNamePointAffix()
    {
        this.verifier.actual(this.setup.invalidNamePointAffix(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNamePointApostrophe()
    {
        this.verifier.actual(this.setup.validNamePointApostrophe(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointApostropheLower()
    {
        this.verifier.actual(this.setup.validNamePointApostropheLower(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointApostropheAllCaps()
    {
        this.verifier.actual(this.setup.validNamePointApostropheAllCaps(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidNamePointApostrophe()
    {
        this.verifier.actual(this.setup.invalidNamePointApostrophe(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNamePointAllCaps()
    {
        this.verifier.actual(this.setup.validNamePointAllCaps(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointNoCaps()
    {
        this.verifier.actual(this.setup.validNamePointNoCaps(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointLowerCasePreposition()
    {
        this.verifier.actual(this.setup.validNamePointLowerCasePreposition(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointLowerCaseArticle()
    {
        this.verifier.actual(this.setup.validNamePointLowerCaseArticle(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validNamePointLowerCaseArticleStart()
    {
        this.verifier.actual(this.setup.validNamePointLowerCaseArticleStart(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidNamePointLowerCaseArticleStart()
    {
        this.verifier.actual(this.setup.invalidNamePointLowerCaseArticleStart(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointEn()
    {
        this.verifier.actual(this.setup.invalidNamePointEn(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidNamePointGreek()
    {
        this.verifier.actual(this.setup.invalidNamePointGreek(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNamePointGreek()
    {
        this.verifier.actual(this.setup.validNamePointGreek(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidNamePointGreekEl()
    {
        this.verifier.actual(this.setup.invalidNamePointGreekEl(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNamePointGreekEl()
    {
        this.verifier.actual(this.setup.validNamePointGreekEl(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidNamePointChn()
    {
        this.verifier.actual(this.setup.invalidNamePointChn(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validNamePointChn()
    {
        this.verifier.actual(this.setup.validNamePointChn(),
                new MixedCaseNameCheck(inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
