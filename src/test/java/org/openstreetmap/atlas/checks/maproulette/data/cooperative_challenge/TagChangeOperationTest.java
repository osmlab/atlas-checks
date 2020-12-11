package org.openstreetmap.atlas.checks.maproulette.data.cooperative_challenge;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.flag.CheckFlagTestRule;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

import com.google.gson.JsonObject;

/**
 * Tests {@link TagChangeOperation}
 *
 * @author seancoulter
 */
public class TagChangeOperationTest
{

    // Using a publicly accessible atlas to avoid unnecessary redefinition
    @Rule
    public final CheckFlagTestRule setup = new CheckFlagTestRule();

    @Test
    public void testCreationOfRemoveOperation()
    {
        final CompleteEdge edge = CompleteEdge.from(this.setup.getAtlas().edge(1L))
                .withAddedTag("random", "certainly");
        final ChangeDescription changeDescription = new ChangeDescription(1L, ItemType.EDGE, edge,
                edge.copy().withRemovedTag("random"), ChangeType.ADD);
        final TagChangeOperation tagChangeOperation = new TagChangeOperation(changeDescription)
                .create();
        final JsonObject json = tagChangeOperation.getJson();
        Assert.assertEquals("modifyElement", json.get("operationType").getAsString());
        Assert.assertEquals("way/1", json.get("data").getAsJsonObject().get("id").getAsString());
        Assert.assertEquals("unsetTags", json.get("data").getAsJsonObject().get("operations")
                .getAsJsonArray().get(0).getAsJsonObject().get("operation").getAsString());
        Assert.assertEquals("random", json.get("data").getAsJsonObject().get("operations")
                .getAsJsonArray().get(0).getAsJsonObject().get("data").getAsString());
    }

    // also covers case of ADD
    @Test
    public void testCreationOfUpdateOperation()
    {
        final CompleteEdge edge = CompleteEdge.from(this.setup.getAtlas().edge(1L));
        final ChangeDescription changeDescription = new ChangeDescription(1L, ItemType.EDGE, edge,
                edge.copy().withAddedTag("random", "probably"), ChangeType.ADD);
        final TagChangeOperation tagChangeOperation = new TagChangeOperation(changeDescription)
                .create();
        final JsonObject json = tagChangeOperation.getJson();
        Assert.assertEquals("modifyElement", json.get("operationType").getAsString());
        Assert.assertEquals("way/1", json.get("data").getAsJsonObject().get("id").getAsString());
        Assert.assertEquals("setTags", json.get("data").getAsJsonObject().get("operations")
                .getAsJsonArray().get(0).getAsJsonObject().get("operation").getAsString());
        Assert.assertEquals("probably",
                json.get("data").getAsJsonObject().get("operations").getAsJsonArray().get(0)
                        .getAsJsonObject().get("data").getAsJsonObject().get("random")
                        .getAsString());
    }

}
