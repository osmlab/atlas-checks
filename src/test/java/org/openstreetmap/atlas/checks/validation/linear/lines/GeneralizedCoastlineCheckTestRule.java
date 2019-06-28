package org.openstreetmap.atlas.checks.validation.linear.lines;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * GeneralizedCoastlineCheck test rule containing sample test atlases
 *
 * @author seancoulter
 */
public class GeneralizedCoastlineCheckTestRule extends CoreTestRule
{

    private static final String GENERALIZED_ONE = "-1.1828327178955078, 50.60478594162257";
    private static final String GENERALIZED_TWO = "-1.1796140670776367, 50.59647795127558";
    private static final String GENERALIZED_THREE = "-1.1839485168457031, 50.6037781652228568";
    private static final String GENERALIZED_FOUR = "-1.1837339401245117, 50.603641977567506";
    private static final String GENERALIZED_FIVE = "-21.072356700897217, 63.83826549528443";
    private static final String GENERALIZED_SIX = "-21.070640087127686, 63.83778298895231";
    private static final String GENERALIZED_SEVEN = "-21.06770038604736, 63.837300474348496";
    private static final String GENERALIZED_EIGHT = "-21.067142486572266, 63.834272742952685";
    private static final String GENERALIZED_NINE = "-21.0593318939209, 63.833742856474814";

    private static final String GENERALIZED_TEN = "-21.05778694152832, 63.83302370887263";
    private static final String GENERALIZED_ELEVEN = "-21.058902740478512, 63.832455947788745";

    private static final String GENERALIZED_TWELVE = "-1.1808907985687256, 50.60060148554075";
    private static final String GENERALIZED_THIRTEEN = "-1.1802524328231812, 50.600618510119126";
    private static final String GENERALIZED_FOURTEEN = "-1.1801719665527344, 50.59947784975088";
    private static final String GENERALIZED_FIFTEEN = "-1.1804670095443726, 50.59954594965331";
    private static final String GENERALIZED_SIXTEEN = "-1.180483102798462, 50.599733223876946";
    private static final String GENERALIZED_SEVENTEEN = "-1.1825752258300781, 50.59965831427692";
    private static final String GENERALIZED_EIGHTEEN = " -1.1824679374694824, 50.599491469739256";
    private static final String GENERALIZED_NINETEEN = "-1.182006597518921, 50.599491469739256";
    private static final String GENERALIZED_TWENTY = "-1.1820226907730103, 50.59929397952208";

    private static final String GENERALIZED_TWENTYONE = "-1.1812824010849, 50.6005418994679";
    private static final String GENERALIZED_TWENTYTWO = "-1.1812207102775574, 50.600523172400834";
    private static final String GENERALIZED_TWENTYTHREE = "-1.1796998977661133, 50.59173765385348";

    private static final String GENERALIZED_TWENTYFOUR = "-21.072356700897217, 63.83826549528443";
    private static final String GENERALIZED_TWENTYFIVE = "-21.070640087127686, 63.83778298895231";

    @TestAtlas(
            // One generalized coastline, one noisy line
            lines = {
                    @Line(id = "1", tags = "natural=coastline", coordinates = {
                            @Loc(value = GENERALIZED_ONE), @Loc(value = GENERALIZED_TWO) }),
                    @Line(id = "2", tags = "natural=scrub", coordinates = {
                            @Loc(value = GENERALIZED_TWENTYTWO), @Loc(value = GENERALIZED_SIX) }) },
            // Noisy nodes
            nodes = { @Node(id = "3", coordinates = @Loc(value = GENERALIZED_EIGHT)),
                    @Node(id = "4") },
            // Relation containing all the lines and nodes in this test atlas
            relations = { @Relation(tags = { "natural=coastline" }, members = {
                    @Member(id = "1", role = "na", type = "line"),
                    @Member(id = "2", role = "na", type = "line"),
                    @Member(id = "3", role = "na", type = "node"),
                    @Member(id = "4", role = "na", type = "node") }) })
    private Atlas withSingleRelationGeneralized;

    @TestAtlas(lines = {
            @Line(id = "1", tags = "natural=coastline", coordinates = {
                    @Loc(value = GENERALIZED_ONE), @Loc(value = GENERALIZED_TWO) }),
            @Line(id = "2", tags = "natural=coastline", coordinates = {
                    @Loc(value = GENERALIZED_TEN), @Loc(value = GENERALIZED_TWELVE) }) },
            // Noisy nodes
            nodes = { @Node(id = "3", coordinates = @Loc(value = GENERALIZED_EIGHT)),
                    @Node(id = "4") }, relations = {
                            // Nested relation
                            @Relation(id = "123", tags = { "natural=coastline" }, members = {
                                    @Member(id = "1", role = "na", type = "line"),
                                    @Member(id = "2", role = "na", type = "line"),
                                    @Member(id = "3", role = "na", type = "node"),
                                    @Member(id = "4", role = "na", type = "node") }),

                            // Outer relation
                            @Relation(tags = { "natural=coastline" }, members = {
                                    @Member(id = "123", role = "na", type = "relation") }) })
    private Atlas withNestedRelationGeneralized;

