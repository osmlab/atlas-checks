package org.openstreetmap.atlas.checks.maproulette.data.cooperative_challenge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorName;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Support geometry change operations
 *
 * @author Taylor Smock
 */
public class GeometryChangeOperation extends CooperativeChallengeOperation
{
    private static final String CONTENT = "content";
    private static final Map<String, String> FILE_MAP = new HashMap<>(3);
    static
    {
        FILE_MAP.put("type", "xml");
        FILE_MAP.put("format", "osc");
        FILE_MAP.put("encoding", "base64");
    }

    /** The OSC for the geometry change */
    private final String osc;

    public GeometryChangeOperation(final ChangeDescription changeDescription)
    {
        super(changeDescription);
        final var optionalOsc = changeDescription.getOsc();
        if (optionalOsc.isPresent())
        {
            this.osc = optionalOsc.get();
        }
        else if (changeDescription.toJsonElement().getAsJsonObject().has("osc"))
        {
            this.osc = changeDescription.toJsonElement().getAsJsonObject().get("osc").getAsString();
        }
        else
        {
            this.osc = null;
        }
    }

    @Override
    public GeometryChangeOperation create()
    {
        final var json = new JsonObject();
        if (this.osc != null && !this.osc.isBlank())
        {
            FILE_MAP.forEach((key, value) -> json.add(key, new JsonPrimitive(value)));
            json.add(CONTENT, new JsonPrimitive(this.osc));
        }
        this.setJson(json);
        return this;
    }

    @Override
    protected Predicate<ChangeDescriptor> operationFilter()
    {
        return changeDescriptor -> changeDescriptor.getName() == ChangeDescriptorName.GEOMETRY;
    }
}
