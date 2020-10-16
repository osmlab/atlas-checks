package org.openstreetmap.atlas.checks.event;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorName;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.names.NameTag;

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link CheckFlagEvent}.
 *
 * @author bbreithaupt
 */
public class CheckFlagEventTest
{

    @Rule
    public final CheckFlagEventTestRule rule = new CheckFlagEventTestRule();

    @Test
    public void flagToFeatureAddTagFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToFeature(
                new CheckFlag("1").addFixSuggestion(FeatureChange.add(
                        CompleteNode.from(atlas.node(1)).withAddedTag(NameTag.KEY, "n"), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.get("properties").getAsJsonObject().has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("properties").getAsJsonObject()
                .get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.TAG.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.ADD.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(NameTag.KEY, firstDescriptor.get("key").getAsString());
        Assert.assertEquals("n", firstDescriptor.get("value").getAsString());
    }

    @Test
    public void flagToFeatureRemoveFeatureFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToFeature(
                new CheckFlag("1").addFixSuggestion(
                        FeatureChange.remove(CompleteNode.from(atlas.node(1)), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.get("properties").getAsJsonObject().has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("properties").getAsJsonObject()
                .get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(0, node1.get("descriptors").getAsJsonArray().size());
    }

    @Test
    public void flagToFeatureRemoveTagFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToFeature(
                new CheckFlag("1").addFixSuggestion(FeatureChange
                        .add(CompleteNode.from(atlas.node(1)).withRemovedTag(LayerTag.KEY), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.get("properties").getAsJsonObject().has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("properties").getAsJsonObject()
                .get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.TAG.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.REMOVE.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(LayerTag.KEY, firstDescriptor.get("key").getAsString());
        Assert.assertEquals("1", firstDescriptor.get("value").getAsString());
    }

    @Test
    public void flagToFeatureUpdateGeometryFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent
                .flagToFeature(
                        new CheckFlag("1")
                                .addFixSuggestion(
                                        FeatureChange.add(
                                                (AtlasEntity) CompleteNode.from(atlas.node(1))
                                                        .withGeometry(Collections
                                                                .singleton(Location.CENTER)),
                                                atlas)),
                        Collections.emptyMap());

        Assert.assertTrue(flagJson.get("properties").getAsJsonObject().has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("properties").getAsJsonObject()
                .get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.GEOMETRY.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.UPDATE.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(Location.CENTER.toWkt(),
                firstDescriptor.get("afterView").getAsString());
    }

    @Test
    public void flagToFeatureUpdateRelationMemberRoleFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToFeature(
                new CheckFlag("1").addFixSuggestion(
                        FeatureChange.add(CompleteRelation.from(atlas.relation(123))
                                .changeMemberRole(atlas.node(1), "still_not_real"), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.get("properties").getAsJsonObject().has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("properties").getAsJsonObject()
                .get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Relation123"));
        final JsonObject node1 = fixSuggestions.get("Relation123").getAsJsonObject();
        Assert.assertEquals(2, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.RELATION_MEMBER.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.ADD.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals("still_not_real", firstDescriptor.get("role").getAsString());
        final JsonObject secondDescriptor = node1.get("descriptors").getAsJsonArray().get(1)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.RELATION_MEMBER.toString(),
                secondDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.REMOVE.toString(),
                secondDescriptor.get("type").getAsString());
        Assert.assertEquals("not_real", secondDescriptor.get("role").getAsString());
    }

    @Test
    public void flagToFeatureUpdateTagFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToFeature(
                new CheckFlag("1")
                        .addFixSuggestion(FeatureChange.add(CompleteNode.from(atlas.node(1))
                                .withReplacedTag(LayerTag.KEY, LayerTag.KEY, "2"), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.get("properties").getAsJsonObject().has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("properties").getAsJsonObject()
                .get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.TAG.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.UPDATE.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(LayerTag.KEY, firstDescriptor.get("key").getAsString());
        Assert.assertEquals("2", firstDescriptor.get("value").getAsString());
        Assert.assertEquals("1", firstDescriptor.get("originalValue").getAsString());
    }

    @Test
    public void flagToJsonAddTagFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToJson(
                new CheckFlag("1").addFixSuggestion(FeatureChange.add(
                        CompleteNode.from(atlas.node(1)).withAddedTag(NameTag.KEY, "n"), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.TAG.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.ADD.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(NameTag.KEY, firstDescriptor.get("key").getAsString());
        Assert.assertEquals("n", firstDescriptor.get("value").getAsString());
    }

    @Test
    public void flagToJsonRemoveTagFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToJson(
                new CheckFlag("1").addFixSuggestion(FeatureChange
                        .add(CompleteNode.from(atlas.node(1)).withRemovedTag(LayerTag.KEY), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.TAG.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.REMOVE.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(LayerTag.KEY, firstDescriptor.get("key").getAsString());
        Assert.assertEquals("1", firstDescriptor.get("value").getAsString());
    }

    @Test
    public void flagToJsonUpdateGeometryFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent
                .flagToJson(
                        new CheckFlag("1")
                                .addFixSuggestion(
                                        FeatureChange.add(
                                                (AtlasEntity) CompleteNode.from(atlas.node(1))
                                                        .withGeometry(Collections
                                                                .singleton(Location.CENTER)),
                                                atlas)),
                        Collections.emptyMap());

        Assert.assertTrue(flagJson.has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.GEOMETRY.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.UPDATE.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(Location.CENTER.toWkt(),
                firstDescriptor.get("afterView").getAsString());
    }

    @Test
    public void flagToJsonUpdateRelationMemberRoleFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToJson(
                new CheckFlag("1").addFixSuggestion(
                        FeatureChange.add(CompleteRelation.from(atlas.relation(123))
                                .changeMemberRole(atlas.node(1), "still_not_real"), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Relation123"));
        final JsonObject node1 = fixSuggestions.get("Relation123").getAsJsonObject();
        Assert.assertEquals(2, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.RELATION_MEMBER.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.ADD.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals("still_not_real", firstDescriptor.get("role").getAsString());
        final JsonObject secondDescriptor = node1.get("descriptors").getAsJsonArray().get(1)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.RELATION_MEMBER.toString(),
                secondDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.REMOVE.toString(),
                secondDescriptor.get("type").getAsString());
        Assert.assertEquals("not_real", secondDescriptor.get("role").getAsString());
    }

    @Test
    public void flagToJsonUpdateTagFixSuggestionTest()
    {
        final Atlas atlas = this.rule.getAtlas();
        final JsonObject flagJson = CheckFlagEvent.flagToJson(
                new CheckFlag("1")
                        .addFixSuggestion(FeatureChange.add(CompleteNode.from(atlas.node(1))
                                .withReplacedTag(LayerTag.KEY, LayerTag.KEY, "2"), atlas)),
                Collections.emptyMap());

        Assert.assertTrue(flagJson.has("fix_suggestions"));
        final JsonObject fixSuggestions = flagJson.get("fix_suggestions").getAsJsonObject();
        Assert.assertTrue(fixSuggestions.has("Node1"));
        final JsonObject node1 = fixSuggestions.get("Node1").getAsJsonObject();
        Assert.assertEquals(1, node1.get("descriptors").getAsJsonArray().size());
        final JsonObject firstDescriptor = node1.get("descriptors").getAsJsonArray().get(0)
                .getAsJsonObject();
        Assert.assertEquals(ChangeDescriptorName.TAG.toString(),
                firstDescriptor.get("name").getAsString());
        Assert.assertEquals(ChangeDescriptorType.UPDATE.toString(),
                firstDescriptor.get("type").getAsString());
        Assert.assertEquals(LayerTag.KEY, firstDescriptor.get("key").getAsString());
        Assert.assertEquals("2", firstDescriptor.get("value").getAsString());
        Assert.assertEquals("1", firstDescriptor.get("originalValue").getAsString());
    }
}
