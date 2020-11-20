package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author mm-ciub
 */
public class SourceMaxspeedCheckTest
{

    @Rule
    public SourceMaxspeedCheckTestRule setup = new SourceMaxspeedCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void exceptionCountry()
    {
        this.verifier.actual(this.setup.exceptionCountry(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidContextTest()
    {
        this.verifier.actual(this.setup.invalidContextAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void invalidCountryTest()
    {
        this.verifier.actual(this.setup.invalidCountryCodeAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidValueTest()
    {
        this.verifier.actual(this.setup.invalidValueAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validElementTest()
    {
        this.verifier.actual(this.setup.validAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validZone()
    {
        this.verifier.actual(this.setup.zoneAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
