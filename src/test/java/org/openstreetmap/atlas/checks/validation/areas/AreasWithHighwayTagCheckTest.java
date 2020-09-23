package org.openstreetmap.atlas.checks.validation.areas;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author matthieun
 * @author daniel-baah
 * @author bbreithaupt
 */
public class AreasWithHighwayTagCheckTest
{

    @Rule
    public AreasWithHighwayTagCheckTestRule setup = new AreasWithHighwayTagCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final AreasWithHighwayTagCheck check = new AreasWithHighwayTagCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"AreasWithHighwayTagCheck\":{\"tags.filter\":\"highway->*&area->yes\"}}"));

    private static void verifyObjectsAndSuggestions(final CheckFlag flag, final int count)
    {
        Assert.assertEquals(count, flag.getFlaggedObjects().size());
        Assert.assertEquals(count, flag.getFixSuggestions().size());
        final List<Long> objectIds = flag.getFlaggedObjects().stream()
                .map(object -> Long.valueOf(object.getProperties().get("identifier")))
                .collect(Collectors.toList());
        flag.getFixSuggestions().forEach(
                suggestion -> Assert.assertTrue(objectIds.contains(suggestion.getIdentifier())));
    }

    @Test
    public void areaNoHighwayTag()
    {
        this.verifier.actual(this.setup.areaNoHighwayTagAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void connectedEdgesBadTags()
    {
        this.verifier.actual(this.setup.connectedEdgesBadTags(), this.check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 2));
    }

    @Test
    public void invalidAreaHighwayFootwayTag()
    {
        this.verifier.actual(this.setup.invalidAreaHighwayFootwayTagAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 1));
    }

    @Test
    public void invalidAreaHighwayPrimaryTag()
    {
        this.verifier.actual(this.setup.invalidAreaHighwayPrimaryTagAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 1));
    }

    @Test
    public void invalidEdgeHighwaySecondary()
    {
        this.verifier.actual(this.setup.invalidEdgeHighwaySecondary(), this.check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> verifyObjectsAndSuggestions(flag, 1));
    }

    @Test
    public void invalidHighwayPedestrianNoAreaTag()
    {
        this.verifier.actual(this.setup.invalidHighwayPedestrianNoAreaTagAtlas(), this.check);
        this.verifier.verify(flag -> Assert.assertEquals(flag.getInstructions(),
                String.format("Area with OSM ID %s is missing area tag.",
                        AreasWithHighwayTagCheckTestRule.INVALID_AREA_ID)));
    }

    @Test
    public void validAreaHighwayPlatform()
    {
        this.verifier.actual(this.setup.validAreaHighwayPlatform(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validAreaHighwayPrimaryNoAreaTag()
    {
        this.verifier.actual(this.setup.validAreaHighwayPrimaryNoAreaTag(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validEdgeHighwayService()
    {
        this.verifier.actual(this.setup.validEdgeHighwayService(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validEdgeNoAreaTag()
    {
        this.verifier.actual(this.setup.validEdgeNoAreaTag(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validHighwayPedestrianTag()
    {
        this.verifier.actual(this.setup.validHighwayPedestrianTagAtlas(), this.check);
        this.verifier.verifyEmpty();
    }
}
