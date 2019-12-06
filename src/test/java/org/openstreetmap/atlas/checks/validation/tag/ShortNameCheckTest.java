package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author cuthbertm
 */
public class ShortNameCheckTest
{
    @Rule
    public ShortNameCheckTestCaseRule setup = new ShortNameCheckTestCaseRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testBadAtlas()
    {
        this.verifier.actual(this.setup.bad(),
                new ShortNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testGoodAtlas()
    {
        this.verifier.actual(this.setup.good(),
                new ShortNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortNameBadAtlasCheck()
    {
        this.verifier.actual(this.setup.shortNameBadAtlas(),
                new ShortNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testShortNameCheckInlineConfig()
    {
        this.verifier.actual(this.setup.shortNameGoodAtlas(),
                new ShortNameCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"ShortNameCheck\":{\"non.latin.countries\":[\"IRN\"]}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortNameGoodAtlasCheck()
    {
        this.verifier.actual(this.setup.shortNameGoodAtlas(),
                new ShortNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
