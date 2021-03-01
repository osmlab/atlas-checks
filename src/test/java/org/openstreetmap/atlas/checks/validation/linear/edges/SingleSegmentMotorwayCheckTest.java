package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.tags.HighwayTag;

/**
 * Unit tests for {@link SingleSegmentMotorwayCheck}.
 *
 * @author bbreithaupt
 */
public class SingleSegmentMotorwayCheckTest
{
    @Rule
    public SingleSegmentMotorwayCheckTestRule setup = new SingleSegmentMotorwayCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidMotorwaySegmentOneConnectionRoundaboutTest()
    {
        this.verifier.actual(this.setup.invalidMotorwaySegmentOneConnectionRoundaboutAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags ->
        {
            Assert.assertEquals(1, flags.size());
            Assert.assertEquals(1, flags.get(0).getFixSuggestions().size());
            flags.get(0).getFixSuggestions()
                    .forEach(fixSuggestion -> Assert
                            .assertTrue(fixSuggestion.getTag(HighwayTag.KEY).isPresent()
                                    && fixSuggestion.getTag(HighwayTag.KEY).get()
                                            .equals(HighwayTag.PRIMARY.name().toLowerCase())));
        });
    }

    @Test
    public void invalidMotorwaySegmentOneConnectionTest()
    {
        this.verifier.actual(this.setup.invalidMotorwaySegmentOneConnectionAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags ->
        {
            Assert.assertEquals(1, flags.size());
            Assert.assertEquals(1, flags.get(0).getFixSuggestions().size());
            flags.get(0).getFixSuggestions()
                    .forEach(fixSuggestion -> Assert
                            .assertTrue(fixSuggestion.getTag(HighwayTag.KEY).isPresent()
                                    && fixSuggestion.getTag(HighwayTag.KEY).get()
                                            .equals(HighwayTag.PRIMARY.name().toLowerCase())));
        });

    }

    @Test
    public void invalidPrimaryMotorwayPrimarySegmentTest()
    {
        this.verifier.actual(this.setup.invalidPrimaryMotorwayPrimarySegmentAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags ->
        {
            Assert.assertEquals(1, flags.size());
            Assert.assertEquals(1, flags.get(0).getFixSuggestions().size());
            flags.get(0).getFixSuggestions()
                    .forEach(fixSuggestion -> Assert
                            .assertTrue(fixSuggestion.getTag(HighwayTag.KEY).isPresent()
                                    && fixSuggestion.getTag(HighwayTag.KEY).get()
                                            .equals(HighwayTag.PRIMARY.name().toLowerCase())));
        });
    }

    @Test
    public void noFixSuggestionTest()
    {
        this.verifier.actual(this.setup.invalidNoSuggestionMotorwayAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags ->
        {
            Assert.assertEquals(1, flags.size());
            Assert.assertEquals(0, flags.get(0).getFixSuggestions().size());
        });
    }

    @Test
    public void validMotorwaySegmentOneConnectionTest()
    {
        this.verifier.actual(this.setup.validMotorwaySegmentOneConnectionAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validMotorwaySegmentsTest()
    {
        this.verifier.actual(this.setup.validMotorwaySegmentsAtlas(),
                new SingleSegmentMotorwayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
