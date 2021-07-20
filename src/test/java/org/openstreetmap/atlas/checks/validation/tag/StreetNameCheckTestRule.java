package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Tests for {@link StreetNameCheck}
 *
 * @author v-naydinyan
 */
public class StreetNameCheckTestRule extends CoreTestRule
{

    private static final String WAY1_NODE1 = "40.9130354, 29.4700719";
    private static final String WAY1_NODE2 = "40.9123887, 29.4698597";

    @TestAtlas(nodes = { @Node(id = "100001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "100002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=AUT", "name=Peter-Strasser-Platz" }) }, edges = {
                            @Edge(id = "100003", coordinates = { @Loc(value = WAY1_NODE1),
                                    @Loc(value = WAY1_NODE2) }) })
    private Atlas falsePositiveAutNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "200001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "200002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "200003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "200004", members = {
                                            @Member(id = "200002", type = "node", role = "any"),
                                            @Member(id = "200003", type = "edge", role = "any") }, tags = {
                                                    "iso_country_code=AUT",
                                                    "type=associatedStreet" }) })
    private Atlas falsePositiveAutRelationDeprecatedTag;

    @TestAtlas(nodes = { @Node(id = "300001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "300002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "300003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "300004", members = {
                                            @Member(id = "300001", type = "node", role = "any"),
                                            @Member(id = "300003", type = "edge", role = "any") }, tags = {
                                                    "iso_country_code=AUT",
                                                    "name=Peter-Strasser-Platz" }) })
    private Atlas falsePositiveAutRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "400001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "400002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "400003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=AUT",
                                    "name=Peter-Strasser-Platz" }) })
    private Atlas falsePositiveAutWayInvalidValue;

    @TestAtlas(nodes = { @Node(id = "500001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "500002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=CHE", "name=Hauptstrasse" }) }, edges = {
                            @Edge(id = "500003", coordinates = { @Loc(value = WAY1_NODE1),
                                    @Loc(value = WAY1_NODE2) }) })
    private Atlas falsePositiveCheNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "600001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "600002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "600003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "600004", members = {
                                            @Member(id = "600003", type = "edge", role = "any"),
                                            @Member(id = "600002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=CHE",
                                                    "type=associatedStreet" }) })
    private Atlas falsePositiveCheRelationDeprecatedTag;

    @TestAtlas(nodes = { @Node(id = "700001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "700002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "700003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "700004", members = {
                                            @Member(id = "700003", type = "edge", role = "any"),
                                            @Member(id = "700002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=CHE",
                                                    "name=Hauptstrasse" }) })
    private Atlas falsePositiveCheRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "800001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "800002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "800003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=CHE",
                                    "name=Hauptstrasse" }) })
    private Atlas falsePositiveCheWayInvalidValue;

    @TestAtlas(nodes = { @Node(id = "900001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "900002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=DEU", "name=Peter-Strasser-Platz" }) }, edges = {
                            @Edge(id = "900003", coordinates = { @Loc(value = WAY1_NODE1),
                                    @Loc(value = WAY1_NODE2) }) })
    private Atlas falsePositiveDeuNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1000001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1000002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1000003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "1000004", members = {
                                            @Member(id = "1000003", type = "edge", role = "any"),
                                            @Member(id = "1000002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=DEU",
                                                    "type=associatedStreet" }) })
    private Atlas falsePositiveDeuRelationDeprecatedTag;

    @TestAtlas(nodes = { @Node(id = "1100001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1100002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1100003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "1100004", members = {
                                            @Member(id = "1100003", type = "edge", role = "any"),
                                            @Member(id = "1100002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=DEU",
                                                    "name=Peter-Strasser-Platz" }) })
    private Atlas falsePositiveDeuRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1200001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1200002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1200003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=DEU",
                                    "name=Peter-Strasser-Platz" }) })
    private Atlas falsePositiveDeuWayInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1300001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1300002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=LIE", "name=Hauptstrasse" }) }, edges = {
                            @Edge(id = "1300003", coordinates = { @Loc(value = WAY1_NODE1),
                                    @Loc(value = WAY1_NODE2) }) })
    private Atlas falsePositiveLieNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1400001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1400002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1400003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "1400004", members = {
                                            @Member(id = "1400003", type = "edge", role = "any"),
                                            @Member(id = "1400002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=LIE",
                                                    "type=associatedStreet" }) })
    private Atlas falsePositiveLieRelationDeprecatedTag;

    @TestAtlas(nodes = { @Node(id = "1500001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1500002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1500003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "1500004", members = {
                                            @Member(id = "1500003", type = "edge", role = "any"),
                                            @Member(id = "1500002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=LIE",
                                                    "name=Hauptstrasse" }) })
    private Atlas falsePositiveLieRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1600001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1600002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1600002", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=LIE",
                                    "name=Hauptstrasse" }) })
    private Atlas falsePositiveLieWayInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1700001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1700002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=AUT", "name=Hauptstrasse" }) }, edges = {
                            @Edge(id = "1700003", coordinates = { @Loc(value = WAY1_NODE1),
                                    @Loc(value = WAY1_NODE2) }) })
    private Atlas truePositiveAutNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1800001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1800002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1800003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "1800004", members = {
                                            @Member(id = "1800003", type = "edge", role = "any"),
                                            @Member(id = "1800002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=AUT",
                                                    "name=Hauptstrasse" }) })
    private Atlas truePositiveAutRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "1900001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "1900002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "1900003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=AUT",
                                    "name=Hauptstrasse" }) })
    private Atlas truePositiveAutWayInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2000001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2000002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=CHE", "name=Hauptstra\u00dfe" // NOSONAR
            }) }, edges = { @Edge(id = "2000003", coordinates = { @Loc(value = WAY1_NODE1),
                    @Loc(value = WAY1_NODE2) }) })
    private Atlas truePositiveCheNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2100001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2100002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "2100003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "2100004", members = {
                                            @Member(id = "2100003", type = "edge", role = "any"),
                                            @Member(id = "2100002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=CHE", "name=Hauptstra\u00dfe" // NOSONAR
                                    }) })
    private Atlas truePositiveCheRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2200001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2200002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "2200003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=CHE",
                                    "name=Hauptstra\u00dfe" // NOSONAR
                    }) })
    private Atlas truePositiveCheWayInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2300001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2300002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=DEU", "name=Hauptstrasse" }) }, edges = {
                            @Edge(id = "2300003", coordinates = { @Loc(value = WAY1_NODE1),
                                    @Loc(value = WAY1_NODE2) }) })
    private Atlas truePositiveDeuNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2400001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2400002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "2400003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "2400004", members = {
                                            @Member(id = "2400003", type = "edge", role = "any"),
                                            @Member(id = "2400002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=DEU",
                                                    "type=associatedStreet" }) })
    private Atlas truePositiveDeuRelationDeprecatedTag;

    @TestAtlas(nodes = { @Node(id = "2500001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2500002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "2500003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "2500004", members = {
                                            @Member(id = "2500003", type = "edge", role = "any"),
                                            @Member(id = "2500002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=DEU",
                                                    "name=Hauptstrasse" }) })
    private Atlas truePositiveDeuRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2600001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2600002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "2600003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=DEU",
                                    "name=Hauptstrasse" }) })
    private Atlas truePositiveDeuWayInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2700001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2700002", coordinates = @Loc(value = WAY1_NODE2), tags = {
                    "iso_country_code=LIE", "name=Hauptstra\u00dfe" // NOSONAR
            }) }, edges = { @Edge(id = "2700003", coordinates = { @Loc(value = WAY1_NODE1),
                    @Loc(value = WAY1_NODE2) }) })
    private Atlas truePositiveLieNodeInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2800001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2800002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "2800003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) }, relations = {
                                    @Relation(id = "2800004", members = {
                                            @Member(id = "2800003", type = "edge", role = "any"),
                                            @Member(id = "2800002", type = "node", role = "any") }, tags = {
                                                    "iso_country_code=LIE", "name=Hauptstra\u00dfe" // NOSONAR
                                    }) })
    private Atlas truePositiveLieRelationInvalidValue;

    @TestAtlas(nodes = { @Node(id = "2900001", coordinates = @Loc(value = WAY1_NODE1)),
            @Node(id = "2900002", coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "2900003", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "iso_country_code=LIE",
                                    "name=Hauptstra\u00dfe" // NOSONAR
                    }) })
    private Atlas truePositiveLieWayInvalidValue;

    public Atlas falsePositiveAutNodeInvalidValue()
    {
        return this.falsePositiveAutNodeInvalidValue;
    }

    public Atlas falsePositiveAutRelationDeprecatedTag()
    {
        return this.falsePositiveAutRelationDeprecatedTag;
    }

    public Atlas falsePositiveAutRelationInvalidValue()
    {
        return this.falsePositiveAutRelationInvalidValue;
    }

    public Atlas falsePositiveAutWayInvalidTag()
    {
        return this.falsePositiveAutWayInvalidValue;
    }

    public Atlas falsePositiveCheNodeInvalidValue()
    {
        return this.falsePositiveCheNodeInvalidValue;
    }

    public Atlas falsePositiveCheRelationDeprecatedTag()
    {
        return this.falsePositiveCheRelationDeprecatedTag;
    }

    public Atlas falsePositiveCheRelationInvalidValue()
    {
        return this.falsePositiveCheRelationInvalidValue;
    }

    public Atlas falsePositiveCheWayInvalidValue()
    {
        return this.falsePositiveCheWayInvalidValue;
    }

    public Atlas falsePositiveDeuNodeInvalidValue()
    {
        return this.falsePositiveDeuNodeInvalidValue;
    }

    public Atlas falsePositiveDeuRelationDeprecatedTag()
    {
        return this.falsePositiveDeuNodeInvalidValue;
    }

    public Atlas falsePositiveDeuRelationInvalidValue()
    {
        return this.falsePositiveDeuRelationInvalidValue;
    }

    public Atlas falsePositiveDeuWayInvalidValue()
    {
        return this.falsePositiveDeuWayInvalidValue;
    }

    public Atlas falsePositiveLieNodeInvalidValue()
    {
        return this.falsePositiveLieNodeInvalidValue;
    }

    public Atlas falsePositiveLieRelationDeprecatedTag()
    {
        return this.falsePositiveLieRelationDeprecatedTag;
    }

    public Atlas falsePositiveLieRelationInvalidValue()
    {
        return this.falsePositiveLieRelationInvalidValue;
    }

    public Atlas falsePositiveLieWayInvalidValue()
    {
        return this.falsePositiveLieWayInvalidValue;
    }

    public Atlas truePositiveAutNodeInvalidValue()
    {
        return this.truePositiveAutNodeInvalidValue;
    }

    public Atlas truePositiveAutRelationInvalidValue()
    {
        return this.truePositiveAutRelationInvalidValue;
    }

    public Atlas truePositiveAutWayInvalidValue()
    {
        return this.truePositiveAutWayInvalidValue;
    }

    public Atlas truePositiveCheNodeInvalidValue()
    {
        return this.truePositiveCheNodeInvalidValue;
    }

    public Atlas truePositiveCheRelationInvalidValue()
    {
        return this.truePositiveCheRelationInvalidValue;
    }

    public Atlas truePositiveCheWayInvalidValue()
    {
        return this.truePositiveCheWayInvalidValue;
    }

    public Atlas truePositiveDeuNodeInvalidValue()
    {
        return this.truePositiveDeuNodeInvalidValue;
    }

    public Atlas truePositiveDeuRelationDeprecatedTag()
    {
        return this.truePositiveDeuRelationDeprecatedTag;
    }

    public Atlas truePositiveDeuRelationInvalidValue()
    {
        return this.truePositiveDeuRelationInvalidValue;
    }

    public Atlas truePositiveDeuWayInvalidValue()
    {
        return this.truePositiveDeuWayInvalidValue;
    }

    public Atlas truePositiveLieNodeInvalidValue()
    {
        return this.truePositiveLieNodeInvalidValue;
    }

    public Atlas truePositiveLieRelationInvalidValue()
    {
        return this.truePositiveLieRelationInvalidValue;
    }

    public Atlas truePositiveLieWayInvalidValue()
    {
        return this.truePositiveLieWayInvalidValue;
    }
}
