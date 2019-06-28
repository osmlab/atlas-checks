package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * This is a basic unit test to validate the results of our PoolSizeCheck
 * 
 * @author cuthbertm
 */
public class PoolSizeCheckTest
{
    // The setup class contains our test atlas to use in validating results see {@link
    // PoolSizeCheckTestRule}
    @Rule
    public PoolSizeCheckTestRule setup = new PoolSizeCheckTestRule();

    // The verifier class runs our check and has helper functions to validate the results of the
    // check
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    /**
     * Tests whether the pool in our test atlas falls outside the bounds set by the configuration
     * and is larger than the maximum allowed size. In this test case we are using the same test
     * atlas so we simply modify the maximum to 60m2 as the pool in our test atlas is 63.5m2
     */
    @Test
    public void testLargePoolSize()
    {
        this.verifier.actual(this.setup.getPoolAtlas(),
                new PoolSizeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"PoolSizeCheck\":{\"surface.maximum\":60.0,\"surface.minimum\":1.0}}")));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Tests whether the pool in our test atlas falls outside the bounds set by the configuration
     * and is smaller than the minimum allowed size. In this test case we are using the same test
     * atlas so we simply modify the minimum to 70m2 as the pool in our test atlas is 63.5m2
     */
    @Test
    public void testSmallPoolSize()
    {
        this.verifier.actual(this.setup.getPoolAtlas(),
                new PoolSizeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"PoolSizeCheck\":{\"surface.maximum\":100.0,\"surface.minimum\":70.0}}")));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Tests whether the pool in our test atlas falls within the bounds set by the configuration, in
     * this instance we expect the pool to be greater than 1m2 and less than 100m2
     */
    @Test
    public void testValidPoolSize()
    {
        this.verifier.actual(this.setup.getPoolAtlas(),
                new PoolSizeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"PoolSizeCheck\":{\"surface.maximum\":100.0,\"surface.minimum\":1.0}}")));
        this.verifier.verifyEmpty();
    }
}
