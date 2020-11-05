package org.openstreetmap.atlas.checks.utility;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlaggedObject;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
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
import org.openstreetmap.atlas.tags.names.NameTag;

import com.google.common.collect.Iterables;

/**
 * Unit tests for {@link OpenStreetMapCheckFlagConverter}.
 *
 * @author bbreithaupt
 */
public class OpenStreetMapCheckFlagConverterTest
{
    @Rule
    public final OpenStreetMapCheckFlagConverterTestRule rule = new OpenStreetMapCheckFlagConverterTestRule();

    @Test
    public void edgeTest()
    {
        final CheckFlag flag = new CheckFlag("1");
        final Edge edge1 = this.rule.atlas().edge(1000001L);
        final Edge edge2 = this.rule.atlas().edge(1000002L);
        flag.addObject(edge1);
        flag.addObject(edge2);
        flag.addFixSuggestion(
                FeatureChange.add(
                        (AtlasEntity) CompleteEdge.from(edge1)
                                .withGeometry(Arrays.asList(edge1.asPolyLine().first(),
                                        Location.CENTER, edge1.asPolyLine().last())),
                        edge1.getAtlas()));
        flag.addFixSuggestion(
                FeatureChange.add(
                        (AtlasEntity) CompleteEdge.from(edge2)
                                .withGeometry(Arrays.asList(edge2.asPolyLine().first(),
                                        Location.COLOSSEUM, edge2.asPolyLine().last())),
                        edge2.getAtlas()));

        final Optional<CheckFlag> osmFlag = OpenStreetMapCheckFlagConverter.openStreetMapify(flag);

        Assert.assertTrue(osmFlag.isPresent());
        Assert.assertEquals(1, osmFlag.get().getFlaggedObjects().size());
        Assert.assertEquals(1, osmFlag.get().getFixSuggestions().size());
        Assert.assertEquals(5,
                Iterables.size(osmFlag.get().getFlaggedObjects().iterator().next().getGeometry()));
        Assert.assertEquals(5,
                ((Edge) osmFlag.get().getFixSuggestions().iterator().next().getBeforeView())
                        .asPolyLine().size());
        Assert.assertEquals(5,
                ((Edge) osmFlag.get().getFixSuggestions().iterator().next().getAfterView())
                        .asPolyLine().size());
        Assert.assertEquals(osmFlag.get().getFlaggedObjects().iterator().next().getGeometry(),
                ((Edge) osmFlag.get().getFixSuggestions().iterator().next().getBeforeView())
                        .asPolyLine());
        Assert.assertTrue(
                ((Edge) osmFlag.get().getFixSuggestions().iterator().next().getAfterView())
                        .asPolyLine().contains(Location.CENTER));
        Assert.assertTrue(
                ((Edge) osmFlag.get().getFixSuggestions().iterator().next().getAfterView())
                        .asPolyLine().contains(Location.COLOSSEUM));
    }

    @Test
    public void noChangeTest()
    {
        final CheckFlag flag = new CheckFlag("1");
        final Area area = this.rule.atlas().area(1000000L);
        final Line line = this.rule.atlas().line(1000000L);
        final Point point = this.rule.atlas().point(1000000L);
        final Relation relation = this.rule.atlas().relation(1000000L);
        flag.addObject(area);
        flag.addObject(line);
        flag.addObject(point);
        flag.addObject(relation);
        flag.addFixSuggestion(FeatureChange.add(
                CompleteArea.from(area).withReplacedTag(NameTag.KEY, NameTag.KEY, "name"),
                area.getAtlas()));
        flag.addFixSuggestion(FeatureChange
                .add(CompleteLine.from(line).withAddedTag(NameTag.KEY, "name1"), line.getAtlas()));
        flag.addFixSuggestion(FeatureChange.add(
                CompletePoint.from(point).withAddedTag(NameTag.KEY, "name2"), point.getAtlas()));
        flag.addFixSuggestion(FeatureChange.add(
                CompleteRelation.from(relation).withReplacedTag(NameTag.KEY, NameTag.KEY, "name3"),
                relation.getAtlas()));

        final Optional<CheckFlag> osmFlag = OpenStreetMapCheckFlagConverter.openStreetMapify(flag);

        Assert.assertTrue(osmFlag.isPresent());
        Assert.assertEquals(flag, osmFlag.get());
    }

    @Test
    public void nodePointTest()
    {
        final CheckFlag flag = new CheckFlag("1");
        final Node node = this.rule.atlas().node(1000000L);
        final Point point1 = this.rule.atlas().point(1000000L);
        final Point point2 = this.rule.atlas().point(2000000L);
        flag.addObject(node);
        flag.addObject(point1);
        flag.addObject(point2);
        flag.addFixSuggestion(FeatureChange
                .add(CompleteNode.from(node).withAddedTag(NameTag.KEY, "name"), node.getAtlas()));
        flag.addFixSuggestion(FeatureChange.add(
                CompletePoint.from(point1).withAddedTag(NameTag.KEY, "name1"), point1.getAtlas()));
        flag.addFixSuggestion(FeatureChange.add(
                CompletePoint.from(point2).withAddedTag(NameTag.KEY, "name2"), point2.getAtlas()));

        final Optional<CheckFlag> osmFlag = OpenStreetMapCheckFlagConverter.openStreetMapify(flag);

        Assert.assertTrue(osmFlag.isPresent());
        Assert.assertEquals(1, osmFlag.get().getFlaggedObjects().stream().filter(
                object -> object.getProperties().get(FlaggedObject.ITEM_TYPE_TAG).equals("Node"))
                .count());
        Assert.assertEquals(1, osmFlag.get().getFlaggedObjects().stream().filter(
                object -> object.getProperties().get(FlaggedObject.ITEM_TYPE_TAG).equals("Point"))
                .count());
    }
}
