package org.openstreetmap.atlas.checks.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlaggedObject;
import org.openstreetmap.atlas.checks.flag.FlaggedRelation;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.GeometryWithProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.tags.HighwayTag;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Wraps a {@link CheckFlag} for submission to the {@link EventService} for handling {@link Check}
 * results
 *
 * @author mkalender, bbreithaupt
 */
public final class CheckFlagEvent extends Event
{
    private static final GeoJsonBuilder GEOJSON_BUILDER = new GeoJsonBuilder();
    private static final String GEOMETRY = "geometry";
    public static final String GEOMETRY_COLLECTION = "GeometryCollection";
    private static final String GEOMETRIES = "geometries";
    private static final String PROPERTIES = "properties";
    private static final String FEATURES = "features";
    private static final String TYPE = "type";
    private static final String FEATURE_COLLECTION = "FeatureCollection";

    private final String checkName;
    private final CheckFlag flag;

    /**
     * Converts give {@link CheckFlag} to {@link GeoJsonObject} with additional key-value parameters
     *
     * @param flag
     *            {@link CheckFlag} to convert to {@link GeoJsonObject}
     * @param additionalProperties
     *            additional key-value parameters to be added in "properties" element of the
     *            top-level JSON object
     * @return {@link GeoJsonObject} created from {@link CheckFlag}
     */
    public static JsonObject flagToFeature(final CheckFlag flag,
            final Map<String, String> additionalProperties)
    {
        final JsonObject flagProperties = new JsonObject();
        flagProperties.addProperty("instructions", flag.getInstructions());

        // Add additional properties
        additionalProperties.forEach(flagProperties::addProperty);

        final JsonObject feature;
        final List<GeometryWithProperties> geometriesWithProperties = flag
                .getGeometryWithProperties();
        final Set<FlaggedRelation> flaggedRelations = flag.getFlaggedRelations();
        final JsonArray geometriesJsonArray;
        final JsonArray featureProperties = new JsonArray();
        final Set<JsonElement> featureOsmIds = new HashSet<>();
        if (!geometriesWithProperties.isEmpty())
        {
            if (geometriesWithProperties.size() + flaggedRelations.size() == 1)
            {
                feature = GEOJSON_BUILDER.create(geometriesWithProperties.get(0));
            }
            else
            {
                feature = GEOJSON_BUILDER.createGeometryCollectionFeature(geometriesWithProperties)
                        .jsonObject();
            }
            // Geometries with coordinates
            geometriesJsonArray = feature.get(GEOMETRY).getAsJsonObject()
                    .getAsJsonArray(GEOMETRIES);
            geometriesWithProperties.stream().forEach(geometry -> Optional
                    .ofNullable(geometry.getProperties()).ifPresent(propertyMap ->
                    {
                        final JsonObject properties = new JsonObject();
                        propertyMap.forEach(
                                (key, value) -> properties.addProperty(key, (String) value));
                        featureProperties.add(properties);

                        Optional.ofNullable(properties.get("osmid")).ifPresent(featureOsmIds::add);
                    }));
        }
        else
        {
            feature = new JsonObject();
            geometriesJsonArray = new JsonArray();
        }
        if (!flaggedRelations.isEmpty())
        {
            final JsonArray geometriesOfFlaggedRelations = new JsonArray();
            if (!feature.has(TYPE))
            {
                feature.addProperty(TYPE, "Feature");
            }
            // Get flagged relations as GeoJson features
            final List<JsonObject> flaggedRelationFeatures = flaggedRelations.stream()
                    .map(flaggedRelation -> flaggedRelation.asGeoJsonFeature(flag.getIdentifier()))
                    .collect(Collectors.toList());
            if (flaggedRelations.size() == 1 && !feature.has(GEOMETRY))
            {
                feature.add(GEOMETRY, flaggedRelationFeatures.get(0).get(GEOMETRY));
            }
            else if (!feature.has(GEOMETRY_COLLECTION))
            {
                final JsonObject geometryCollection = new JsonObject();
                geometryCollection.add(GEOMETRIES, geometriesJsonArray);
                geometryCollection.addProperty(TYPE, GEOMETRY_COLLECTION);
                feature.add(GEOMETRY, geometryCollection);
            }

            // Add all geometries of flaggedRelations to the json array
            flaggedRelationFeatures.stream()
                    .map(flaggedRelationFeature -> flaggedRelationFeature.get(GEOMETRY))
                    .forEach(jsonElement -> geometriesOfFlaggedRelations.add(jsonElement));
            // To geometries of flagged objects add geometries of flaggedRelation
            geometriesJsonArray.addAll(geometriesOfFlaggedRelations);
            // Update feature properties from flaggedRelation features
            flaggedRelationFeatures.stream()
                    .map(flaggedRelationFeature -> flaggedRelationFeature.get(PROPERTIES))
                    .forEach(property -> featureProperties.add(property));
            // Add osm id
            flaggedRelationFeatures.stream()
                    .map(flaggedRelationFeature -> flaggedRelationFeature.get(PROPERTIES))
                    .map(jsonElement -> jsonElement.getAsJsonObject().get("osmIdentifier"))
                    .forEach(featureOsmIds::add);
        }

        final JsonArray uniqueFeatureOsmIds = new JsonArray();
        featureOsmIds.forEach(uniqueFeatureOsmIds::add);

        // Override name property if able to add a decorator to the name
        CheckFlagEvent.featureDecorator(featureProperties)
                .ifPresent(decorator -> flagProperties.addProperty("name",
                        String.format("%s (%s)",
                                Optional.ofNullable(flagProperties.getAsJsonPrimitive("name"))
                                        .map(JsonPrimitive::getAsString).orElse("Task"),
                                decorator)));
        // Reference properties lost during GeoJson conversion
        flagProperties.add("feature_properties", featureProperties);
        flagProperties.add("feature_osmids", uniqueFeatureOsmIds);
        flagProperties.addProperty("feature_count", featureProperties.size());

        feature.addProperty("id", flag.getIdentifier());
        feature.add("properties", flagProperties);
        return feature;
    }

