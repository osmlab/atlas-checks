package org.openstreetmap.atlas.checks.validation.linear.lines;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

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

    @Test
    public void exactThresholdGeneralized()
    {
        this.verifier.actual(this.setup.getExactThresholdGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"GeneralizedCoastlineCheck\":{\"generalizedCoastline.node.minimum.threshold\":33.33}}")));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void lessThanThresholdNotGeneralized()
    {
        this.verifier.actual(this.setup.getLessThanThresholdNotGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void moreThanThresholdGeneralized()
    {
        this.verifier.actual(this.setup.getMoreThanThresholdGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void nestedRelationGeneralized()
    {
        this.verifier.actual(this.setup.getWithNestedRelationGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void oneLineGeneralizedOneLineNotGeneralized()
    {
        this.verifier.actual(this.setup.getOneLineGeneralizedOneLineNotGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void oneLineSegmentGeneralized()
    {
        this.verifier.actual(this.setup.getOneLineSegmentGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void oneLineSegmentGeneralizedNoSource()
    {
        this.verifier.actual(this.setup.getOneLineSegmentGeneralizedNoSource(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"GeneralizedCoastlineCheck\":{\"coastline.tags.filters\": \"\"}}")));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void oneLineSegmentGeneralizedSourcePGS()
    {
        this.verifier.actual(this.setup.getOneLineSegmentGeneralizedSourcePGS(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void oneLineSegmentGeneralizedSourceSurvey()
    {
        this.verifier.actual(this.setup.getOneLineSegmentGeneralizedSourceSurvey(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void oneLineSegmentNotGeneralized()
    {
        this.verifier.actual(this.setup.getOneLineSegmentNotGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void oneRelationGeneralized()
    {
        this.verifier.actual(this.setup.getWithSingleRelationGeneralized(),
                new GeneralizedCoastlineCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(2);
    }

}
