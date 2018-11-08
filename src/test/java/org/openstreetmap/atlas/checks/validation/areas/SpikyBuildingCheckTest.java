package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;

import static org.junit.Assert.*;

public class SpikyBuildingCheckTest
{
    @Rule
    public SpikyBuildingCheckTestRule setup = new SpikyBuildingCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void findsSpikyBuildings() {
        verifier.actual(setup.getSpikyBuilding(), new SpikyBuildingCheck(
                ConfigurationResolver.emptyConfiguration()));
        verifier.verifyExpectedSize(1);
    }

    @Test
    public void findsSpikyBuildingsRoundNumbers() {
        verifier.actual(setup.getRoundNumbersSpiky(), new SpikyBuildingCheck(
                ConfigurationResolver.emptyConfiguration()));
        verifier.verifyExpectedSize(1);
    }
    @Test
    public void doesNotFindNormalBuilding() {
        verifier.actual(setup.getNormalBuilding(), new SpikyBuildingCheck(
                ConfigurationResolver.emptyConfiguration()));
        verifier.verifyEmpty();
    }

    @Test
    public void doesNotFindNormalBuildingRound() {
        verifier.actual(setup.getNormalRound(), new SpikyBuildingCheck(
                ConfigurationResolver.emptyConfiguration()));
        verifier.verifyEmpty();
    }
    @Test
    public void distanceTest() {
        verifier.verifyEmpty();
        Location start = Location.forString("0,0");
        Location end = Location.forString("0.5,0.5");
        Assert.assertEquals(5000000 * Math.sqrt(2), SpikyBuildingCheck.distance(start, end), .001);
    }

    @Test
    public void dotProdTest() {
        verifier.verifyEmpty();
        Location start = Location.forString("0,0");
        Location middle = Location.forString("0.5,0.5");
        Location end = Location.forString("1,0");
        Location otherEnd = Location.forString("1,1");
        Segment a = new Segment(start, middle);
        Segment b = new Segment(middle, end);
        Segment c = new Segment(start, end);
        Assert.assertEquals(0, SpikyBuildingCheck.dotProd(start, middle, end), .0001);
        Assert.assertEquals(-50000000000000L, SpikyBuildingCheck.dotProd(start, middle, otherEnd), .0001);
    }
}
