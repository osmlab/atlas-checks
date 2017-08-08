package org.openstreetmap.atlas.checks.maproulette.serializer;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author cuthbertm
 */
public class ChallengeDifficultySerializer
        implements JsonSerializer<ChallengeDifficulty>, JsonDeserializer<ChallengeDifficulty>
{
    @Override
    public ChallengeDifficulty deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final int key = json.getAsInt();
        return ChallengeDifficulty.fromValue(key);
    }

    @Override
    public JsonElement serialize(final ChallengeDifficulty src, final Type typeOfSrc,
            final JsonSerializationContext context)
    {
        return context.serialize(src.intValue());
    }
}
