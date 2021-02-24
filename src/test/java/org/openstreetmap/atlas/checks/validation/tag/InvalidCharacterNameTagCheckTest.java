package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit test for {@link InvalidCharacterNameTagCheck}
 *
 * @author sayas01
 */
public class InvalidCharacterNameTagCheckTest
{
    @Rule
    public InvalidCharacterNameTagCheckTestRule setup = new InvalidCharacterNameTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final InvalidCharacterNameTagCheck check = new InvalidCharacterNameTagCheck(
            ConfigurationResolver.resourceConfiguration("InvalidCharacterNameTagCheckTest.json",
                    this.getClass()));

    @Test
    public void testDoubleQuotesInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getDoubleQuotesInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testDoubleQuotesInNameTag()
    {
        this.verifier.actual(this.setup.getDoubleQuotesInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidCharacterInArea()
    {
        this.verifier.actual(this.setup.getInvalidCharacterInAreaAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidCharacterInRelation()
    {
        this.verifier.actual(this.setup.getInvalidCharacterInRelationAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testNonWaterFeatures()
    {
        this.verifier.actual(this.setup.getNonWaterFeatures(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testNumbersInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getNumbersInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testNumbersInNameTag()
    {
        this.verifier.actual(this.setup.getNumbersInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSmartQuotesInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getSmartQuotesInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSmartQuotesInNameTag()
    {
        this.verifier.actual(this.setup.getSmartQuotesInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSpecialCharactersInLocalizedNameTag()
    {
        this.verifier.actual(this.setup.getSpecialCharactersInLocalizedNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSpecialCharactersInNameTag()
    {
        this.verifier.actual(this.setup.getSpecialCharactersInNameTagAtlas(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }
}
