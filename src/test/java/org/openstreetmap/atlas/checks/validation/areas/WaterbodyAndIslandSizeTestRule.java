package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * WaterbodyAndIslandSizeTestRule test Atlas
 *
 * @author danielbaah
 */
public class WaterbodyAndIslandSizeTestRule extends CoreTestRule
{

    private static final String TEST_1 = "17.65021, -63.24537";
    private static final String TEST_2 = "17.61776, -63.26013";
    private static final String TEST_3 = "17.61499, -63.22494";
    private static final String TEST_4 = "17.64467, -63.21018";
    private static final String TEST_5 = "17.65024, -63.23069";

    private static final String SMALL_AREA_TEST_1 = "17.64874807036599, -63.22976231575012";
    private static final String SMALL_AREA_TEST_2 = "17.64874807036599, -63.229751586914055";
    private static final String SMALL_AREA_TEST_3 = "17.648758294228287, -63.229751586914055";
    private static final String SMALL_AREA_TEST_4 = "17.648758294228287, -63.22976231575012";

    // Islet with surface area larger than default 10 m^2 maximum
    @TestAtlas(
            // Area
            areas = { @Area(id = "127001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3), @Loc(value = TEST_4),
                    @Loc(value = TEST_5), @Loc(value = TEST_1) }, tags = { "place=islet" }) })
    private Atlas largeIsletAtlas;

    // Island with valid surface area size
    @TestAtlas(
            // Area
            areas = { @Area(id = "127001", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2), @Loc(value = TEST_3), @Loc(value = TEST_4),
                    @Loc(value = TEST_5), @Loc(value = TEST_1) }, tags = { "place=island" }) })
    private Atlas validSizeIslandAtlas;

    // Archipelago island with surface area smaller than default 10 m^2 minimum
    @TestAtlas(
            // Area
            areas = { @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                    @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                    @Loc(value = SMALL_AREA_TEST_4),
                    @Loc(value = SMALL_AREA_TEST_1) }, tags = { "place=archipelago" }) })
    private Atlas smallArchipelagoAtlas;

    // Waterbody with surface area smaller than default 10 m^2
    @TestAtlas(
            // Area
            areas = { @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                    @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                    @Loc(value = SMALL_AREA_TEST_4),
                    @Loc(value = SMALL_AREA_TEST_1) }, tags = { "natural=water" }) })
    private Atlas smallWaterbodyAtlas;

    // Islet with surface area smaller than default 10 m^2
    @TestAtlas(
            // Area
            areas = { @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                    @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                    @Loc(value = SMALL_AREA_TEST_4),
                    @Loc(value = SMALL_AREA_TEST_1) }, tags = { "place=islet" }) })
    private Atlas smallIsletAtlas;

    // MultiPolygon Relation island with surface area smaller than 10 m^2 minimum
    @TestAtlas(
            // Areas
            areas = {
                    @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                            @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                            @Loc(value = SMALL_AREA_TEST_4), @Loc(value = SMALL_AREA_TEST_1) }),
                    @Area(id = "127002", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3), @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }) },
            // Relation
            relations = { @Relation(id = "1001", members = {
                    @Member(id = "127001", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_INNER),
                    @Member(id = "127002", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_OUTER) }, tags = {
                            "type=multipolygon", "natural=water" }) })
    private Atlas smallMultiPolygonIslandAtlas;

    // MultiPolygon water Relation missing natural=water tag
    @TestAtlas(
            // Areas
            areas = {
                    @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                            @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                            @Loc(value = SMALL_AREA_TEST_4), @Loc(value = SMALL_AREA_TEST_1) }),
                    @Area(id = "127002", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3), @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }) },
            // Relation
            relations = { @Relation(id = "1001", members = {
                    @Member(id = "127001", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_INNER),
                    @Member(id = "127002", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_OUTER) }, tags = {
                            "type=multipolygon" }) })
    private Atlas invalidMultiPolygonNoNaturalWaterTagRelationAtlas;

    // Multipolygon inner member tagged natural=rock. This should be ignored by check
    @TestAtlas(
            // Areas
            areas = {
                    @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                            @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                            @Loc(value = SMALL_AREA_TEST_4),
                            @Loc(value = SMALL_AREA_TEST_1) }, tags = { "natural=rock" }),
                    @Area(id = "127002", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3), @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }) },
            // Relation
            relations = { @Relation(id = "1001", members = {
                    @Member(id = "127001", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_INNER),
                    @Member(id = "127002", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_OUTER) }, tags = {
                            "type=multipolygon", "natural=water" }) })
    private Atlas smallRockMultiPolygonIslandAtlas;

    // Feature 127001 could be flagged as an Area OR a MultiPolygon island
    @TestAtlas(
            // Areas
            areas = {
                    @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                            @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                            @Loc(value = SMALL_AREA_TEST_4),
                            @Loc(value = SMALL_AREA_TEST_1) }, tags = { "place=island" }),
                    @Area(id = "127002", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3), @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }) },
            // Relations
            relations = { @Relation(id = "1001", members = {
                    @Member(id = "127001", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_INNER),
                    @Member(id = "127002", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_OUTER) }, tags = {
                            "type=multipolygon", "natural=water" }) })
    private Atlas smallIslandMultiPolygonDuplicateAtlas;

    // MultiPolygon Relation with outer waterbody member smaller than 10 m^2 miminum
    @TestAtlas(
            // Areas
            areas = {
                    @Area(id = "127001", coordinates = { @Loc(value = SMALL_AREA_TEST_1),
                            @Loc(value = SMALL_AREA_TEST_2), @Loc(value = SMALL_AREA_TEST_3),
                            @Loc(value = SMALL_AREA_TEST_4), @Loc(value = SMALL_AREA_TEST_1) }),
                    @Area(id = "127002", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3), @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }) },
            // Relation
            relations = { @Relation(id = "1001", members = {
                    @Member(id = "127002", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_INNER),
                    @Member(id = "127001", type = "area", role = RelationTypeTag.MULTIPOLYGON_ROLE_OUTER) }, tags = {
                            "type=multipolygon", "natural=water" }) })
    private Atlas smallMultiPolygonWaterbodyMemberAtlas;

    public Atlas getLargeIsletAtlas()
    {
        return largeIsletAtlas;
    }

    public Atlas getValidSizeIslandAtlas()
    {
        return validSizeIslandAtlas;

    }

    public Atlas getSmallArchipelagoAtlas()
    {
        return smallArchipelagoAtlas;
    }

    public Atlas getSmallWaterbodyAtlas()
    {
        return smallWaterbodyAtlas;
    }

    public Atlas getSmallIsletAtlas()
    {
        return smallIsletAtlas;
    }

    public Atlas getSmallMultiPolygonIslandAtlas()
    {
        return smallMultiPolygonIslandAtlas;
    }

    public Atlas getSmallRockMultiPolygonIslandAtlas()
    {
        return smallRockMultiPolygonIslandAtlas;
    }

    public Atlas getSmallIslandMultiPolygonDuplicateAtlas()
    {
        return smallIslandMultiPolygonDuplicateAtlas;
    }

    public Atlas getInvalidMultiPolygonNoNaturalWaterTagRelationAtlas()
    {
        return invalidMultiPolygonNoNaturalWaterTagRelationAtlas;
    }

    public Atlas getSmallMultiPolygonWaterbodyMemberAtlas()
    {
        return smallMultiPolygonWaterbodyMemberAtlas;
    }
}
