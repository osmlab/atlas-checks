package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link InvalidTurnRestrictionCheck} tests.
 *
 * @author gpogulsky
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
    public void testGoodAtlas()
    {
        this.verifier.actual(this.testCaseRule.getGoodAtlas(), testCheck);
        this.verifier.verifyEmpty();
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
