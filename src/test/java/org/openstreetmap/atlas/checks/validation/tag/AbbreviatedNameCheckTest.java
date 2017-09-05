package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link AbbreviatedNameCheck}.
 *
 * @author mkalender
 */
public class AbbreviatedNameCheckTest
{
    @Rule
    public AbbreviatedNameCheckTestRule setup = new AbbreviatedNameCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testAbbreviationsWithAllAbbreviationsConfig()
    {
        this.verifier.actual(this.setup.atlasWithAbbreviations(),
                new AbbreviatedNameCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"AbbreviatedNameCheck\":{\"locale\": \"en\", \"abbreviations\": [\"ave\", \"St\", \"Dr\", \"NE\", \"Cir\", \"Pl\", \"Apt\"]}}")));
        this.verifier.verifyNotEmpty();

        // one node and six edge should be flagged
        this.verifier.verifyExpectedSize(7);
    }

    @Test
    public void testAbbreviationsWithEmptyConfig()
    {
        this.verifier.actual(this.setup.atlasWithAbbreviations(),
                new AbbreviatedNameCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"AbbreviatedNameCheck\":{\"locale\": \"en\", \"abbreviations\": []}}")));

        // no abbreviations provided through config, so we shouldn't flag
        this.verifier.verifyEmpty();
    }

    @Test
    public void testAbbreviationsWithSomeAbbreviationsConfig()
    {
        this.verifier.actual(this.setup.atlasWithAbbreviations(),
                new AbbreviatedNameCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"AbbreviatedNameCheck\":{\"locale\": \"en\", \"abbreviations\": [\"ave\", \"St\", \"Dr\", \"NE\", \"Cir\", \"Pl\"]}}")));
        this.verifier.verifyNotEmpty();

        // Apt is not in the list, so six edge should be flagged
        this.verifier.verifyExpectedSize(6);
    }

    @Test
    public void testNoAbbreviationsWithAllAbbreviationsConfig()
    {
        this.verifier.actual(this.setup.atlasWithoutAbbreviations(),
                new AbbreviatedNameCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"AbbreviatedNameCheck\":{\"locale\": \"en\", \"abbreviations\": [\"ave\", \"St\", \"Dr\", \"NE\", \"Cir\", \"Pl\", \"Apt\"]}}")));

        // data doesn't have any abbreviations
        this.verifier.verifyEmpty();
    }
}
