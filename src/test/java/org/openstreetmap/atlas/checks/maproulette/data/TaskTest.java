package org.openstreetmap.atlas.checks.maproulette.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Test class for {@link Task}.
 *
 * @author Taylor Smock
 */
public class TaskTest
{
    @Rule
    public TaskTestRule rule = new TaskTestRule();

    /**
     * Create a basic task
     *
     * @param points
     *            The points
     * @return The created task
     */
    private static Task createTaskSkeleton(final Location... points)
    {
        return createTaskSkeleton(Stream.of(points).collect(Collectors.toSet()));
    }

    /**
     * Create a basic task
     *
     * @param points
     *            The points
     * @return The created task
     */
    private static Task createTaskSkeleton(final Set<Location> points)
    {
        final var task = new Task();
        // Need to see challenge/identifier to avoid NPE
        task.setChallengeName("Test challenge");
        task.setTaskIdentifier("Test identifier");

        task.setPoints(points);
        task.setInstruction("Test instruction");
        return task;
    }

    @Test
    public void generateTaskTest()
    {
        final var task = createTaskSkeleton(this.rule.getAtlas().point(1000000).getLocation());
        final var taskJson = task.generateTask(1);
        Assert.assertEquals("", taskJson.get("errorTags").getAsString());
    }

    /**
     * Test for the private inner class FixSuggestionToCooperativeWorkConverter (Geometry)
     */
    @Test
    public void testChangeDescriptionCreationGeometry()
    {
        final var line = this.rule.getAtlas().line(1000000L);
        final var linePoints = Iterables.asList(line.asPolyLine());
        Collections.reverse(linePoints);
        final var task = createTaskSkeleton(linePoints.get(0));
        task.setCooperativeWork(Collections.singleton(FeatureChange.add(
                (AtlasEntity) CompleteLine.shallowFrom(line).withGeometry(linePoints),
                line.getAtlas(), FeatureChange.Options.OSC_IF_POSSIBLE)));
        final var taskJson = task.generateTask(1);
        final var cooperativeWork = taskJson.getAsJsonObject("geometries")
                .getAsJsonObject("cooperativeWork");
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><modify><way action=\"modify\" id=\"1\" version=\"1\" visible=\"true\"><nd ref=\"2\"/><nd ref=\"1\"/></way></modify></osmChange>",
                new String(Base64.getDecoder().decode(cooperativeWork.getAsJsonObject("file")
                        .getAsJsonPrimitive("content").getAsString())));
        assertEquals(2, cooperativeWork.getAsJsonObject("meta").get("type").getAsByte());
    }

    @Test
    public void testChangeDescriptionCreationTag()
    {
        final var line = this.rule.getAtlas().line(1000000L);
        final var linePoints = Iterables.asList(line.asPolyLine());
        Collections.reverse(linePoints);
        final var task = createTaskSkeleton(linePoints.get(0));
        task.setCooperativeWork(Collections.singleton(
                FeatureChange.add(CompleteLine.shallowFrom(line).withAddedTag("test", "tag"),
                        line.getAtlas(), FeatureChange.Options.OSC_IF_POSSIBLE)));
        final var taskJson = task.generateTask(1);
        final var cooperativeWork = taskJson.getAsJsonObject("geometries")
                .getAsJsonObject("cooperativeWork");
        assertEquals(1, cooperativeWork.getAsJsonObject("meta").get("type").getAsByte());
        final var operations = cooperativeWork.getAsJsonArray("operations");
        assertEquals(1, operations.size());
        final var operation = operations.get(0).getAsJsonObject().getAsJsonObject("data")
                .getAsJsonArray("operations").get(0).getAsJsonObject();
        assertEquals("setTags", operation.get("operation").getAsString());
        final var entries = operation.getAsJsonObject("data").entrySet();
        assertEquals(1, entries.size());
        assertEquals("test", entries.iterator().next().getKey());
        assertEquals("tag", entries.iterator().next().getValue().getAsString());
    }
}
