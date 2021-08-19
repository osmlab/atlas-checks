package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link DuplicateMapFeatureCheckTest} data generator
 *
 * @author Xiaohong Tang
 */
public class DuplicateMapFeatureCheckTestRule extends CoreTestRule
{
    private static final String ONE = "18.4360044, -71.7194204";
    private static final String TWO = "18.4360737, -71.6970306";
    private static final String THREE = "18.4273807, -71.7052283";
    private static final String AREA_LOCATION_ONE = "37.320524859664474, -122.03601479530336";
    private static final String AREA_LOCATION_TWO = "37.320524859664474,-122.03530669212341";
    private static final String AREA_LOCATION_THREE = "37.32097706357857, -122.03530669212341";
    private static final String AREA_LOCATION_FOUR = "37.32097706357857, -122.03601479530336";
    private static final String Node_LOCATION = "37.32067706357857, -122.03591479530336";
    private static final String Node_LOCATION2 = "37.32067706357867, -122.03591479530336";

    @TestAtlas(nodes = {
            @Node(id = "1000000", coordinates = @Loc(value = Node_LOCATION), tags = {
                    "amenity=cafe" }),
            @Node(id = "2000000", coordinates = @Loc(value = Node_LOCATION2)) }, areas = {
                    @Area(id = "12000000", coordinates = { @Loc(value = AREA_LOCATION_ONE),
                            @Loc(value = AREA_LOCATION_TWO), @Loc(value = AREA_LOCATION_THREE),
                            @Loc(value = AREA_LOCATION_FOUR),
                            @Loc(value = AREA_LOCATION_ONE) }, tags = { "amenity=cafe" }) })
    private Atlas areaNodeDuplicateFeature;

    @TestAtlas(nodes = {
            @Node(id = "1000000", coordinates = @Loc(value = Node_LOCATION), tags = {
                    "building=yes" }),
            @Node(id = "2000000", coordinates = @Loc(value = Node_LOCATION2)) }, areas = {
                    @Area(id = "12000000", coordinates = { @Loc(value = AREA_LOCATION_ONE),
                            @Loc(value = AREA_LOCATION_TWO), @Loc(value = AREA_LOCATION_THREE),
                            @Loc(value = AREA_LOCATION_FOUR),
                            @Loc(value = AREA_LOCATION_ONE) }, tags = { "building=yes",
                                    "type=any" }) })
    private Atlas areaNodeNotDuplicateFeature;

