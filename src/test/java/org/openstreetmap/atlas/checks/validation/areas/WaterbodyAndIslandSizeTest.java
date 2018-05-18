package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * WaterbodyAndIslandSizeTest unit tests
 *
 * @author danielbaah
 */
public class WaterbodyAndIslandSizeTest
{
    @Rule
    public WaterbodyAndIslandSizeTestRule setup = new WaterbodyAndIslandSizeTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void largeIsletTest()
    {
        this.verifier.actual(this.setup.getLargeIsletAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void validIslandSizeTest()
    {
        this.verifier.actual(this.setup.getValidSizeIslandAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void smallArchipelagoTest()
    {
        this.verifier.actual(this.setup.getSmallArchipelagoAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallWaterbodyTest()
    {
        this.verifier.actual(this.setup.getSmallWaterbodyAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallIsletAtlas()
    {
        this.verifier.actual(this.setup.getSmallIsletAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallMultiPolygonIslandTest()
    {
        this.verifier.actual(this.setup.getSmallMultiPolygonIslandAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallRockMultiPolygonIslandTest()
    {
        this.verifier.actual(this.setup.getSmallRockMultiPolygonIslandAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void smallIslandMultiPolygonDuplicateTest()
    {
        this.verifier.actual(this.setup.getSmallIslandMultiPolygonDuplicateAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidMultiPolygonNoNaturalWaterTagRelationTest()
    {
        this.verifier.actual(this.setup.getInvalidMultiPolygonNoNaturalWaterTagRelationAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void smallMultiPolygonWaterbodyMemberTest()
    {
        this.verifier.actual(this.setup.getSmallMultiPolygonWaterbodyMemberAtlas(),
                new WaterbodyAndIslandSizeCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotEmpty();
    }
}
