package org.openstreetmap.atlas.checks.constants;

/**
 * Holds commonly used constants throughout the checks codebase
 *
 * @author mgostintsev
 */
public interface CommonConstants
{
    // Common string literals
    String COMMA = ",";
    String SEMI_COLON = ";";
    String NEW_LINE = "\n";
    String SINGLE_SPACE = " ";
    String EMPTY_STRING = "";
    String PIPE = "|";
    String PADDED_PIPE = " | ";
    String DASH = "-";
    String UNDERSCORE = "_";
    String EQUALS = "=";
    String LINE_SEPARATOR = System.getProperty("line.separator");

    // Common characters
    char OPEN_PARENTHESES_CHAR = '(';
    char CLOSED_PARENTHESES_CHAR = ')';

    // Common file extensions
    String GEOJSON_FILE_EXTENSION = "geojson";
}
