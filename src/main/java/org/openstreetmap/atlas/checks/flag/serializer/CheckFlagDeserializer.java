package org.openstreetmap.atlas.checks.flag.serializer;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.checks.flag.CheckFlag;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Very simple deserializer for CheckFlag
 *
 * @author danielbaah
 */
public class CheckFlagDeserializer implements JsonDeserializer<CheckFlag>
{
    private static final String PROPERTIES = "properties";
    private static final String GENERATOR = "generator";
    private static final String INSTRUCTIONS = "instructions";
    private static final String ID = "id";

    public CheckFlagDeserializer()
    {
        // Default constructor
    }

    @Override
    public CheckFlag deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context)
    {
        final JsonObject full = json.getAsJsonObject();
        final JsonObject properties = full.get(PROPERTIES).getAsJsonObject();
        final String checkName = properties.get(GENERATOR).getAsString();
        final String instruction = properties.get(INSTRUCTIONS).getAsString();
        final String flagIdentifier = properties.get(ID).getAsString();
        final CheckFlag flag = new CheckFlag(flagIdentifier);
        flag.addInstruction(instruction);
        flag.setChallengeName(checkName);

        return flag;
    }
}
