package org.openstreetmap.atlas.checks.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
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
        final List<GeometryWithProperties> geometriesWithProperties = flag.getLocationIterableProperties();
        if (geometriesWithProperties.size() == 1)
        {
            feature = GEOJSON_BUILDER.create(geometriesWithProperties.get(0));
        }
        else
        {
            feature = GEOJSON_BUILDER.createGeometryCollectionFeature(geometriesWithProperties)
                    .jsonObject();
        }
        // Geometry json element of feature : "type":"feature", "geometry":{"type":"GeometryCoolection","geometries":[type and coordinates]
        // first geometry
        final JsonElement geometryCollection = feature.get("geometry");
        // To access geometries of jsonObject geometry
        final JsonObject asJsonObject = geometryCollection.getAsJsonObject();
        // Second geometries with coordinates
        final JsonArray geometriesJsonArray = asJsonObject.getAsJsonArray("geometries");



        final JsonArray featureProperties = new JsonArray();
        final Set<JsonElement> featureOsmIds = new HashSet<>();
        geometriesWithProperties.stream().forEach(
                geometry -> Optional.ofNullable(geometry.getProperties()).ifPresent(propertyMap ->
                {
                    final JsonObject properties = new JsonObject();
                    propertyMap.forEach( (key, value) -> properties.addProperty(key, (String) value));
                    featureProperties.add(properties);

                    Optional.ofNullable(properties.get("osmid")).ifPresent(featureOsmIds::add);
                }));

        if(!flag.getFlaggedRelations().isEmpty())
        {
            final Set<FlaggedRelation> flaggedRelations = flag.getFlaggedRelations();
            final List<JsonObject> flaggedRelationFeatures = flaggedRelations.stream()
                    .map(flaggedRelation -> flaggedRelation.asGeoJsonFeature(flag.getIdentifier()))
                    .collect(Collectors.toList());
            // add all geometries of flaggedRelations to the json array
            final JsonArray geometriesOfFlaggedRelations = new JsonArray();
            flaggedRelationFeatures.stream()
                    .map(flaggedRelationFeature -> flaggedRelationFeature.get("geometry"))
                    .forEach(jsonElement -> geometriesOfFlaggedRelations.add(jsonElement));

            // To geometries of flagged objects add geometries of flaggedRelation
            geometriesJsonArray.addAll(geometriesOfFlaggedRelations);
            flaggedRelationFeatures.stream()
                    .map(flaggedRelationFeature -> flaggedRelationFeature.get("properties"))
                    .forEach(property -> featureProperties.add(property));
            flaggedRelationFeatures.stream()
                    .map(flaggedRelationFeature -> flaggedRelationFeature.get("properties")).map(jsonElement -> jsonElement.getAsJsonObject().get("osmIdentifier")).forEach(featureOsmIds::add);
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
//    /**
//     * Populates properties of flaggedRelations. Both the relation properties as well as its member
//     * properties will be populated.
//     *
//     * @param flag
//     * @param geometriesWithProperties
//     * @param featureOsmIds
//     * @param featureProperties
//     */
//    private static void populateFlaggedRelationProperties(final CheckFlag flag,
//            final List<GeometryWithProperties> geometriesWithProperties,
//            final Set<JsonElement> featureOsmIds, final JsonArray featureProperties)
//    {
//        final Iterator<FlaggedRelation> iterator = flag.getFlaggedRelations().iterator();
//        while (iterator.hasNext())
//        {
//            final JsonObject relationProperties = new JsonObject();
//            final JsonObject innerRelationProperties = new JsonObject();
//            final Set<Long> flaggedMemberOSMIds = new HashSet<>();
//            final FlaggedRelation next = iterator.next();
//            // Populate flagged relation properties
//            next.getProperties().forEach(relationProperties::addProperty);
//            featureProperties.add(relationProperties);
//            // If the flagged relations have relations as members, then add the properties of the
//            // member relations as well.
//            // Get all members of the relation that are relations
//            final List<RelationMember> innerRelations = next.members().stream()
//                    .filter(member -> member.getEntity() instanceof Relation)
//                    .collect(Collectors.toList());
//            if (!innerRelations.isEmpty())
//            {
//                // Get properties of the member relation
//                innerRelations.stream()
//                        .map(relationMember -> new FlaggedRelation(
//                                (Relation) relationMember.getEntity()).getProperties())
//                        // Populate the properties of the member relations
//                        .forEach(map -> map.forEach(innerRelationProperties::addProperty));
//                featureProperties.add(innerRelationProperties);
//            }
//            // Set of relation member OSM IDs
//            final Set<Long> relationMemberOsmIds = next.members().stream()
//                    .map(member -> member.getEntity().getOsmIdentifier())
//                    .collect(Collectors.toSet());
//            // Populate properties of members
//            populateMemberProperties(geometriesWithProperties, featureOsmIds, featureProperties,
//                    flaggedMemberOSMIds, relationMemberOsmIds);
//            Optional.ofNullable(relationProperties.get("osmid")).ifPresent(featureOsmIds::add);
//            Optional.ofNullable(innerRelationProperties.get("osmid")).ifPresent(featureOsmIds::add);
//        }
//    }

    /**
     * Populate properties of flagged objects
     *
     * @param geometriesWithProperties
     * @param featureOsmIds
     * @param featureProperties
     */
    private static void populateFlaggedObjectProperties(
            final List<GeometryWithProperties> geometriesWithProperties,
            final Set<JsonElement> featureOsmIds, final JsonArray featureProperties)
    {
        geometriesWithProperties.stream().forEach(geometryWithProperties -> Optional
                .ofNullable(geometryWithProperties.getProperties()).ifPresent(propertyMap ->
                {
                    final JsonObject properties = new JsonObject();
                    // Consider only those geometriesWithProperties that are not members/flattened
                    // members of relations
                    if (!propertyMap.containsKey("roles"))
                    {
                        propertyMap.forEach(
                                (key, value) -> properties.addProperty(key, (String) value));
                        featureProperties.add(properties);
                        Optional.ofNullable(properties.get("osmid")).ifPresent(featureOsmIds::add);
                    }

                }));
    }

    /**
     * Populate properties of relation members. Note: @param geometries has geometries of all
     * flattened members. Properties will be populated from the list of geometries of relation
     * members only. This makes sure that the duplicate properties of way sectioned edges are not
     * added. Eg: if a way in OSM has five edges in Atlas, then the geometry of all the five edges
     * will be added but only the properties of one of these.
     *
     * @param geometriesWithProperties
     * @param featureOsmIds
     * @param featureProperties
     * @param relationMemberOsmIds
     * @param flaggedMemberOSMIds
     */
    private static void populateMemberProperties(
            final List<GeometryWithProperties> geometriesWithProperties,
            final Set<JsonElement> featureOsmIds, final JsonArray featureProperties,
            final Set<Long> flaggedMemberOSMIds, final Set<Long> relationMemberOsmIds)
    {
        geometriesWithProperties.stream()
                .filter(geometryWithProperties -> Optional
                        .ofNullable(geometryWithProperties.getProperties()).isPresent())
                .map(geometryWithProperties -> geometryWithProperties.getProperties())
                .forEach(propertyMap ->
                {
                    final Long osmid = Long.valueOf((String) propertyMap.get("osmid"));
                    // For each geometry, add properties for only the members of the relation.
                    // This ensures that the same properties of way sectioned edges are not
                    // duplicated. Also, only relation member properties are added and not
                    // properties
                    // of all flattened members.
                    if (!flaggedMemberOSMIds.contains(osmid)
                            && relationMemberOsmIds.contains(osmid))
                    {
                        final JsonObject properties = new JsonObject();
                        propertyMap.forEach((key, value) ->
                        {
                            if (key.contains("roles"))
                            {
                                properties.add(key, (JsonElement) value);
                            }
                            else
                            {
                                properties.addProperty(key, (String) value);
                            }
                        });
                        featureProperties.add(properties);
                        Optional.ofNullable(properties.get("osmid")).ifPresent(featureOsmIds::add);
                        flaggedMemberOSMIds.add(osmid);
                    }
                });
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
        final JsonObject flagJson = GEOJSON_BUILDER.createFromGeometriesWithProperties(flag.getLocationIterableProperties())
                .jsonObject();
        final JsonObject flagPropertiesJson = new JsonObject();
        flagPropertiesJson.addProperty("id", flag.getIdentifier());
        flagPropertiesJson.addProperty("instructions", flag.getInstructions());

        // Add additional properties
        additionalProperties.forEach((key, value) -> flagPropertiesJson.addProperty(key, value));
        // Add relation properties of the flaggedRelation to all its members.
        // If relations are not flagged then the list is empty and no action will be taken.
        final Set<FlaggedRelation> flaggedRelations = flag.getFlaggedRelations();
        if (!flaggedRelations.isEmpty())
        {
            final JsonArray relationsJson = new JsonArray();
            flag.getFlaggedRelations().stream()
                    .map(flaggedRelation -> flaggedRelation.getProperties()).forEach(properties ->
            {
                final JsonObject relationPropertiesJson = new JsonObject();
                properties.forEach(relationPropertiesJson::addProperty);
                final String osmid = properties.get("osmid");
                relationsJson.add(relationPropertiesJson);
            });
            flagPropertiesJson.add("relations", relationsJson);
        }

        // Add properties to the previously generate geojson
        flagJson.add("properties", flagPropertiesJson);
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
        return asLineDelimitedGeoJsonFeatures((jsonObject) ->
        {
        });
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
