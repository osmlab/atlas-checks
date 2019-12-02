package org.openstreetmap.atlas.checks.atlas.predicates;

import java.util.ArrayDeque;
import java.util.Deque;

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
     * 
     * @param value
     *            passed as an argument to check if valid parenthesis
     * @return true if passed argument is valid parenthesis
     */
    public static boolean isValidParenthesis(final String value)
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
    }

    private ValuePredicates()
    {
        // default constructor to fix the error "Utility classes do have public or default
        // constructor"
    }
}
