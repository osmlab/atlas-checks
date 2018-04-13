package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link DuplicatePointCheck}
 *
 * @author savannahostrowski
 */
public class DuplicatePointCheckTest
{
    @Rule
    public DuplicatePointCheckTestRule setup = new DuplicatePointCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void singlePoint()
    {
        this.verifier.actual(this.setup.singlePointAtlas(),
                new DuplicatePointCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void duplicatePoint()
    {
        this.verifier.actual(this.setup.duplicatePointAtlas(),
                new DuplicatePointCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

}
