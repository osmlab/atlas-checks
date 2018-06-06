package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link OneMemberRelationCheck}
 *
 * @author savannahostrowski
 */
public class OneMemberRelationCheckTest
{
    private static final OneMemberRelationCheck check = new OneMemberRelationCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public OneMemberRelationCheckTestRule setup = new OneMemberRelationCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testValidRelation()
    {
        this.verifier.actual(this.setup.getValidRelation(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testOneMemberRelation()
    {
        this.verifier.actual(this.setup.getOneMemberRelation(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testOneMemberRelationMultipolygonInner()
    {
        this.verifier.actual(this.setup.getOneMemberRelationMultipolygonInner(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testOneMemberRelationMultipolygonOuter()
    {
        this.verifier.actual(this.setup.getOneMemberRelationMultipolygonOuter(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testValidRelationMultipolygon()
    {
        this.verifier.actual(this.setup.getValidRelationMultipolygon(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
