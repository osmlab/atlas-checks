package org.openstreetmap.atlas.checks.atlas.predicates;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;

/**
 * Collection of value based predicates
 *
 * @author brian_l_davis
 */
public final class ValuePredicates
{
    /**
     * Tests if the {@link String} contains '(' and ')' that are balanced and closed. Balanced
     * meaning there are the same number of open parentheses as closed. Closed meaning that every
     * open parentheses is followed by a closed parentheses.
     */
    private static Predicate<String> ARE_PARENTHESES_BALANCED_AND_CLOSED = value ->
    {
        final char[] chars = value.toCharArray();
        final Deque<Character> stack = new ArrayDeque<>();
        for (final char character : chars)
        {
            if ((character == ')' && (stack.isEmpty() || stack.pop() != '('))
                    || (character == ']' && (stack.isEmpty() || stack.pop() != '['))
                    || (character == '}' && (stack.isEmpty() || stack.pop() != '{')))
            {
                return false;
            }
            if (character == '(' || character == '[' || character == '{')
            {
                stack.push(character);
            }
        }
        return stack.isEmpty();
    };

    public static Predicate<String> isValidParenthesis()
    {
        return ARE_PARENTHESES_BALANCED_AND_CLOSED;
    }

    private ValuePredicates()
    {
        // default constructor to fix the error "Utility classes do have public or default
        // constructor"
    }
}
