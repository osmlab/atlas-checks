package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

public class ConstructionCheckTest
{
    @Rule
    public ConstructionCheckTestRule setup = new ConstructionCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final ConstructionCheck check = new ConstructionCheck(ConfigurationResolver
        .inlineConfiguration(
            "{" +
                "\"ConstructionCheck\": {" +
                    "\"oldConstructionDays\": 730.0," +
                    "\"oldCheckDateMonths\": 6.0" +
                "}" +
            "}"
        ));

    @Test
    public void testyyyyDateFormatter()
    {
        this.verifier.actual(this.setup.yyyyDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testyyyydDateFormatter()
    {
        this.verifier.actual(this.setup.yyyydDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testdyyyyDateFormatter()
    {
        this.verifier.actual(this.setup.dyyyyDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testMMMyyyyDateFormatter()
    {
        this.verifier.actual(this.setup.MMMyyyyDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testMMMMyyyyDateFormatter()
    {
        this.verifier.actual(this.setup.MMMMyyyyDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testyyyyMdDateFormatter()
    {
        this.verifier.actual(this.setup.yyyyMdDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testdMyyyyDateFormatter()
    {
        this.verifier.actual(this.setup.dMyyyyDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testdMMMyyyyDateFormatter()
    {
        this.verifier.actual(this.setup.dMMMyyyyDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testdMMMMyyyyDateFormatter()
    {
        this.verifier.actual(this.setup.dMMMMyyyyDateFormatterAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testIsConstruction()
    {
        this.verifier.actual(this.setup.isConstructionAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testisNotConstructionColonDateAtlas()
    {
        this.verifier.actual(this.setup.isNotConstructionColonDateAtlas(), this.check);
        this.verifier.verifyEmpty();
    }
    @Test
    public void testIsStartsWithConstructionColon()
    {
        this.verifier.actual(this.setup.isStartsWithConstructionColonAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testIsHighwayConstruction()
    {
        this.verifier.actual(this.setup.isHighwayConstructionAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testIsLanduseConstruction()
    {
        this.verifier.actual(this.setup.isLandUseConstructionAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
    @Test
    public void testIsBuildingConstruction()
    {
        this.verifier.actual(this.setup.isBuildingConstructionAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testOldCheckDateAtlas()
    {
        this.verifier.actual(this.setup.oldCheckDateAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testOldLastEditTimeAtlas()
    {
        this.verifier.actual(this.setup.oldLastEditTimeAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