    @TestAtlas(nodes = {
            @Node(id = "1000000", coordinates = @Loc(value = ONE), tags = { "type=destination_sign",
                    "destination=Space Needle" }),
            @Node(id = "2000000", coordinates = @Loc(value = TWO), tags = { "building=yes",
                    "name=one" }),
            @Node(id = "3000000", coordinates = @Loc(value = THREE), tags = { "amenity=school",
                    "building=one" }) }, edges = {
                            @Edge(id = "12000000", coordinates = { @Loc(value = ONE),
                                    @Loc(value = TWO) }, tags = { "type=destination_sign",
                                            "destination=Space Needle" }),
                            @Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, tags = { "building=yes", "name=one" }),
                            @Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                                    @Loc(value = ONE) }, tags = { "amenity=school",
                                            "building=one" }) })
    private Atlas edgeNodeDuplicateFeature;

    @TestAtlas(nodes = {
            @Node(id = "1000000", coordinates = @Loc(value = ONE), tags = { "type=destination_sign",
                    "destination=Space Needle", "building=yes" }),
            @Node(id = "2000000", coordinates = @Loc(value = TWO), tags = { "building=yes",
                    "name=two" }),
            @Node(id = "3000000", coordinates = @Loc(value = THREE), tags = { "amenity=school",
                    "building=two" }) }, edges = {
                            @Edge(id = "12000000", coordinates = { @Loc(value = ONE),
                                    @Loc(value = TWO) }, tags = { "type=destination_sign",
                                            "destination=Space Needle" }),
                            @Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                                    @Loc(value = THREE) }, tags = { "building=yes", "name=one" }),
                            @Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                                    @Loc(value = ONE) }, tags = { "amenity=school",
                                            "building=one" }) })
    private Atlas edgeNodeNotDuplicateFeature;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
            @Node(id = "2000000", coordinates = @Loc(value = TWO)),
            @Node(id = "3000000", coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = "12000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "leisure=track", "sport=running" }),
                    @Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "amenity=park" }),
                    @Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) }, relations = {
                                    @Relation(id = "523318400000000", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "leisure=track", "sport=running", "type=any" }),
                                    @Relation(id = "523318500000000", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "amenity=park", "type=any" }) })
    private Atlas relationEdgeDuplicateFeature;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = ONE)),
            @Node(id = "2000000", coordinates = @Loc(value = TWO)),
            @Node(id = "3000000", coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = "12000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "leisure=track", "sport=cycling" }),
                    @Edge(id = "23000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "amenity=park", "name=two" }),
                    @Edge(id = "31000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }) }, relations = {
                                    @Relation(id = "523318400000000", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "leisure=track", "sport=running", "type=any" }),
                                    @Relation(id = "523318500000000", members = {
                                            @Member(id = "12000000", type = "edge", role = "any"),
                                            @Member(id = "2000000", type = "node", role = "any"),
                                            @Member(id = "23000000", type = "edge", role = "any") }, tags = {
                                                    "amenity=park", "name=one", "type=any" }) })
    private Atlas relationEdgeNotDuplicateFeature;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = Node_LOCATION), tags = {
            "leisure=pitch", "sport=soccer" }) }, areas = {
                    @Area(id = "12000000", coordinates = { @Loc(value = AREA_LOCATION_ONE),
                            @Loc(value = AREA_LOCATION_TWO), @Loc(value = AREA_LOCATION_THREE),
                            @Loc(value = AREA_LOCATION_FOUR),
                            @Loc(value = AREA_LOCATION_ONE) }, tags = {
                                    "type=any" }) }, relations = {
                                            @Relation(id = "123", members = {
                                                    @Member(id = "12000000", type = "area", role = "inner") }, tags = {
                                                            "type=multipolygon", "leisure=pitch",
                                                            "sport=soccer" }) })
    private Atlas relationNodeDuplicateFeature;

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = Node_LOCATION), tags = {
            "leisure=pitch", "sport=soccer" }) }, areas = {
                    @Area(id = "12000000", coordinates = { @Loc(value = AREA_LOCATION_ONE),
                            @Loc(value = AREA_LOCATION_TWO), @Loc(value = AREA_LOCATION_THREE),
                            @Loc(value = AREA_LOCATION_FOUR),
                            @Loc(value = AREA_LOCATION_ONE) }, tags = {
                                    "type=any" }) }, relations = {
                                            @Relation(id = "123", members = {
                                                    @Member(id = "12000000", type = "area", role = "any"), }, tags = {
                                                            "type=multipolygon", "leisure=pitch",
                                                            "sport=basketball" }) })
    private Atlas relationNodeNotDuplicateFeature;

    public Atlas getAreaNodeDuplicateFeature()
    {
        return this.areaNodeDuplicateFeature;
    }

    public Atlas getAreaNodeNotDuplicateFeature()
    {
        return this.areaNodeNotDuplicateFeature;
    }

    public Atlas getEdgeNodeDuplicateFeature()
    {
        return this.edgeNodeDuplicateFeature;
    }

    public Atlas getEdgeNodeNotDuplicateFeature()
    {
        return this.edgeNodeNotDuplicateFeature;
    }

    public Atlas getRelationEdgeDuplicateFeature()
    {
        return this.relationEdgeDuplicateFeature;
    }

    public Atlas getRelationEdgeNotDuplicateFeature()
    {
        return this.relationEdgeNotDuplicateFeature;
    }

    public Atlas getRelationNodeDuplicateFeature()
    {
        return this.relationNodeDuplicateFeature;
    }

    public Atlas getRelationNodeNotDuplicateFeature()
    {
        return this.relationNodeNotDuplicateFeature;
    }
}
