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

    private final WaterbodyAndIslandSizeCheck check = new WaterbodyAndIslandSizeCheck(
            ConfigurationResolver.emptyConfiguration());

    @Test
    public void largeIsletTest()
    {
        this.verifier.actual(this.setup.getLargeIsletAtlas(), this.check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void validIslandSizeTest()
    {
        this.verifier.actual(this.setup.getValidSizeIslandAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void smallArchipelagoTest()
    {
        this.verifier.actual(this.setup.getSmallArchipelagoAtlas(), this.check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallWaterbodyTest()
    {
        this.verifier.actual(this.setup.getSmallWaterbodyAtlas(), this.check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallIsletAtlas()
    {
        this.verifier.actual(this.setup.getSmallIsletAtlas(), this.check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallMultiPolygonIslandTest()
    {
        this.verifier.actual(this.setup.getSmallMultiPolygonIslandAtlas(), this.check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void smallRockMultiPolygonIslandTest()
    {
        this.verifier.actual(this.setup.getSmallRockMultiPolygonIslandAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void smallIslandMultiPolygonDuplicateTest()
    {
        this.verifier.actual(this.setup.getSmallIslandMultiPolygonDuplicateAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidMultiPolygonNoNaturalWaterTagRelationTest()
    {
        this.verifier.actual(this.setup.getInvalidMultiPolygonNoNaturalWaterTagRelationAtlas(),
                this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void smallMultiPolygonWaterbodyMemberTest()
    {
        this.verifier.actual(this.setup.getSmallMultiPolygonWaterbodyMemberAtlas(), this.check);
        this.verifier.verifyNotEmpty();
    }
}
