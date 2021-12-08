package org.openstreetmap.atlas.checks.maproulette.data.cooperative_challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

import com.google.gson.JsonObject;

/**
 * Test class for {@link GeometryChangeOperationTestRule}
 *
 * @author Taylor Smock
 */
public class GeometryChangeOperationTest
{
    private static final String EDGE_ATLAS_OSC = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><osmChange generator=\"atlas ChangeDescription v0.0.1\" version=\"0.6\"><modify><way action=\"modify\" id=\"-1\" version=\"1\" visible=\"true\"><tag k=\"random\" v=\"probably\"/><nd ref=\"2\"/><nd ref=\"1\"/></way></modify></osmChange>";
    @Rule
    public final GeometryChangeOperationTestRule setup = new GeometryChangeOperationTestRule();

    /**
     * Check that an OSC is generated and/or returned properly
     */
    @Test
    public void testCreationOfOscTask()
    {
        final CompleteEdge edge = CompleteEdge.from(this.setup.getEdgeAtlas().edge(1L));
        final List<Location> locationCollection = new ArrayList<>(edge.asPolyLine());
        Collections.reverse(locationCollection);
        final FeatureChange featureChange = FeatureChange.add(
                (AtlasEntity) CompleteEdge.shallowFrom(edge).withGeometry(locationCollection)
                        .withAddedTag("random", "probably"),
                this.setup.getEdgeAtlas(), FeatureChange.Options.OSC_IF_POSSIBLE);
        final GeometryChangeOperation geometryChangeOperation = new GeometryChangeOperation(
                featureChange.explain()).create();
        final JsonObject json = geometryChangeOperation.getJson();

        assertEquals("xml", json.get("type").getAsString());
        assertEquals("osc", json.get("format").getAsString());
        assertEquals("base64", json.get("encoding").getAsString());
        // Decode the content so that if it breaks, a human can see what changed between the
        // expected osm change and the actual osm change
        assertEquals(EDGE_ATLAS_OSC,
                new String(Base64.getDecoder().decode(json.get("content").getAsString())));
    }

    /**
     * This check ensures that an empty OSC is reflected in the generated json
     */
    @Test
    public void testEmptyOscTask()
    {
        final CompleteEdge edge = CompleteEdge.from(this.setup.getEdgeAtlas().edge(1L));
        final FeatureChange featureChange = FeatureChange.add(
                CompleteEdge.shallowFrom(edge).withAddedTag("random", "probably"),
                this.setup.getEdgeAtlas(), FeatureChange.Options.OSC_IF_POSSIBLE);
        featureChange.withOsc("");
        final GeometryChangeOperation geometryChangeOperation = new GeometryChangeOperation(
                featureChange.explain()).create();
        final JsonObject json = geometryChangeOperation.getJson();

        assertTrue(json.entrySet().isEmpty());
    }

    /**
     * This check ensures that there isn't a crash due to a missing "osc" entry
     */
    @Test
    public void testNoCreationOfOscTask()
    {
        final CompleteEdge edge = CompleteEdge.from(this.setup.getEdgeAtlas().edge(1L));
        final FeatureChange featureChange = FeatureChange.add(
                CompleteEdge.shallowFrom(edge).withAddedTag("random", "probably"),
                this.setup.getEdgeAtlas(), FeatureChange.Options.OSC_IF_POSSIBLE);
        final GeometryChangeOperation geometryChangeOperation = new GeometryChangeOperation(
                featureChange.explain()).create();
        final JsonObject json = geometryChangeOperation.getJson();

        assertTrue(json.entrySet().isEmpty(), json.toString());
    }

    /**
     * This check ensures that there isn't a crash due to a missing "osc" entry
     */
    @Test
    public void testSetOfOscTask()
    {
        final CompleteEdge edge = CompleteEdge.from(this.setup.getEdgeAtlas().edge(1L));
        final FeatureChange featureChange = FeatureChange.add(
                CompleteEdge.shallowFrom(edge).withAddedTag("random", "probably"),
                this.setup.getEdgeAtlas(), FeatureChange.Options.OSC_IF_POSSIBLE);
        featureChange.withOsc(Base64.getEncoder()
                .encodeToString(EDGE_ATLAS_OSC.getBytes(StandardCharsets.UTF_8)));
        final GeometryChangeOperation geometryChangeOperation = new GeometryChangeOperation(
                featureChange.explain()).create();
        final JsonObject json = geometryChangeOperation.getJson();

        assertEquals("xml", json.get("type").getAsString());
        assertEquals("osc", json.get("format").getAsString());
        assertEquals("base64", json.get("encoding").getAsString());
        // Decode the content so that if it breaks, a human can see what changed between the
        // expected osm change and the actual osm change
        assertEquals(EDGE_ATLAS_OSC,
                new String(Base64.getDecoder().decode(json.get("content").getAsString())));
    }
}
