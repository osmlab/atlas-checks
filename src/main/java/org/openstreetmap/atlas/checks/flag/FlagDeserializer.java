package org.openstreetmap.atlas.checks.flag;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Deserializes a line from a line-delimited geojson log file into into a Task object, given a
 * particular project name.
 *
 * @author nachtm
 */
public class FlagDeserializer implements JsonDeserializer<CheckFlag>
{
    private static final String PROPERTIES = "properties";
    private static final String GENERATOR = "generator";
    private static final String INSTRUCTIONS = "instructions";
    private static final String ID = "id";
    private static final String FEATURES = "features";
    private static final String GEOMETRY = "geometry";
    private static final String COORDINATES = "coordinates";

    public FlagDeserializer()
    {

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
        final JsonArray features = full.get(FEATURES).getAsJsonArray();
        final CheckFlag flag = new CheckFlag(flagIdentifier);

        flag.addInstruction(instruction);
        flag.setChallengeName(checkName);

        return flag;
    }

    /**
     * The opposite of getPointsFromGeojson -- get all geojson features which do contain a
     * properties field from a {@link JsonArray}.
     *
     * @param features
     *            a {@link JsonArray} of geojson features
     * @return a JsonArray containing all features which were added to a task through
     *         task.setGeojson() and therefore should have properties.
     */
    private JsonArray filterOutPointsFromGeojson(final JsonArray features)
    {
        return this.objectStream(features).filter(feature -> feature.has(PROPERTIES))
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    /**
     * Returns a {@link Set} of {@link Location}s gathered from feature.geometry.coordinates for
     * each feature in features which does not have a properties field.
     *
     * @param features
     *            a {@link JsonArray} of geojson features.
     * @return a {@link Set} of {@link Location}s which were generated from task.addPoints() (and
     *         therefore have no properties of their own).
     */
    private Set<Location> getPointsFromGeojson(final JsonArray features)
    {
        return this.objectStream(features).filter(feature -> !feature.has(PROPERTIES))
                .map(feature -> feature.get(GEOMETRY).getAsJsonObject().get(COORDINATES)
                        .getAsJsonArray())
                .map(longlatArray -> new Location(
                        Latitude.degrees(longlatArray.get(1).getAsDouble()),
                        Longitude.degrees(longlatArray.get(0).getAsDouble())))
                .collect(Collectors.toSet());
    }

    /**
     * Stream all of the {@link JsonObject}s in a {@link JsonArray}.
     * 
     * @param features
     *            a {@link JsonArray} containing only {@link JsonObject}s
     * @return a {@link Stream} containing all {@link JsonObject}s inside features.
     */
    private Stream<JsonObject> objectStream(final JsonArray features)
    {
        return StreamSupport.stream(features.spliterator(), false)
                .map(JsonElement::getAsJsonObject);
    }
}
