package org.openstreetmap.atlas.checks.validation.linear.lines;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * GeneralizedCoastlineCheck tests
 *
 * @author seancoulter
 */
public class GeneralizedCoastlineCheckTest
{

    @Rule
    public GeneralizedCoastlineCheckTestRule setup = new GeneralizedCoastlineCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private static final Configuration CONFIG_DEFAULT = ConfigurationResolver.emptyConfiguration();
    private static final String sharpAngleLocation = "47.5998, -122.3182";

    @Test
    public void exactThresholdGeneralized()
    {
        this.verifier.actual(this.setup.getExactThresholdGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"GeneralizedCoastlineCheck\":{\"node.minimum.threshold\":33.33}}")));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void lessThanThresholdNotGeneralized()
    {
        this.verifier.actual(this.setup.getLessThanThresholdNotGeneralized(),
                new GeneralizedCoastlineCheck(CONFIG_DEFAULT));
        this.verifier.verifyEmpty();
    }

    @Test
    public void moreThanThresholdGeneralized()
    {
        this.verifier.actual(this.setup.getMoreThanThresholdGeneralized(),
                new GeneralizedCoastlineCheck(CONFIG_DEFAULT));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void nestedRelationGeneralized()
    {
        this.verifier.actual(this.setup.getWithNestedRelationGeneralized(),
                new GeneralizedCoastlineCheck(CONFIG_DEFAULT));
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void oneLineGeneralizedOneLineNotGeneralized()
    {
        this.verifier.actual(this.setup.getOneLineGeneralizedOneLineNotGeneralized(),
                new GeneralizedCoastlineCheck(CONFIG_DEFAULT));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void oneLineSegmentGeneralized()
    {
        this.verifier.actual(this.setup.getOneLineSegmentGeneralized(),
                new GeneralizedCoastlineCheck(CONFIG_DEFAULT));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void oneLineSegmentNotGeneralized()
    {
        this.verifier.actual(this.setup.getOneLineSegmentNotGeneralized(),
                new GeneralizedCoastlineCheck(CONFIG_DEFAULT));
        this.verifier.verifyEmpty();
    }

    @Test
    public void oneRelationGeneralized()
    {
        this.verifier.actual(this.setup.getWithSingleRelationGeneralized(),
                new GeneralizedCoastlineCheck(CONFIG_DEFAULT));
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void testSharpAngleNoFilter()
    {
        this.verifier.actual(this.setup.getWithSharpAngle(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getPoints().stream()
                .noneMatch(angle -> angle.equals(Location.forString(sharpAngleLocation)))));
    }

    @Test
    public void testSharpAngleYesFilter()
    {
        this.verifier.actual(this.setup.getWithSharpAngle(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"GeneralizedCoastlineCheck\":{\"angle.minimum.threshold\":97.0}}")));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getPoints().stream()
                .anyMatch(angle -> angle.equals(Location.forString(sharpAngleLocation)))));
    }

}
