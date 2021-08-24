package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link InvalidTurnRestrictionCheck} tests.
 *
 * @author gpogulsky
 * @author bbreithaupt
 */
public class InvalidTurnRestrictionTest
{
    private static final InvalidTurnRestrictionCheck testCheck = new InvalidTurnRestrictionCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public InvalidTurnRestrictionTestRule testCaseRule = new InvalidTurnRestrictionTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void disconnectedFromTest()
    {
        this.verifier.actual(this.testCaseRule.disconnectedFromAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("There is not a single navigable route to restrict")));
    }

    @Test
    public void doubleFromTest()
    {
        this.verifier.actual(this.testCaseRule.doubleFromAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("There is not a single navigable route to restrict")));
    }

    @Test
    public void doubleViaTest()
    {
        this.verifier.actual(this.testCaseRule.doubleViaAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertTrue(
                flag.getInstructions().contains("A Turn Restriction should only have 1 via Node")));
    }

    @Test
    public void onlyViaTest()
    {
        this.verifier.actual(this.testCaseRule.onlyViaAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("Missing a FROM and/or TO member and/or VIA member")));
    }

    @Test
    public void redundantRestrictionTest()
    {
        this.verifier.actual(this.testCaseRule.redundantRestrictionAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("There is not a single navigable route to restrict")));
    }

    @Test
    public void sameFromToNoViaTest()
    {
        this.verifier.actual(this.testCaseRule.sameFromToNoViaAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("Missing a FROM and/or TO member and/or VIA member")));
    }

    @Test
    public void testGoodAtlas()
    {
        this.verifier.actual(this.testCaseRule.getGoodAtlas(), testCheck);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testStraightTopology()
    {
        this.verifier.actual(this.testCaseRule.straightTopologyAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("Restriction doesn't match topology")));        
    }

    @Test
    public void testInvalidAtlas()
    {
        this.verifier.actual(this.testCaseRule.getInvalidAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void testInvalidRelationAtlas()
    {
        this.verifier.actual(this.testCaseRule.getInvalidRelationAtlas(), testCheck);
        this.verifier.verifyNotEmpty();
    }

}
