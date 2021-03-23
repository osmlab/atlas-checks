package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.tags.FerryTag;
import org.openstreetmap.atlas.tags.HighwayTag;

/**
 * {@link HighwayToFerryTagCheckTest} Unit Test
 *
 * @author sayas01
 */
public class HighwayToFerryTagCheckTest
{
    @Rule
    public HighwayToFerryTagCheckTestRule setup = new HighwayToFerryTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testAtlasWithDifferentFerryAndHighwayTags()
    {
        this.verifier.actual(this.setup.getDifferentFerryHighwayAtlas(),
                new HighwayToFerryTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(
                    "has a Ferry and a Highway tag for a ferry route. Please verify and update the Ferry tag with the Highway tag value and remove the highway tag."));
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertEquals(2, fixSuggestion.getTags().size());
            Assert.assertEquals(HighwayTag.TRUNK.toString().toLowerCase(),
                    fixSuggestion.getTags().get(FerryTag.KEY));
        });

    }

    @Test
    public void testAtlasWithNoFerryTag()
    {
        this.verifier.actual(this.setup.getHighwayAtlas(),
                new HighwayToFerryTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(4, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions()
                    .contains("has a Highway tag for a ferry route instead of a Ferry tag"));
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            final FeatureChange fixSuggestion = flag.getFixSuggestions().iterator().next();
            Assert.assertTrue(fixSuggestion.getTag(HighwayTag.KEY).isEmpty());
            Assert.assertTrue(fixSuggestion.getTag(FerryTag.KEY).isPresent());
        });
    }

    @Test
    public void testAtlasWithSameFerryAndHighwayTags()
    {
        this.verifier.actual(this.setup.getSameFerryHighwayAtlas(),
                new HighwayToFerryTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(
                    "has a Ferry and a Highway tag for a ferry route. Please verify and remove the highway tag."));
            Assert.assertFalse(flag.getFixSuggestions().isEmpty());
            Assert.assertTrue(
                    flag.getFixSuggestions().iterator().next().getTag(HighwayTag.KEY).isEmpty());
        });
    }

    @Test
    public void testAtlasWithValidFerryHighwayTags()
    {
        this.verifier.actual(this.setup.getValidAtlas(),
                new HighwayToFerryTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"HighwayToFerryTagCheck\":{\"highway.type.minimum\":\"path\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testMinimumHighwayConfiguration()
    {
        this.verifier.actual(this.setup.getMinimumHighwayAtlas(),
                new HighwayToFerryTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"HighwayToFerryTagCheck\":{\"highway.type.minimum\":\"service\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }
}
