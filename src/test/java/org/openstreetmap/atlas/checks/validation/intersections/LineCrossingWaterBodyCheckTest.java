package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author mkalender
 * @author sayana_saithu
 * @author seancoulter
 */
public class LineCrossingWaterBodyCheckTest
{
    private static LineCrossingWaterBodyCheck check = new LineCrossingWaterBodyCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public LineCrossingWaterBodyCheckTestRule setup = new LineCrossingWaterBodyCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testBuildingCrossing()
    {
        this.verifier.actual(this.setup.invalidCrossingBuildingAtlas(),
                new LineCrossingWaterBodyCheck(ConfigurationResolver.inlineConfiguration(
                        "{  \"LineCrossingWaterBodyCheck\": {" + "    \"enabled\": true,"
                                + "    \"lineItems.offending\": \"railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature\","
                                + "    \"buildings.flag\": true" + "  }}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getPolyLines().size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getPoints().size()));
    }

    @Test
    public void testCrossingLineWithNoOsmTagAtlas()
    {
        this.verifier.actual(this.setup.crossingLineWithNoOsmTagAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testCrossingLineWithValidLineTagAtlas()
    {
        this.verifier.actual(this.setup.crossingLineWithValidLineTagAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getPolyLines().size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getPoints().size()));
    }

    @Test
    public void testInvalidIntersectionItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidIntersectionItemsAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getPolyLines().size()));
        this.verifier.verify(flag -> Assert.assertEquals(1, flag.getPoints().size()));
    }

    @Test
    public void testInvalidLineCrossingAtlas()
    {
        this.verifier.actual(this.setup.invalidLineCrossingAtlas(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(5, flag.getPolyLines().size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getPoints().size()));
    }

    @Test
    public void testInvalidLineCrossingRelationWaterbody()
    {
        this.verifier.actual(this.setup.invalidLineCrossingRelationWaterbody(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidLineItemCrossing()
    {
        this.verifier.actual(this.setup.invalidCrossingLineItemAtlas(),
                new LineCrossingWaterBodyCheck(ConfigurationResolver.inlineConfiguration(
                        "{  \"LineCrossingWaterBodyCheck\": {" + "    \"enabled\": true,"
                                + "    \"lineItems.offending\": \"railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature\" }}")));
        this.verifier.verify(flag -> Assert.assertEquals(1, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidWithinOuterMemberNoInteractionWithInnerMember()
    {
        this.verifier.actual(this.setup.invalidWithinOuterMemberNoInteractionWithInnerMember(),
                new LineCrossingWaterBodyCheck(ConfigurationResolver.inlineConfiguration(
                        "{  \"LineCrossingWaterBodyCheck\": {" + "    \"enabled\": true,"
                                + "    \"lineItems.offending\": \"railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature\","
                                + "    \"buildings.flag\": true" + "  }}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testMultipolygonMemberCrossingAtlas()
    {
        this.verifier.actual(this.setup.multipolygonMemberCrossingAtlas(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(4, flag.getPolyLines().size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getPoints().size()));
    }

    @Test
    public void testNoCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.noCrossingItemsAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.validCrossingItemsAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidFerryStopIntersection()
    {
        this.verifier.actual(this.setup.validFerryStopIntersection(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidFerryTerminalIntersection()
    {
        this.verifier.actual(this.setup.validFerryTerminalIntersection(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidFordAtIntersectionLocation()
    {
        this.verifier.actual(this.setup.validFordAtIntersectionLocation(),
                new LineCrossingWaterBodyCheck(ConfigurationResolver.inlineConfiguration(
                        "{  \"LineCrossingWaterBodyCheck\": {" + "    \"enabled\": true,"
                                + "    \"lineItems.offending\": \"railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature\","
                                + "    \"buildings.flag\": true" + "  }}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidFordedRoad()
    {
        this.verifier.actual(this.setup.validFordedRoad(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidIntersectionItemsAtlas()
    {
        this.verifier.actual(this.setup.validIntersectionItemsAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidLineItemCrossing()
    {
        this.verifier.actual(this.setup.validCrossingLineItemAtlas(),
                new LineCrossingWaterBodyCheck(ConfigurationResolver.inlineConfiguration(
                        "{  \"LineCrossingWaterBodyCheck\": {" + "    \"enabled\": true,"
                                + "    \"lineItems.offending\": \"railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature\" }}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSlipwayIntersection()
    {
        this.verifier.actual(this.setup.validSlipwayIntersection(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidStreetWithinInnerMember()
    {
        this.verifier.actual(this.setup.validStreetWithinInnerMember(),
                new LineCrossingWaterBodyCheck(ConfigurationResolver.inlineConfiguration(
                        "{  \"LineCrossingWaterBodyCheck\": {" + "    \"enabled\": true,"
                                + "    \"lineItems.offending\": \"railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature\","
                                + "    \"buildings.flag\": true" + "  }}")));
        this.verifier.verifyEmpty();
    }
}
