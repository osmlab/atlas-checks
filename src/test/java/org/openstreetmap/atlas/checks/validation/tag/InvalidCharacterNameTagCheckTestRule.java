package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test data for {@link InvalidCharacterNameTagCheckTest}
 *
 * @author sayas01
 */
public class InvalidCharacterNameTagCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "13.4979706, -88.8739079";
    private static final String TEST_2 = "13.4973655, -88.8734680";

    private static final String POND_1 = "14.7432879, -90.8914377";
    private static final String POND_2 = "14.7432925, -90.8913715";
    private static final String POND_3 = "14.7432114, -90.8913634";
    private static final String POND_4 = "14.7432020, -90.8914312";

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = POND_1)),
            @Node(id = "2000000", coordinates = @Loc(value = POND_2)),
            @Node(id = "3000000", coordinates = @Loc(value = POND_3)),
            @Node(id = "4000000", coordinates = @Loc(value = POND_4)) }, areas = {
                    @Area(id = "1001000000", coordinates = { @Loc(value = POND_1),
                            @Loc(value = POND_2), @Loc(value = POND_3),
                            @Loc(value = POND_4) }, tags = { "name=Pleta \"Chi Poyón\"",
                                    "natural=water", "water=pond" }) })
    private Atlas invalidCharacterInAreaAtlas;

    @TestAtlas(loadFromTextResource = "invalidNameRelation.txt")
    private Atlas invalidCharacterInRelationAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name=Rio Guamá 2",
                                    "natural=spring" }) })
    private Atlas numbersInNameTagAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name:es=Rio Guamá 2",
                                    "waterway=stream" }) })
    private Atlas numbersInLocalizedNameTagAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name=Rio # Guamá ",
                                    "natural=spring" }) })
    private Atlas specialCharactersInNameTagAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name:es=Rio #Guamá",
                                    "waterway=river" }) })
    private Atlas specialCharactersInLocalizedNameTagAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name=Rio \"Guamá\"",
                                    "natural=spring" }) })
    private Atlas doubleQuotesInNameTagAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name:en=Rio \"Guamá\"",
                                    "waterway=river" }) })
    private Atlas doubleQuotesInLocalizedNameTagAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name=Rio “Guamá”",
                                    "natural=spring" }) })
    private Atlas smartQuotesInNameTagAtlas;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, lines = {
                    @Line(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "name=Rio “Guamá",
                                    "name:es=Rio “Guamá” ", "natural=spring" }) })
    private Atlas smartQuotesInLocalizedNameTagAtlas;

    public Atlas getDoubleQuotesInLocalizedNameTagAtlas()
    {
        return this.doubleQuotesInLocalizedNameTagAtlas;
    }

    public Atlas getDoubleQuotesInNameTagAtlas()
    {
        return this.doubleQuotesInNameTagAtlas;
    }

    public Atlas getInvalidCharacterInAreaAtlas()
    {
        return this.invalidCharacterInAreaAtlas;
    }

    public Atlas getInvalidCharacterInRelationAtlas()
    {
        return this.invalidCharacterInRelationAtlas;
    }

    public Atlas getNumbersInLocalizedNameTagAtlas()
    {
        return this.numbersInLocalizedNameTagAtlas;
    }

    public Atlas getNumbersInNameTagAtlas()
    {
        return this.numbersInNameTagAtlas;
    }

    public Atlas getSmartQuotesInLocalizedNameTagAtlas()
    {
        return this.smartQuotesInLocalizedNameTagAtlas;
    }

    public Atlas getSmartQuotesInNameTagAtlas()
    {
        return this.smartQuotesInNameTagAtlas;
    }

    public Atlas getSpecialCharactersInLocalizedNameTagAtlas()
    {
        return this.specialCharactersInLocalizedNameTagAtlas;
    }

    public Atlas getSpecialCharactersInNameTagAtlas()
    {
        return this.specialCharactersInNameTagAtlas;
    }
}