    @TestAtlas(lines = { @Line(tags = { "natural=coastline" }, coordinates = {
            @Loc(value = GENERALIZED_ONE), @Loc(value = GENERALIZED_TWO) }) })
    private Atlas oneLineSegmentGeneralized;

    @TestAtlas(lines = { @Line(tags = { "natural=coastline" }, coordinates = {
            @Loc(value = GENERALIZED_THREE), @Loc(value = GENERALIZED_FOUR) }) })
    private Atlas oneLineSegmentNotGeneralized;

    @TestAtlas(lines = {
            @Line(coordinates = { @Loc(value = GENERALIZED_FIVE), @Loc(value = GENERALIZED_SIX),
                    @Loc(value = GENERALIZED_SEVEN), @Loc(value = GENERALIZED_EIGHT),
                    @Loc(value = GENERALIZED_NINE), @Loc(value = GENERALIZED_TEN),
                    @Loc(value = GENERALIZED_ELEVEN) }, tags = { "natural=coastline" }) })
    private Atlas moreThanThresholdGeneralized;

    @TestAtlas(lines = {
            @Line(tags = { "natural=coastline" }, coordinates = { @Loc(value = GENERALIZED_TWELVE),
                    @Loc(value = GENERALIZED_THIRTEEN), @Loc(value = GENERALIZED_FOURTEEN),
                    @Loc(value = GENERALIZED_FIFTEEN), @Loc(value = GENERALIZED_SIXTEEN),
                    @Loc(value = GENERALIZED_SEVENTEEN), @Loc(value = GENERALIZED_EIGHTEEN),
                    @Loc(value = GENERALIZED_NINETEEN), @Loc(value = GENERALIZED_TWENTY) }) })
    private Atlas lessThanThresholdNotGeneralized;

    @TestAtlas(lines = { @Line(tags = { "natural=coastline" }, coordinates = {
            @Loc(value = GENERALIZED_TWENTYONE), @Loc(value = GENERALIZED_TWENTYTWO),
            @Loc(value = GENERALIZED_TWENTYTHREE) }) })
    private Atlas exactThresholdGeneralized;

    @TestAtlas(lines = {
            @Line(tags = { "natural=coastline" }, coordinates = { @Loc(value = GENERALIZED_TWELVE),
                    @Loc(value = GENERALIZED_THIRTEEN), @Loc(value = GENERALIZED_FOURTEEN),
                    @Loc(value = GENERALIZED_FIFTEEN), @Loc(value = GENERALIZED_SIXTEEN),
                    @Loc(value = GENERALIZED_SEVENTEEN), @Loc(value = GENERALIZED_EIGHTEEN),
                    @Loc(value = GENERALIZED_NINETEEN), @Loc(value = GENERALIZED_TWENTY) }),
            @Line(tags = { "natural=coastline" }, coordinates = {
                    @Loc(value = GENERALIZED_TWENTYFOUR), @Loc(value = GENERALIZED_TWENTYFIVE) }) })
    private Atlas oneLineGeneralizedOneLineNotGeneralized;

    public Atlas getWithSingleRelationGeneralized()
    {
        return this.withSingleRelationGeneralized;
    }

    public Atlas getWithNestedRelationGeneralized()
    {
        return this.withNestedRelationGeneralized;
    }

    public Atlas getMoreThanThresholdGeneralized()
    {
        return this.moreThanThresholdGeneralized;
    }

    public Atlas getLessThanThresholdNotGeneralized()
    {
        return this.lessThanThresholdNotGeneralized;
    }

    public Atlas getExactThresholdGeneralized()
    {
        return this.exactThresholdGeneralized;
    }

    public Atlas getOneLineSegmentGeneralized()
    {
        return this.oneLineSegmentGeneralized;
    }

    public Atlas getOneLineSegmentNotGeneralized()
    {
        return this.oneLineSegmentNotGeneralized;
    }

    public Atlas getOneLineGeneralizedOneLineNotGeneralized()
    {
        return this.oneLineGeneralizedOneLineNotGeneralized;
    }

}
