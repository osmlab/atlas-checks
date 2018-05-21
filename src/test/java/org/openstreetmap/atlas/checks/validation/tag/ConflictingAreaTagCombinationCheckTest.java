package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * ConflictingAreaTagCombination unit tests
 *
 * @author danielbaah
 */
public class ConflictingAreaTagCombinationCheckTest
{
    @Rule
    public ConflictingAreaTagCombinationCheckTestRule setup = new ConflictingAreaTagCombinationCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void buildingHighwayTagAtlasTest()
    {
        this.verifier.actual(this.setup.getBuildingHighwayTagAtlas(),
                new ConflictingAreaTagCombination(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void buildingNaturalTagAtlasTest()
    {
        this.verifier.actual(this.setup.getBuildingNaturalTagAtlas(),
                new ConflictingAreaTagCombination(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void naturalHighwayTagAtlasTest()
    {
        this.verifier.actual(this.setup.getNaturalHighwayTagAtlas(),
                new ConflictingAreaTagCombination(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void naturalLeisureTagAtlasTest()
    {
        this.verifier.actual(this.setup.getNaturalLeisureTagAtlas(),
                new ConflictingAreaTagCombination(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void buildingLandUseTagAtlasTest()
    {
        this.verifier.actual(this.setup.getBuildingLandUseTagAtlas(),
                new ConflictingAreaTagCombination(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void naturalManMadeTagAtlasTest()
    {
        this.verifier.actual(this.setup.getNaturalManMadeTagAtlas(),
                new ConflictingAreaTagCombination(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void waterLandUseTagAtlasTest()
    {
        this.verifier.actual(this.setup.getWaterLandUseTagAtlas(),
                new ConflictingAreaTagCombination(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }
}
