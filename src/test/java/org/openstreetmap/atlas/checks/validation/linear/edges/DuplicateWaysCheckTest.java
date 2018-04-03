package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link DuplicateWaysCheck}
 *
 * @author savannahostrowski
 */
public class DuplicateWaysCheckTest
{
    @Rule
    public DuplicateWaysCheckTestRule setup = new DuplicateWaysCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void duplicateEdgeCompleteCoverageTwoEdges()
    {
        this.verifier.actual(this.setup.duplicateEdgeCompleteCoverageTwoEdges(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void duplicateEdgeCompleteCoverageTwoEdgesArea()
    {
        this.verifier.actual(this.setup.duplicateEdgeCompleteCoverageTwoEdgesArea(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void duplicateEdgeCompleteCoverageThreeEdges()
    {
        this.verifier.actual(this.setup.duplicateEdgeCompleteCoverageThreeEdges(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

    @Test
    public void duplicateEdgeCompleteCoverageThreeEdgesOneTag()
    {
        this.verifier.actual(this.setup.getDuplicateEdgeCompleteCoverageThreeEdgesAreaOneTag(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void duplicateEdgeCompleteCoverageThreeEdgesTwoTag()
    {
        this.verifier.actual(this.setup.getDuplicateEdgeCompleteCoverageThreeEdgesAreaTwoTag(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void duplicateEdgePartialCoverageTwoEdges()
    {
        this.verifier.actual(this.setup.duplicateEdgePartialCoverageTwoEdges(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void duplicateEdgePartialCoverageThreeEdges()
    {
        this.verifier.actual(this.setup.duplicateEdgePartialCoverageThreeEdges(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));

    }

    @Test
    public void duplicateEdgeNoCoverageTwoEdges()
    {
        this.verifier.actual(this.setup.duplicateEdgeNoCoverageTwoEdges(),
                new DuplicateWaysCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));

    }
}