    /**
     * Converts given {@link CheckFlag} to {@link JsonObject} with additional key-value parameters
     *
     * @param flag
     *            {@link CheckFlag} to convert to {@link JsonObject}
     * @param additionalProperties
     *            additional key-value parameters to be added in "properties" element of the
     *            top-level JSON object
     * @return {@link JsonObject} created from {@link CheckFlag}
     */
    public static JsonObject flagToJson(final CheckFlag flag,
            final Map<String, String> additionalProperties)
    {
        JsonObject flagJson = new JsonObject();
        if (!flag.getFlaggedObjects().isEmpty())
        {
            flagJson = GEOJSON_BUILDER
                    .createFromGeometriesWithProperties(flag.getGeometryWithProperties())
                    .jsonObject();
        }
        final Set<FlaggedRelation> flaggedRelations = flag.getFlaggedRelations();
        // Add features of FlaggedRelation if any
        if (!flaggedRelations.isEmpty())
        {
            // Add type feature collection if not already set.
            if (!flagJson.has(FEATURES))
            {
                flagJson.addProperty(TYPE, FEATURE_COLLECTION);
                flagJson.add(FEATURES, new JsonArray());
            }
            final JsonArray features = flagJson.getAsJsonArray(FEATURES);
            // Get features of each flaggedRelations and add it to the flagJson object's FEATURES
            // element
            flaggedRelations.stream()
                    .map(flaggedRelation -> flaggedRelation.asGeoJsonFeature(flag.getIdentifier()))
                    .forEach(jsonObject -> features.add(jsonObject));
        }
        final JsonObject flagPropertiesJson = new JsonObject();
        flagPropertiesJson.addProperty("id", flag.getIdentifier());
        flagPropertiesJson.addProperty("instructions", flag.getInstructions());

        // Add additional properties
        additionalProperties.forEach(flagPropertiesJson::addProperty);

        // Add properties to the previously generate geojson
        flagJson.add("properties", flagPropertiesJson);
        return flagJson;
    }

    private static void accept(final JsonObject jsonObject)
    {
    }

