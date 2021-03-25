package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link LongNameCheck}.
 *
 * @author bbreithaupt
 */
public class LongNameCheckTest
{
    @Rule
    public LongNameCheckTestRule testAtlases = new LongNameCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidAltNameTest()
    {
        this.verifier.actual(this.testAtlases.invalidAltNameAtlas(),
                new LongNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidEdgeNamesTest()
    {
        this.verifier.actual(this.testAtlases.invalidEdgeNamesAtlas(),
                new LongNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void invalidNameEnTest()
    {
        this.verifier.actual(this.testAtlases.invalidNameEnAtlas(),
                new LongNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidNameTest()
    {
        this.verifier.actual(this.testAtlases.invalidNameAtlas(),
                new LongNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidRegionalNameTest()
    {
        this.verifier.actual(this.testAtlases.invalidRegionalLocalNameAtlas(),
                new LongNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void validNameConfigurationTest()
    {
        this.verifier.actual(this.testAtlases.invalidNameAtlas(),
                new LongNameCheck(ConfigurationResolver
                        .inlineConfiguration("{\"LongNameCheck\":{\"name.max\":100}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validNameTest()
    {
        this.verifier.actual(this.testAtlases.validNameAtlas(),
                new LongNameCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
