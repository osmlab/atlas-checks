package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link InvalidTagsCheck}.
 *
 * @author bbreithaupt
 */
public class InvalidTagsCheckTest
{
    @Rule
    public InvalidTagsCheckTestRule setup = new InvalidTagsCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void appendConfigFiltersTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": false,\"filters.classes.tags\":[[\"node\",\"crossing->traffic_signals&highway->!crossing\"],"
                                + "[\"point\",\"crossing->traffic_signals&highway->!crossing\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void configRegexInstruction()
    {
        this.verifier.actual(this.setup.getIllegalSourceLinkNode(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.regex\": ["
                                + "      [\"node\", [\"source\"],[\".*(?i)\\\\bgoogle\\\\b.*\", \".*(?i)\\\\bhere\\\\b(?=.*map|.com)\",\n"
                                + "        \".*(?i)\\\\bvworld\\\\b.*\", \".*(?i)\\\\bxdworld\\\\b.*\"], \"Illegal tag: {0}.\"]"
                                + "    ]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. OSM feature 1 has invalid tags.\n" + "2. Illegal tag: source.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void configTaggableInstruction()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[[\"edge\",\"route->ferry&highway->*\",\"The feature has tag issues.\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. OSM feature 5 has invalid tags.\n" + "2. The feature has tag issues.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void illegalSourceEdge()
    {
        this.verifier.actual(this.setup.getIllegalSourceLinkEdge(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.regex\": ["
                                + "      [\"edge\", [\"source\"],[\".*(?i)\\\\bgoogle\\\\b.*\", \".*(?i)\\\\bhere\\\\b(?=.*map|.com)\",\n"
                                + "        \".*(?i)\\\\bvworld\\\\b.*\", \".*(?i)\\\\bxdworld\\\\b.*\"]]"
                                + "    ]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void illegalSourceNode()
    {
        this.verifier.actual(this.setup.getIllegalSourceLinkNode(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.regex\": ["
                                + "      [\"node\", [\"source\"],[\".*(?i)\\\\bgoogle\\\\b.*\", \".*(?i)\\\\bhere\\\\b(?=.*map|.com)\",\n"
                                + "        \".*(?i)\\\\bvworld\\\\b.*\", \".*(?i)\\\\bxdworld\\\\b.*\"]]"
                                + "    ]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void invalidAreaRelationTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[[\"area\",\"boundary->protected_area&protect_class->!\"], [\"relation\",\"boundary->protected_area&protect_class->!\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void invalidEdgeLineTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[[\"edge\",\"route->ferry&highway->*\"], [\"line\",\"route->ferry&highway->*\"],[\"edge\",\"construction->*&highway->!construction\"],"
                                + "[\"line\",\"construction->*&highway->!construction\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void invalidEdgeTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[[\"edge\",\"route->ferry&highway->*\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidLineTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[[\"line\",\"construction->*&highway->!construction\"],[\"line\",\"water->*&natural->!water\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.globallyVerify(flags -> flags.forEach(flag -> Assert.assertEquals(
                "1. OSM feature 6 has invalid tags.\n"
                        + "2. Check the following tags for missing, conflicting, or incorrect values: [construction, highway]\n"
                        + "3. Check the following tags for missing, conflicting, or incorrect values: [natural, water]",
                flag.getInstructions())));
    }

    @Test
    public void invalidNodePointTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[[\"node\",\"crossing->traffic_signals&highway->!crossing\"],"
                                + "[\"point\",\"crossing->traffic_signals&highway->!crossing\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void invalidRoundaboutAreaTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource\": {\"override\": true,\"append\": false},\"filters.classes.tags\":[[\"edge\",\"highway->motorway,trunk,primary,secondary,tertiary,unclassified,residential,service,motorway_link,trunk_link,primary_link,secondary_link,tertiary_link,living_street,track&junction->roundabout&area->*\"]]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. OSM feature 5 has invalid tags.\n"
                        + "2. Check the following tags for missing, conflicting, or incorrect values: [junction, area, highway]",
                flags.get(0).getInstructions()));
    }

    @Test
    public void resourceRegexInstruction()
    {
        this.verifier.actual(this.setup.getIllegalSourceLinkNode(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": false}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. OSM feature 1 has invalid tags.\n"
                        + "2. The following element has an illegal source.",
                flags.get(0).getInstructions()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. OSM feature 2 has invalid tags.\n"
                        + "2. The following element has an illegal source.",
                flags.get(1).getInstructions()));
    }

    @Test
    public void resourceTaggableInstruction()
    {
        this.verifier.actual(this.setup.getInconsistentHighwayAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": false}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(
                "1. OSM feature 1001 has invalid tags.\n"
                        + "2. The element has a junction tag but no highway tag.",
                flags.get(0).getInstructions()));
    }

    @Test
    public void validEmptyConfigTest()
    {
        this.verifier.actual(this.setup.testAtlas(),
                new InvalidTagsCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"InvalidTagsCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[]}}")));
        this.verifier.verifyEmpty();
    }
}