    /**
     * Extracts a decorator based on the collective features properties. Currently the only
     * decoration is the highest class highway tag withing all of the feature properties for flags
     * involving Edges.
     */
    private static Optional<String> featureDecorator(final JsonArray featureProperties)
    {
        HighwayTag highestHighwayTag = null;
        for (final JsonElement featureProperty : featureProperties)
        {
            final HighwayTag baslineHighwayTag = highestHighwayTag == null ? HighwayTag.NO
                    : highestHighwayTag;
            try
            {
                highestHighwayTag = Optional
                        .ofNullable(
                                ((JsonObject) featureProperty).getAsJsonPrimitive(HighwayTag.KEY))
                        .map(JsonPrimitive::getAsString).map(String::toUpperCase)
                        .map(HighwayTag::valueOf).filter(baslineHighwayTag::isLessImportantThan)
                        .orElse(highestHighwayTag);
            }
            catch (final IllegalArgumentException badValue)
            {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(highestHighwayTag)
                .map(tag -> String.format("%s=%s", HighwayTag.KEY, tag.getTagValue()));
    }

    /**
     * Construct a {@link CheckFlagEvent}
     *
     * @param checkName
     *            name of the check that created this event
     * @param flag
     *            {@link CheckFlag} generated within this event
     */
    public CheckFlagEvent(final String checkName, final CheckFlag flag)
    {
        this.checkName = checkName;
        this.flag = flag;
    }

    /**
     * @return {@link CheckFlag} generated by the check
     */
    public CheckFlag getCheckFlag()
    {
        return this.flag;
    }

    /**
     * @return Name of the check that generated this event
     */
    public String getCheckName()
    {
        return this.checkName;
    }

    /**
     * @return GeoJson Feature representation
     */
    public JsonObject toGeoJsonFeature()
    {
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("name",
                this.getCheckFlag().getChallengeName().orElse(this.getCheckName()));
        contextualProperties.put("generator", "AtlasChecks");
        contextualProperties.put("timestamp", this.getTimestamp().toString());

        // Generate json for check flag with given contextual properties
        return flagToFeature(this.getCheckFlag(), contextualProperties);
    }

    /**
     * @return {@link JsonObject} form of the GeoJson FeatureCollection representation
     */
    public JsonObject toGeoJsonFeatureCollection()
    {
        final Map<String, String> contextualProperties = new HashMap<>();
        contextualProperties.put("generator", this.getCheckName());
        contextualProperties.put("timestamp", this.getTimestamp().toString());

        // Generate json for check flag with given contextual properties
        return flagToJson(this.getCheckFlag(), contextualProperties);
    }

    /**
     * @return {@link String} form of the GeoJson FeatureCollection representation
     */
    @Override
    public String toString()
    {
        return this.toGeoJsonFeatureCollection().toString();
    }

    public String asLineDelimitedGeoJsonFeatures()
    {
        return asLineDelimitedGeoJsonFeatures(CheckFlagEvent::accept);
    }

    public String asLineDelimitedGeoJsonFeatures(final Consumer<JsonObject> jsonMutator)
    {
        final JsonObject flagGeoJsonFeature = flag.asGeoJsonFeature();
        final JsonObject flagGeoJsonProperties = flagGeoJsonFeature.get("properties")
                .getAsJsonObject();
        flagGeoJsonProperties.addProperty("flag:check", getCheckName());
        flagGeoJsonProperties.addProperty("flag:timestamp", getTimestamp().toString());

        jsonMutator.accept(flagGeoJsonFeature);

        final StringBuilder builder = new StringBuilder().append(flagGeoJsonFeature.toString());

        final FlaggedObject[] flaggedObjects = flag.getFlaggedObjects()
                .toArray(new FlaggedObject[0]);
        final int flaggedObjectsSize = flaggedObjects.length;

        if (flaggedObjectsSize > 0)
        {
            builder.append('\n');

            // loop through all the flagged objects except the last, give a new line
            int index = 0;
            for (; index < flaggedObjectsSize - 1; ++index)
            {
                final JsonObject feature = flaggedObjects[index]
                        .asGeoJsonFeature(flag.getIdentifier());
                jsonMutator.accept(feature);
                builder.append(feature.toString()).append('\n');
            }

            // dont give a new line to the last
            final JsonObject feature = flaggedObjects[index].asGeoJsonFeature(flag.getIdentifier());
            jsonMutator.accept(feature);
            builder.append(feature.toString());
        }

        return builder.toString();
    }
}
