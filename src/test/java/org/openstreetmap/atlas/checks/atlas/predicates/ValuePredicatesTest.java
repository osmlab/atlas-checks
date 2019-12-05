package org.openstreetmap.atlas.checks.atlas.predicates;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
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
        final List<String> parenthesisList = new ArrayList<>(Arrays.asList("{()}[", "[]"));
        assertEquals(
                parenthesisList.stream().filter(this.valuePredicates.isValidParenthesis()).count(),
                1);
    }

}
