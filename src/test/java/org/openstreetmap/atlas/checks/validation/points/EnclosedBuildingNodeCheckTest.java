package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link EnclosedBuildingNodeCheck}
 *
 * @author vladlemberg
 */
public class EnclosedBuildingNodeCheckTest
{
    @Rule
    public EnclosedBuildingNodeCheckTestRule setup = new EnclosedBuildingNodeCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void enclosedBuildingNode()
    {
        this.verifier.actual(this.setup.buildingNodeAtlas(),
                new EnclosedBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void enclosedBuildingNodeAddress()
    {
        this.verifier.actual(this.setup.buildingNodeAddressAtlas(),
                new EnclosedBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void enclosedBuildingNodeArea()
    {
        this.verifier.actual(this.setup.buildingNodeAreaAtlas(),
                new EnclosedBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
