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
     * @param s
     *            passed as an argument to check if valid parenthesis
     * @return true if passed argument is valid parenthesis
     */
    public static boolean isValidParenthesis(final String s)
    {
        final char[] chars = s.toCharArray();
        final Deque<Character> stack = new ArrayDeque<>();
        for (final char c : chars)
        {
            if ((c == ')' && (stack.isEmpty() || stack.pop() != '('))
                    || (c == ']' && (stack.isEmpty() || stack.pop() != '['))
                    || (c == '}' && (stack.isEmpty() || stack.pop() != '{')))
                return false;
            if (c == '(' || c == '[' || c == '{')
                stack.push(c);
        }
        return stack.isEmpty();
    }

    private ValuePredicates()
    {
        // default constructor to fix the error "Utility classes do have public or default
        // constructor"
    }
}
