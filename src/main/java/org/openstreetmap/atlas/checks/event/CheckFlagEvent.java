package org.openstreetmap.atlas.checks.event;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.GEOMETRY;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.TYPE;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonUtils.OSM_IDENTIFIER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlaggedObject;
import org.openstreetmap.atlas.checks.flag.FlaggedRelation;
import org.openstreetmap.atlas.event.Event;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.GeometryWithProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.tags.HighwayTag;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Wraps a {@link CheckFlag} for submission to the
 * {@link org.openstreetmap.atlas.event.EventService} for handling {@link Check} results
 *
 * @author mkalender
 * @author bbreithaupt
 */
public final class CheckFlagEvent extends Event
{
    private static final GeoJsonBuilder GEOJSON_BUILDER = new GeoJsonBuilder();
    private static final String GEOMETRY_COLLECTION = "GeometryCollection";
    private static final String GEOMETRIES = "geometries";
    private static final String FEATURES = "features";
    private static final String FEATURE_COLLECTION = "FeatureCollection";
    public static final String INSTRUCTIONS = "instructions";
    public static final String IDENTIFIERS = "identifiers";
    public static final String FIX_SUGGESTIONS = "fix_suggestions";

    private static final Gson GSON = new Gson();

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
        flagProperties.addProperty(INSTRUCTIONS, flag.getInstructions());

        // Add additional properties
        additionalProperties.forEach(flagProperties::addProperty);

