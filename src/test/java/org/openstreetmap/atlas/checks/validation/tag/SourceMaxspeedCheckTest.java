package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * @author mm-ciub
 */
public class SourceMaxspeedCheckTest
{

    @Rule
    public SourceMaxspeedCheckTestRule setup = new SourceMaxspeedCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"SourceMaxspeedCheck\":{\"countries.denylist\":[\"UK\"], \"context.values\":[\"urban\"], \"values\":[\"implied\"], \"country.exceptions\":[\"RO-TST\"]}}");

    @Test
    public void countryExceptionConfigTest()
    {
        this.verifier.actual(this.setup.countryExceptionConfigAtlas(),
                new SourceMaxspeedCheck(this.inlineConfiguration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void edgeWalkerTest()
    {
        this.verifier.actual(this.setup.edgeWalkerAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void exceptionCountryTest()
    {
        this.verifier.actual(this.setup.exceptionCountryAtlas(),
                new SourceMaxspeedCheck(this.inlineConfiguration));
        this.verifier.verifyEmpty();
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
    public void noSourceMaxspeedTagTest()
    {
        this.verifier.actual(this.setup.noSourceMaxspeedAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validContextConfigTest()
    {
        this.verifier.actual(this.setup.validContextAtlas(),
                new SourceMaxspeedCheck(this.inlineConfiguration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validElementTest()
    {
        this.verifier.actual(this.setup.validAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validNumberContextTest()
    {
        this.verifier.actual(this.setup.numberContextAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validValueConfigTest()
    {
        this.verifier.actual(this.setup.validValueImpliedAtlas(),
                new SourceMaxspeedCheck(this.inlineConfiguration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validZone()
    {
        this.verifier.actual(this.setup.zoneAtlas(),
                new SourceMaxspeedCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
