package org.openstreetmap.atlas.checks.maproulette.data;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.maproulette.serializer.ChallengeDeserializer;
import org.openstreetmap.atlas.streaming.resource.ClassResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Tests whether a challenge can be read correctly from resources.
 *
 * @author cuthbertm
 */
public class ChallengeSerializationTest
{
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String BLURB = "BLURB";
    private static final String INSTRUCTION = "INSTRUCTION";
    private static final String CHECKIN_COMMENT = "#maproulette";

    /**
     * Tests that a challenge with no defaultPriority specified gets loaded as defaultPriority=LOW.
     */
    @Test
    public void serializationNoDefaultPrioritySpecified()
    {
        final Challenge deserializedChallenge = this.getChallenge("challenges/testChallenge4.json");

        Assert.assertEquals(DESCRIPTION, deserializedChallenge.getDescription());
        Assert.assertEquals(BLURB, deserializedChallenge.getBlurb());
        Assert.assertEquals(INSTRUCTION, deserializedChallenge.getInstruction());
        Assert.assertEquals(ChallengeDifficulty.NORMAL, deserializedChallenge.getDifficulty());
        Assert.assertEquals(ChallengePriority.LOW, deserializedChallenge.getDefaultPriority());
        Assert.assertNull(deserializedChallenge.getHighPriorityRule());
        Assert.assertNull(deserializedChallenge.getMediumPriorityRule());
        Assert.assertNull(deserializedChallenge.getLowPriorityRule());
    }

    /**
     * Test if a challange can be deserialized from a test JSON file. The challenge resource json
     * contains no MapRoulette priority information.
     */
    @Test
    public void serializationNoPriorityTest()
    {
        // This line will deserialize the challenge, and if it fails we know it didn't work.
        final Challenge deserializedChallenge = this.getChallenge("challenges/testChallenge2.json");
        Assert.assertEquals(DESCRIPTION, deserializedChallenge.getDescription());
        Assert.assertEquals(BLURB, deserializedChallenge.getBlurb());
        Assert.assertEquals(INSTRUCTION, deserializedChallenge.getInstruction());
        Assert.assertEquals(CHECKIN_COMMENT, deserializedChallenge.getCheckinComment());
        Assert.assertEquals(ChallengeDifficulty.NORMAL, deserializedChallenge.getDifficulty());
        Assert.assertEquals(ChallengePriority.NONE, deserializedChallenge.getDefaultPriority());
        Assert.assertNull(deserializedChallenge.getHighPriorityRule());
        Assert.assertNull(deserializedChallenge.getMediumPriorityRule());
        Assert.assertNull(deserializedChallenge.getLowPriorityRule());
    }

    /**
     * Test if a challenge can be deserialized from a test JSON file. The challenge contains
     * MapRoulette priority information for high and medium priority but not for low priority.
     */
    @Test
    public void serializationTest()
    {
        // This line will deserialize the challenge, and if it fails we know it didn't work.
        final Challenge deserializedChallenge = this.getChallenge("challenges/testChallenge.json");
        Assert.assertEquals(DESCRIPTION, deserializedChallenge.getDescription());
        Assert.assertEquals(BLURB, deserializedChallenge.getBlurb());
        Assert.assertEquals(INSTRUCTION, deserializedChallenge.getInstruction());
        Assert.assertEquals(CHECKIN_COMMENT, deserializedChallenge.getCheckinComment());
        Assert.assertEquals(ChallengeDifficulty.NORMAL, deserializedChallenge.getDifficulty());
        Assert.assertEquals(ChallengePriority.LOW, deserializedChallenge.getDefaultPriority());
        Assert.assertNotNull(deserializedChallenge.getHighPriorityRule());
        Assert.assertNotNull(deserializedChallenge.getMediumPriorityRule());
        Assert.assertNull(deserializedChallenge.getLowPriorityRule());
    }

    /**
     * Test if a challenge can be deserialized from a test JSON file with the json resource
     * containing a name for the challenge. And verifies that the name is set correctly.
     */
    @Test
    public void serializationWithNameTest()
    {
        // If stored json has name specified, it should keep it
        // Name provided to toGeoJsonFeatureCollection method should be ignored
        final Challenge deserializedChallenge = this.getChallenge("challenges/testChallenge3.json");

        final ClassResource challenge = new ClassResource("challenges/testChallenge3.json");
        final String raw = challenge.readAndClose();

        final JsonObject deserializedJson = deserializedChallenge.toJson("123456789");
        final JsonObject rawJson = getGson().fromJson(raw, JsonObject.class);

        Assert.assertEquals(deserializedJson.get("name"), rawJson.get("name"));
        Assert.assertEquals(deserializedJson.get("description"), rawJson.get("description"));
        Assert.assertEquals(deserializedJson.get("blurb"), rawJson.get("blurb"));
        Assert.assertEquals(deserializedJson.get("instruction"), rawJson.get("instruction"));
    }

    /**
     * Test if a challenge can be deserialized from a test JSON file with the json resource
     * containing no name for the challenge, instead the challenge name set from the code.
     */
    @Test
    public void serializationWithoutNameTest()
    {
        // If stored json does not have name specified, it should use the name provided to
        // toGeoJsonFeatureCollection

        final Challenge deserializedChallenge = this.getChallenge("challenges/testChallenge2.json");

        final ClassResource challenge = new ClassResource("challenges/testChallenge2.json");
        final String raw = challenge.readAndClose();

        final JsonObject deserializedJson = deserializedChallenge.toJson("123456789");
        final JsonObject rawJson = getGson().fromJson(raw, JsonObject.class);

        Assert.assertEquals("123456789", deserializedJson.get("name").getAsString());
        Assert.assertNull(rawJson.get("name"));
        Assert.assertEquals(deserializedJson.get("description"), rawJson.get("description"));
        Assert.assertEquals(deserializedJson.get("blurb"), rawJson.get("blurb"));
        Assert.assertEquals(deserializedJson.get("instruction"), rawJson.get("instruction"));
    }

    /**
     * Helper function that converts the resource file into a {@link Challenge}
     *
     * @param resource
     *            The path to the resource file
     * @return A {@link Challenge} object representing the provided resource.
     */
    private Challenge getChallenge(final String resource)
    {
        final ClassResource challengeResource = new ClassResource(resource);
        final JsonObject challengeJSON = challengeResource.getJSONResourceObject(JsonObject.class);
        return getGson().fromJson(challengeJSON, Challenge.class);
    }

    /**
     * Helper function to get a valid {@link Gson} component with the Challenge deserializer
     * registered as a type adapter.
     *
     * @return {@link Gson} object
     */
    private Gson getGson()
    {
        return new GsonBuilder().disableHtmlEscaping()
                .registerTypeAdapter(Challenge.class, new ChallengeDeserializer()).create();
    }
}