        final JsonObject feature;
        final List<GeometryWithProperties> geometriesWithProperties = flag
                .getGeometryWithProperties();
        final Set<FlaggedObject> flaggedRelations = flag.getFlaggedRelations();
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
            geometriesWithProperties.forEach(geometry -> Optional
                    .ofNullable(geometry.getProperties()).ifPresent(propertyMap ->
                    {
                        final JsonObject properties = new JsonObject();
                        propertyMap.forEach(
                                (key, value) -> properties.addProperty(key, (String) value));
                        featureProperties.add(properties);
                        Optional.ofNullable(properties.get(OSM_IDENTIFIER))
                                .ifPresent(featureOsmIds::add);
                    }));
        }
        else
        {
            feature = new JsonObject();
            geometriesJsonArray = new JsonArray();
        }
        if (!flaggedRelations.isEmpty())
        {
            if (!feature.has(TYPE))
            {
                feature.addProperty(TYPE, "Feature");
            }
            // Get flagged relations as GeoJson features
            final List<JsonObject> flaggedRelationFeatures = getFlaggedRelationsGeojsonFeatures(
                    flag);
            if (flaggedRelations.size() == 1 && !feature.has(GEOMETRY))
            {
                feature.add(GEOMETRY, flaggedRelationFeatures.get(0).get(GEOMETRY));
            }
            else if (flaggedRelations.size() != 1 && !feature.has(GEOMETRY))
            {
                final JsonObject geometryCollection = new JsonObject();
                geometryCollection.add(GEOMETRIES, geometriesJsonArray);
                geometryCollection.addProperty(TYPE, GEOMETRY_COLLECTION);
                feature.add(GEOMETRY, geometryCollection);
            }
            // To geometries of flagged objects add geometries of flaggedRelation
            geometriesJsonArray.addAll(populateFlaggedRelationGeometries(flaggedRelationFeatures));
            // To properties of flagged objects add properties of flaggedRelation
            featureProperties
                    .addAll(populateFlaggedRelationFeatureProperties(flaggedRelationFeatures));
            featureOsmIds.addAll(populateFlaggedRelationFeatureOsmIds(flaggedRelationFeatures));
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
        flagProperties.add(IDENTIFIERS, GSON.toJsonTree(flag.getUniqueIdentifiers()));
        flagProperties.add(FIX_SUGGESTIONS, getFixSuggestionDescriptions(flag));

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
        final Set<FlaggedObject> flaggedRelations = flag.getFlaggedRelations();
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
                    .forEach(features::add);
        }
        final JsonObject flagPropertiesJson = new JsonObject();
        flagPropertiesJson.addProperty("id", flag.getIdentifier());
        flagPropertiesJson.addProperty(INSTRUCTIONS, flag.getInstructions());
        flagPropertiesJson.add(IDENTIFIERS, GSON.toJsonTree(flag.getUniqueIdentifiers()));

        // Add additional properties
        additionalProperties.forEach(flagPropertiesJson::addProperty);

        // Add properties to the previously generate geojson
        flagJson.add("properties", flagPropertiesJson);

        // Add fix suggestions as their own foreign object in the geojson
        flagJson.add(FIX_SUGGESTIONS, getFixSuggestionDescriptions(flag));

        return flagJson;
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

    private static JsonObject getFixSuggestionDescriptions(final CheckFlag flag)
    {
        final JsonObject fixSuggestionObject = new JsonObject();
        flag.getFixSuggestions()
                .forEach(suggestion -> fixSuggestionObject.add(
                        StringUtils.capitalize(suggestion.getItemType().toString().toLowerCase())
                                + suggestion.getIdentifier(),
                        suggestion.explain().toJsonElement()));
        return fixSuggestionObject;
    }

    /**
     * Get geometry of flagged relation feature.
     *
     * @param flaggedRelationFeature
     *            {@link FlaggedRelation} feature
     * @return geometry as {@link JsonElement}
     */
    private static JsonElement getFlaggedRelationGeometryFromFeature(
            final JsonObject flaggedRelationFeature)
    {
        return flaggedRelationFeature.get(GEOMETRY);
    }

    /**
     * Get property of the flagged relation feature
     *
     * @param flaggedRelationFeature
     *            {@link FlaggedRelation} feature
     * @return property as {@link JsonElement}
     */
    private static JsonElement getFlaggedRelationPropertyFromFeature(
            final JsonObject flaggedRelationFeature)
    {
        return flaggedRelationFeature.get(PROPERTIES);
    }

    /**
     * Converts all {@link FlaggedRelation} to geojson feature
     *
     * @param flag
     *            CheckFlag
     * @return {@link List<JsonObject>} corresponding to geojson feature of {@link FlaggedRelation}
     */
    private static List<JsonObject> getFlaggedRelationsGeojsonFeatures(final CheckFlag flag)
    {
        return flag.getFlaggedRelations().stream()
                .map(flaggedRelation -> flaggedRelation.asGeoJsonFeature(flag.getIdentifier()))
                .collect(Collectors.toList());
    }

    /**
     * Populates osmids of flaggedRelation features to a {@link Set<JsonElement>}
     *
     * @param flaggedRelationFeatures
     *            flaggedRelationFeatures geojson features of FlaggedRelations
     * @return {@link Set<JsonElement>} of all osmids of flaggedRelations
     */
    private static Set<JsonElement> populateFlaggedRelationFeatureOsmIds(
            final List<JsonObject> flaggedRelationFeatures)
    {
        // Add osm id
        return flaggedRelationFeatures.stream()
                .map(CheckFlagEvent::getFlaggedRelationPropertyFromFeature)
                .map(jsonElement -> jsonElement.getAsJsonObject().get(OSM_IDENTIFIER))
                .collect(Collectors.toSet());
    }

    /**
     * Populates properties of flaggedRelation features to a {@link JsonArray}
     *
     * @param flaggedRelationFeatures
     *            geojson features of FlaggedRelations
     * @return {@link JsonArray} of properties of flaggedRelations
     */
    private static JsonArray populateFlaggedRelationFeatureProperties(
            final List<JsonObject> flaggedRelationFeatures)
    {
        final JsonArray featureProperties = new JsonArray();
        // Update feature properties from flaggedRelation features
        flaggedRelationFeatures.stream().map(CheckFlagEvent::getFlaggedRelationPropertyFromFeature)
                .forEach(featureProperties::add);
        return featureProperties;
    }

    /**
     * Populates geometries of flaggedRelation features to a {@link JsonArray}
     *
     * @param flaggedRelationFeatures
     *            geojson features of FlaggedRelations
     * @return {@link JsonArray} of geometries of flaggedRelations
     */
    private static JsonArray populateFlaggedRelationGeometries(
            final List<JsonObject> flaggedRelationFeatures)
    {
        final JsonArray geometriesOfFlaggedRelations = new JsonArray();
        // Add all geometries of flaggedRelations to the json array
        flaggedRelationFeatures.stream().map(CheckFlagEvent::getFlaggedRelationGeometryFromFeature)
                .forEach(geometriesOfFlaggedRelations::add);
        return geometriesOfFlaggedRelations;
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

    public String asLineDelimitedGeoJsonFeatures()
    {
        return this.asLineDelimitedGeoJsonFeatures(jsonObject ->
        {
        });
    }

    public String asLineDelimitedGeoJsonFeatures(final Consumer<JsonObject> jsonMutator)
    {
        final JsonObject flagGeoJsonFeature = this.flag.asGeoJsonFeature();
        final JsonObject flagGeoJsonProperties = flagGeoJsonFeature.get("properties")
                .getAsJsonObject();
        flagGeoJsonProperties.addProperty("flag:check", this.getCheckName());
        flagGeoJsonProperties.addProperty("flag:timestamp", this.getTimestamp().toString());

        jsonMutator.accept(flagGeoJsonFeature);

        final StringBuilder builder = new StringBuilder().append(flagGeoJsonFeature.toString());

        final FlaggedObject[] flaggedObjects = this.flag.getFlaggedObjects()
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
                        .asGeoJsonFeature(this.flag.getIdentifier());
                jsonMutator.accept(feature);
                builder.append(feature.toString()).append('\n');
            }

            // dont give a new line to the last
            final JsonObject feature = flaggedObjects[index]
                    .asGeoJsonFeature(this.flag.getIdentifier());
            jsonMutator.accept(feature);
            builder.append(feature.toString());
        }

        return builder.toString();
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
}
