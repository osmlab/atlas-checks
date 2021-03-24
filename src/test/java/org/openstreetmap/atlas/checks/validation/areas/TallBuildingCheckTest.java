package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link TallBuildingCheck}
 *
 * @author v-garei
 */
public class TallBuildingCheckTest
{

    @Rule
    public TallBuildingCheckTestRule setup = new TallBuildingCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final ConcerningAngleBuildingCheck check = new ConcerningAngleBuildingCheck(
            ConfigurationResolver.inlineConfiguration("{\"bufferDistanceMeters\": 1600.0, "
                    + "\"minDatasetSizeForStatsComputation\": 50.0, "
                    + "\"maxLevelTagValue\": 100.0, " + "\"outlierMultiplier\": 18.0, "
                    + "\"invalidHeightCharacters\":[\"~\",\"`\",\"!\",\"@\",\"#\",\"$\",\"%\",\"^\",\"&\",\"*\",\"(\",\")\",\"-\",\"_\",\"+\",\"=\",\"[\",\"[\",\"}\",\"]\",\"|\",\"\\\\\",\":\",\";\",\"<\",\",\",\">\",\"?\",\"/\"], "
                    + "\"magicNumbers\": {" + "\"three\": 3.0, " + "\"four\": 4.0, "
                    + "\"oneQuarter\": 0.25, " + "\"threeQuarters\": 0.75}}"));

    @Test
    public void heightTagDoesNotContainNumericalCharacter()
    {
        this.verifier.actual(this.setup.heightTagDoesNotContainNumericalCharacter(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void heightTagNeedsSpace()
    {
        this.verifier.actual(this.setup.heightTagNeedsSpace(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidHeightTagCharacter()
    {
        this.verifier.actual(this.setup.invalidHeightTagCharacter(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidLevelsTag()
    {
        this.verifier.actual(this.setup.invalidLevelsTag(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void levelsTagOver100()
    {
        this.verifier.actual(this.setup.levelsTagOver100(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

}
