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

    /**
     * Test 1: Building Node enclosed in building area
     */
    @Test
    public void enclosedBuildingNode()
    {
        this.verifier.actual(this.setup.buildingNodeAtlas(),
                new EnclosedBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    /**
     * Test 2: Building Node enclosed in building area with address
     */
    @Test
    public void enclosedBuildingNodeAddress()
    {
        this.verifier.actual(this.setup.buildingNodeAddressAtlas(),
                new EnclosedBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    /**
     * Test 3: Building Node enclosed in area
     */
    @Test
    public void enclosedBuildingNodeArea()
    {
        this.verifier.actual(this.setup.buildingNodeAreaAtlas(),
                new EnclosedBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    /**
     * Test 4: Building Node is not geometrically enclosed in any area
     */
    @Test
    public void loneBuildingNodeArea()
    {
        this.verifier.actual(this.setup.loneBuildingNodeAtlas(),
                new EnclosedBuildingNodeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
