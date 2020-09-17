package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests {@link ConflictingTagCombinationCheck}
 *
 * @author mkalender
 * @author bbreithaupt
 */
public class ConflictingTagCombinationCheckTest
{
    private static final String BUILDING = "building";
    private static final String ROUTE = "route";
    private static final String HIGHWAY = "highway";
    private static final String NATURAL = "natural";
    private static final String SERVICE = "service";

    private static final BaseCheck<String> check = new ConflictingTagCombinationCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public ConflictingTagCombinationCheckTestRule setup = new ConflictingTagCombinationCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidAtlasTest()
    {
        this.verifier.actual(this.setup.invalidAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(SERVICE));
            Assert.assertTrue(flag.getInstructions().contains(HIGHWAY));
        });
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(NATURAL));
            Assert.assertTrue(flag.getInstructions().contains(HIGHWAY));
        });
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(BUILDING));
            Assert.assertTrue(flag.getInstructions().contains(HIGHWAY));
        });
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(ROUTE));
            Assert.assertTrue(flag.getInstructions().contains(HIGHWAY));
        });
    }

    @Test
    public void invalidHighwayBuildingAtlasTest()
    {
        this.verifier.actual(this.setup.invalidHighwayBuildingAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(BUILDING));
            Assert.assertTrue(flag.getInstructions().contains(HIGHWAY));
        });
    }

    @Test
    public void invalidHighwayFerryAtlasTest()
    {
        this.verifier.actual(this.setup.invalidHighwayFerryAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(ROUTE));
            Assert.assertTrue(flag.getInstructions().contains(HIGHWAY));
        });
    }

    @Test
    public void invalidHighwayLanduseTagAtlasTest()
    {
        this.verifier.actual(this.setup.invalidHighwayLanduseTagAtlas(), check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void invalidHighwayNaturalAtlasTest()
    {
        this.verifier.actual(this.setup.invalidHighwayNaturalAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(flag.getInstructions().contains(NATURAL));
            Assert.assertTrue(flag.getInstructions().contains(HIGHWAY));
        });
    }

    @Test
    public void invalidHighwayPlaceTagAtlasCheck()
    {
        this.verifier.actual(this.setup.invalidHighwayPlaceTagAtlas(), check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void invalidHighwayServiceTagAtlasCheck()
    {
        this.verifier.actual(this.setup.validHighwayServiceTagAtlas(),
                new ConflictingTagCombinationCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"ConflictingTagCombinationCheck\":{\"tags.conflicting.filters\":[\"service->driveway&highway->construction\"]}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidServiceAtlasTest()
    {
        this.verifier.actual(this.setup.invalidServiceAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(4, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(SERVICE)));
    }

    @Test
    public void invalidServiceTagAtlasTest()
    {
        this.verifier.actual(this.setup.invalidServiceAtlas(), check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void validHighwayBuildingAtlasTest()
    {
        this.verifier.actual(this.setup.validHighwayBuldingAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validHighwayFerryAtlasTest()
    {
        this.verifier.actual(this.setup.validHighwayFerryAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validHighwayNaturalAtlasTest()
    {
        this.verifier.actual(this.setup.validHighwayNaturalAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validHighwayServiceTagAtlasCheck()
    {
        this.verifier.actual(this.setup.validHighwayServiceTagAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validNonNavigableHighwayAtlasTest()
    {
        this.verifier.actual(this.setup.validNonNavigableHighwayAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validServiceAtlasTest()
    {
        this.verifier.actual(this.setup.validServiceAtlas(), check);
        this.verifier.verifyEmpty();
    }
}
