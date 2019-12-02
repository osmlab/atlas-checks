package org.openstreetmap.atlas.checks.atlas.predicates;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.util.Assert;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link ValuePredicates}
 *
 * @author smaheshwaram
 */
@RunWith(MockitoJUnitRunner.class)
public class ValuePredicatesTest
{
    private ValuePredicates valuePredicates;

    @Test
    public void isValidParenthesis()
    {
        Assert.isTrue(this.valuePredicates.isValidParenthesis("({})"));
        assertFalse(this.valuePredicates.isValidParenthesis("({))"));
    }
}
