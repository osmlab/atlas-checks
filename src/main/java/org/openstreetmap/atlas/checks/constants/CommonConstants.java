package org.openstreetmap.atlas.checks.constants;

/**
 * Holds commonly used constants throughout the checks codebase
 *
 * @author mgostintsev
 */
public final class CommonConstants
{
    private CommonConstants()
    {
        // default constructor to fix the error "Utility classes do have public or default
        // constructor"
    }

    // Common string literals
    public static final String COMMA = ",";
    public static final String SEMI_COLON = ";";
    public static final String COLON = ":";
    public static final String NEW_LINE = "\n";
    public static final String SINGLE_SPACE = " ";
    public static final String EMPTY_STRING = "";
    public static final String PIPE = "|";
    public static final String PADDED_PIPE = " | ";
    public static final String DASH = "-";
    public static final String UNDERSCORE = "_";
    public static final String EQUALS_TO = "=";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    // Common characters
    public static final char OPEN_PARENTHESES_CHAR = '(';
    public static final char CLOSED_PARENTHESES_CHAR = ')';

    // Common file extensions
    public static final String GEOJSON_FILE_EXTENSION = "geojson";
}
