package org.openstreetmap.atlas.checks.maproulette.serializer;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengePriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author cuthbertm
 */
public class ChallengeDeserializer implements JsonDeserializer<Challenge>
{
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_BLURB = "blurb";
    public static final String KEY_INSTRUCTION = "instruction";
    public static final String KEY_DIFFICULTY = "difficulty";
    private static final Logger logger = LoggerFactory.getLogger(ChallengeDeserializer.class);

    @Override
    public Challenge deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final JsonObject challengeObject = json.getAsJsonObject();
        ChallengeDifficulty difficulty;
        try
        {
            difficulty = ChallengeDifficulty
                    .valueOf(this.getStringValue(challengeObject, KEY_DIFFICULTY, ""));
        }
        catch (final IllegalArgumentException e)
        {
            logger.trace("Failed to read difficulty value from Challenge JSON, defaulting to EASY");
            difficulty = ChallengeDifficulty.EASY;
        }

        ChallengePriority priority;
        try
        {
            priority = ChallengePriority
                    .valueOf(this.getStringValue(challengeObject, Challenge.KEY_DEFAULT_PRIORITY,
                            String.valueOf(Challenge.KEY_DEFAULT_PRIORITY_VALUE)));
        }
        catch (final IllegalArgumentException e)
        {
            logger.trace("Failed to read priority value from Challenge JSON, defaulting to LOW");
            priority = ChallengePriority.LOW;
        }

        return new Challenge(this.getStringValue(challengeObject, Challenge.KEY_NAME, ""),
                this.getStringValue(challengeObject, KEY_DESCRIPTION, ""),
                this.getStringValue(challengeObject, KEY_BLURB, ""),
                this.getStringValue(challengeObject, KEY_INSTRUCTION, ""), difficulty, priority,
                this.getValue(challengeObject, Challenge.KEY_HIGH_PRIORITY, null),
                this.getValue(challengeObject, Challenge.KEY_MEDIUM_PRIORITY, null),
                this.getValue(challengeObject, Challenge.KEY_LOW_PRIORITY, null),
                this.getStringValue(challengeObject, Challenge.KEY_TAGS, ""),
                this.getBooleanValue(challengeObject, Challenge.DISCOVERABLE, false));
    }

    private boolean getBooleanValue(final JsonObject object, final String key,
            final boolean defaultValue)
    {
        if (object.has(key))
        {
            return object.get(key).getAsBoolean();
        }
        else
        {
            return defaultValue;
        }
    }

    private String getStringValue(final JsonObject object, final String key,
            final String defaultValue)
    {
        if (object.has(key))
        {
            if (object.get(key) instanceof JsonArray)
            {
                try
                {
                    final StringBuilder stringBuilder = new StringBuilder();
                    final Gson googleJson = new Gson();
                    final ArrayList<String> jsonObjectList = googleJson
                            .fromJson(object.get(key).getAsJsonArray(), ArrayList.class);
                    for (final Object jsonObject : jsonObjectList)
                    {
                        stringBuilder.append(jsonObject).append(" ");
                    }
                    return stringBuilder.toString().trim();
                }
                catch (final Exception jsonException)
                {
                    logger.warn(String.format("Failed to process configuration key %s.", key),
                            jsonException);
                    return defaultValue;
                }
            }

            return object.get(key).getAsString();
        }
        else
        {
            return defaultValue;
        }
    }

    private String getValue(final JsonObject object, final String key, final String defaultValue)
    {
        if (object.has(key))
        {
            return object.get(key).toString();
        }
        else
        {
            return defaultValue;
        }
    }
}
