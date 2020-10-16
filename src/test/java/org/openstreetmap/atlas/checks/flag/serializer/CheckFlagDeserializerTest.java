package org.openstreetmap.atlas.checks.flag.serializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.CheckFlagTest;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests if CheckFlag can be read from json resource
 *
 * @author danielbaah
 * @author bbreithaupt
 */
public class CheckFlagDeserializerTest
{
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CheckFlag.class, new CheckFlagDeserializer()).create();

    @Rule
    public CheckFlagDeserializerTestRule rule = new CheckFlagDeserializerTestRule();

    @Test
    public void allTypesDeserializationTest()
    {
        final Atlas atlas = this.rule.atlas();
        final CheckFlag flag = new CheckFlag("complex flag");
        flag.addObjects(Iterables.asSet(atlas.entities()));
        flag.addPoint(atlas.point(1000000L).getLocation());
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("generator", "NullCheck");
        contextualProperties.put("timestamp", new Date().toString());

        final CheckFlag deserializedFlag = gson
                .fromJson(CheckFlagEvent.flagToJson(flag, contextualProperties), CheckFlag.class);

        Assert.assertEquals(flag.getFlaggedObjects(), deserializedFlag.getFlaggedObjects());
    }

    @Test
    public void checkNameDeserializationTest() throws IOException
    {
        final String flag = this.getResource("checkflags2.log").get(0);
        final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
        final String checkName = checkFlag.getChallengeName().isPresent()
                ? checkFlag.getChallengeName().get()
                : null;
        Assert.assertEquals("BuildingRoadIntersectionCheck", checkName);
    }

    @Test
    public void deserializationTest() throws IOException
    {

        final List<String> flags = this.getResource("checkflags1.log");

        flags.forEach(flag ->
        {
            final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
            Assert.assertNotNull(checkFlag);
        });
    }

    @Test
    public void geometryChangeTest()
    {
        final Node node = this.rule.atlas().node(3000000L);
        final Edge edge1 = this.rule.atlas().edge(1000000L);
        final Edge edge2 = this.rule.atlas().edge(2000000L);
        final Edge edge3 = this.rule.atlas().edge(3000000L);
        final CheckFlag flag = new CheckFlag("100000020000003000000");
        flag.addObject(node);
        flag.addObject(edge1);
        flag.addObject(edge2);
        flag.addObject(edge3);
        flag.addFixSuggestion(FeatureChange.add(
                (AtlasEntity) CompleteNode.from(node)
                        .withGeometry(Collections.singleton(edge2.asPolyLine().get(1))),
                node.getAtlas()));
        flag.addFixSuggestion(
                FeatureChange.add(
                        (AtlasEntity) CompleteEdge.from(edge1)
                                .withGeometry(Arrays.asList(edge1.asPolyLine().first(),
                                        edge1.asPolyLine().last(), Location.COLOSSEUM)),
                        edge1.getAtlas()));
        flag.addFixSuggestion(FeatureChange.add((AtlasEntity) CompleteEdge.from(edge2)
                .withGeometry(Arrays.asList(edge2.asPolyLine().first(), Location.COLOSSEUM,
                        Location.EIFFEL_TOWER, Location.CENTER)),
                edge2.getAtlas()));
        flag.addFixSuggestion(FeatureChange.add(
                (AtlasEntity) CompleteEdge.from(edge3)
                        .withGeometry(Arrays.asList(edge3.asPolyLine().first(),
                                edge3.asPolyLine().last(), edge3.asPolyLine().get(1))),
                edge2.getAtlas()));
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("generator", "NullCheck");
        contextualProperties.put("timestamp", new Date().toString());

        final CheckFlag deserializedFlag = gson
                .fromJson(CheckFlagEvent.flagToJson(flag, contextualProperties), CheckFlag.class);

        Assert.assertEquals(4, deserializedFlag.getFixSuggestions().size());
        Assert.assertTrue(deserializedFlag.getFixSuggestions().stream()
                .allMatch(deserializedSuggestion -> flag.getFixSuggestions().stream()
                        .anyMatch(suggestion -> suggestion.getIdentifier() == deserializedSuggestion
                                .getIdentifier()
                                && suggestion.getBeforeView().toWkt()
                                        .equals(deserializedSuggestion.getBeforeView().toWkt())
                                && suggestion.getAfterView().toWkt()
                                        .equals(deserializedSuggestion.getAfterView().toWkt()))));

    }

    @Test
    public void identifierDeserializationTest() throws IOException
    {
        final String flag = this.getResource("checkflags3.log").get(0);
        final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
        Assert.assertEquals("522424754000000", checkFlag.getIdentifier());
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("Area522424754000000", "Edge-522211636000001")),
                checkFlag.getUniqueIdentifiers());
    }

    @Test
    public void instructionsDeserializationTest() throws IOException
    {
        final String flag = this.getResource("checkflags2.log").get(0);
        final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
        final String instructions = checkFlag.getInstructions();
        Assert.assertNotEquals(0, instructions.length());
    }

    @Test
    public void pointDeserializationTest()
    {
        final Point point = this.rule.atlas().point(1000000L);
        final CheckFlag pointFlag = new CheckFlag("1000000", Collections.singleton(point),
                Collections.singletonList("instruction"),
                Collections.singletonList(point.getLocation()), Collections.singleton(
                        FeatureChange.remove(CompletePoint.from(point), point.getAtlas())));
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("generator", "NullCheck");
        contextualProperties.put("timestamp", new Date().toString());

        final CheckFlag deserializedFlag = gson.fromJson(
                CheckFlagEvent.flagToJson(pointFlag, contextualProperties), CheckFlag.class);

        Assert.assertTrue(deserializedFlag.getChallengeName().isPresent());
        Assert.assertEquals("NullCheck", deserializedFlag.getChallengeName().get());
        Assert.assertEquals(pointFlag.getInstructions(), deserializedFlag.getInstructions());
        Assert.assertEquals(pointFlag.getFlaggedObjects(), deserializedFlag.getFlaggedObjects());
        Assert.assertTrue(deserializedFlag.getFixSuggestions().stream()
                .allMatch(suggestion -> suggestion.getChangeType().equals(ChangeType.REMOVE)
                        && suggestion.getIdentifier() == 1000000L));
    }

    @Test
    public void relationMemberChangeTest()
    {
        final Relation relation = this.rule.atlas().relation(1000000L);
        final Line line = this.rule.atlas().line(1000000L);
        final Area area = this.rule.atlas().area(1000000L);
        final CheckFlag flag = new CheckFlag("1000000");
        flag.addObject(relation);
        flag.addFixSuggestion(FeatureChange.add(CompleteRelation.from(relation)
                .withAddedMember(line, "part").withRemovedMember(area), relation.getAtlas()));
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("generator", "NullCheck");
        contextualProperties.put("timestamp", new Date().toString());

        final CheckFlag deserializedFlag = gson
                .fromJson(CheckFlagEvent.flagToJson(flag, contextualProperties), CheckFlag.class);

        Assert.assertEquals(1, deserializedFlag.getFixSuggestions().size());
        final FeatureChange suggestion = flag.getFixSuggestions().iterator().next();
        final FeatureChange deserializedSuggestion = deserializedFlag.getFixSuggestions().iterator()
                .next();
        Assert.assertEquals(((Relation) suggestion.getBeforeView()).members(),
                ((Relation) deserializedSuggestion.getBeforeView()).members());
        Assert.assertEquals(((Relation) suggestion.getAfterView()).members(),
                ((Relation) deserializedSuggestion.getAfterView()).members());
    }

    @Test
    public void tagChangeTest()
    {
        final Area area = this.rule.atlas().area(1000000L);
        final CheckFlag flag = new CheckFlag("1000000");
        flag.addObject(area);
        flag.addFixSuggestion(FeatureChange.add(
                CompleteArea.from(area).withRemovedTag(BuildingTag.KEY)
                        .withAddedTag(BuildingPartTag.KEY, BuildingPartTag.YES.toString())
                        .withReplacedTag(NameTag.KEY, NameTag.KEY, "Climate Pledge Arena"),
                area.getAtlas()));
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("generator", "NullCheck");
        contextualProperties.put("timestamp", new Date().toString());

        final CheckFlag deserializedFlag = gson
                .fromJson(CheckFlagEvent.flagToJson(flag, contextualProperties), CheckFlag.class);

        Assert.assertEquals(1, deserializedFlag.getFixSuggestions().size());
        final FeatureChange suggestion = flag.getFixSuggestions().iterator().next();
        final FeatureChange deserializedSuggestion = deserializedFlag.getFixSuggestions().iterator()
                .next();
        Assert.assertEquals(suggestion.getBeforeView().getTags(),
                deserializedSuggestion.getBeforeView().getTags());
        Assert.assertEquals(suggestion.getAfterView().getTags(),
                deserializedSuggestion.getAfterView().getTags());
    }

    /**
     * @param resource
     *            resource filename
     * @return A List of lines in .log file as string
     * @throws IOException
     *             IOException
     */
    private List<String> getResource(final String resource) throws IOException
    {
        final BufferedReader reader = new BufferedReader(
                new FileReader(CheckFlagTest.class.getResource(resource).getFile()));
        return reader.lines().collect(Collectors.toList());
    }
}
