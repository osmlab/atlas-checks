package org.openstreetmap.atlas.checks.flag.serializer;

import static org.openstreetmap.atlas.checks.event.CheckFlagEvent.FIX_SUGGESTIONS;
import static org.openstreetmap.atlas.checks.event.CheckFlagEvent.INSTRUCTIONS;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlaggedRelation;
import org.openstreetmap.atlas.checks.utility.tags.SyntheticHighlightPointTag;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorName;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.converters.jts.JtsCoordinateArrayConverter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonConstants;
import org.openstreetmap.atlas.geography.geojson.parser.GeoJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.feature.FeatureCollection;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiPolygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Point;
import org.openstreetmap.atlas.geography.geojson.parser.impl.gson.GeoJsonParserGsonImpl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A deserializer for converting a geojson feature collection back to a CheckFlag.
 *
 * @author danielbaah
 * @author bbreithaupt
 */
public class CheckFlagDeserializer implements JsonDeserializer<CheckFlag>
{
    private static final String PROPERTIES = "properties";
    private static final String GENERATOR = "generator";
    private static final String ID = "id";
    private static final String DESCRIPTORS = "descriptors";
    private static final String ITEM_TYPE = "itemType";
    private static final String ROLE = "role";
    private static final String POSITION = "position";
    private static final String AFTER_VIEW = "afterView";

    private static final GeoJsonParser GEOJSON_PARSER_GSON = GeoJsonParserGsonImpl.instance;
    private static final Gson GSON = new Gson();
    private static final WKTReader WKT_READER = new WKTReader();
    private static final JtsCoordinateArrayConverter COORDINATE_ARRAY_CONVERTER = new JtsCoordinateArrayConverter();

    /**
     * Returns a comma delimited string of identifiers.
     *
     * @param identifiers
     *            - array of flag identifiers
     * @return - comma delimited string
     */
    public static String parseIdentifiers(final JsonArray identifiers)
    {
        return Arrays.stream(new Gson().fromJson(identifiers, String[].class)).sorted()
                .map(String::toString).collect(Collectors.joining(","));
    }

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
        // Split the instructions using the new line character and remove the prepended instruction
        // number
        final List<String> instructions = Arrays
                .stream(properties.get(INSTRUCTIONS).getAsString().split("\n"))
                .map(instruction -> instruction.replaceAll("^\\d+\\. ",
                        CommonConstants.EMPTY_STRING))
                .collect(Collectors.toList());
        final String flagIdentifier = properties.get(ID).getAsString();
        final CheckFlag flag = new CheckFlag(flagIdentifier);
        flag.addInstructions(instructions);
        flag.setChallengeName(checkName);

        final GeoJsonItem geojsonItem = GEOJSON_PARSER_GSON.deserialize(GSON.toJson(json));

        // This should never be the case
        if (!(geojsonItem instanceof FeatureCollection))
        {
            return flag;
        }

        // Deserialize all features and their fix suggestions
        ((FeatureCollection) geojsonItem).getFeatures().forEach(feature ->
        {
            // If the feature is a synthetic point just add its geometry
            if (feature.getProperties().asMap().containsKey(SyntheticHighlightPointTag.KEY)
                    && feature.getGeometry() instanceof Point)
            {
                flag.addPoint(((Point) feature.getGeometry()).toAtlasGeometry());
            }
            else
            {
                // Convert the geojson feature into and AtlasEntity and add it to the flag
                GeoJsonFeatureToAtlasEntityConverter.convert(feature).ifPresent(entity ->
                {
                    // If it is a relation, we need to add its geometry separately
                    if (entity instanceof Relation)
                    {
                        flag.addObject(new FlaggedRelation((Relation) entity,
                                ((MultiPolygon) feature.getGeometry()).toAtlasGeometry()));
                    }
                    else
                    {
                        flag.addObject(entity);
                    }
                    // If there is a fix suggestion for the object, reconstruct it to a
                    // FeatureChange and add it to the flag
                    if (full.has(FIX_SUGGESTIONS))
                    {
                        this.getFixSuggestion(entity, full.get(FIX_SUGGESTIONS).getAsJsonObject())
                                .ifPresent(flag::addFixSuggestion);
                    }
                });
            }
        });

        return flag;
    }

    /**
     * Apply geometry change descriptors to a CompleteEntity.
     *
     * @param afterView
     *            {@link CompleteEntity} to apply the change to
     * @param descriptors
     *            {@link Lists}
     */
    private void applyGeometryChanges(final CompleteEntity afterView,
            final List<JsonObject> descriptors)
    {
        // Copy the geometry to apply the changes to
        final List<Location> newGeometry = Lists.newArrayList(afterView.getGeometry());

        // Sort the geometry changes by type and position, as order of application matters
        descriptors.stream().sorted(Comparator
                .comparing(descriptor -> ChangeDescriptorType.valueOf(((JsonObject) descriptor)
                        .get(GeoJsonConstants.TYPE).getAsString().toUpperCase()))
                .thenComparingInt(descriptor -> Integer.valueOf(
                        ((JsonObject) descriptor).get(POSITION).getAsString().split("/")[0])))
                // Apply each change
                .forEach(descriptor ->
                {
                    final Integer position = Integer
                            .valueOf(descriptor.get(POSITION).getAsString().split("/")[0]);
                    switch (ChangeDescriptorType.valueOf(
                            descriptor.get(GeoJsonConstants.TYPE).getAsString().toUpperCase()))
                    {
                        // Apply added location in reverse order so that they push each other
                        // forward from the given position
                        case ADD:
                            Lists.reverse(this.convertWktToLocations(
                                    descriptor.get(AFTER_VIEW).getAsString()))
                                    .forEach(location -> newGeometry.add(position, location));
                            break;
                        // Just remove the location at the position
                        case REMOVE:
                            newGeometry.remove(position.intValue());
                            break;
                        // Updates must override one existing location, but may add beyond the
                        // current length of the geometry
                        case UPDATE:
                            final List<Location> updateLocations = this.convertWktToLocations(
                                    descriptor.get(AFTER_VIEW).getAsString());
                            for (int index = 0; index < updateLocations.size(); index++)
                            {
                                // If this is an existing position, replace it
                                if (newGeometry.size() > position + index)
                                {
                                    newGeometry.set(position + index, updateLocations.get(index));
                                }
                                // Else add to the end
                                else
                                {
                                    newGeometry.add(updateLocations.get(index));
                                }
                            }
                            break;
                        default:
                            break;
                    }
                });

        afterView.withGeometry(newGeometry);
    }

    /**
     * Apply a relation member change descriptor to a CompleteEntity.
     *
     * @param afterView
     *            {@link CompleteEntity}
     * @param descriptor
     *            {@link JsonObject} relation member change descriptor
     */
    private void applyRelationMemberChange(final CompleteEntity afterView,
            final JsonObject descriptor)
    {
        // Only apply to relations
        if (afterView instanceof CompleteRelation)
        {
            // Generate a bean item form the description
            final RelationBean.RelationBeanItem member = new RelationBean.RelationBeanItem(
                    descriptor.get(ID).getAsLong(), descriptor.get(ROLE).getAsString(),
                    ItemType.valueOf(descriptor.get(ITEM_TYPE).getAsString().toUpperCase()));

            // Get the existing relation bean. A RelationBean must be used to apply the change
            // because the bounds of the member cannot be automatically calculated
            final RelationBean bean = ((CompleteRelation) afterView).getBean();

            switch (ChangeDescriptorType
                    .valueOf(descriptor.get(GeoJsonConstants.TYPE).getAsString().toUpperCase()))
            {
                // Add the new member to the bean and add the bean to the relation
                case ADD:
                    bean.add(member);
                    ((CompleteRelation) afterView).withMembers(bean,
                            ((CompleteRelation) afterView).bounds());
                    break;
                // Remove the member from the bean and add the bean to the relation
                case REMOVE:
                    bean.removeItem(member);
                    ((CompleteRelation) afterView).withMembers(bean,
                            ((CompleteRelation) afterView).bounds());
                    break;
                default:
                    break;

            }
        }
    }

    /**
     * Apply a tag change descriptor to a CompleteEntity.
     *
     * @param afterView
     *            {@link CompleteEntity}
     * @param descriptor
     *            {@link JsonObject} tag change descriptor
     */
    private void applyTagChange(final CompleteEntity afterView, final JsonObject descriptor)
    {
        final String key = descriptor.get("key").getAsString();
        final String value = descriptor.get("value").getAsString();

        switch (ChangeDescriptorType
                .valueOf(descriptor.get(GeoJsonConstants.TYPE).getAsString().toUpperCase()))
        {
            case ADD:
                afterView.withAddedTag(key, value);
                break;
            case REMOVE:
                afterView.withRemovedTag(key);
                break;
            case UPDATE:
                afterView.withReplacedTag(key, key, value);
                break;
            default:
                break;
        }
    }

    /**
     * Convert a WKT point, linestring, or polygon to a List of Locations
     *
     * @param wkt
     *            {@link String}
     * @return {@link List} of {@link Location}s
     */
    private List<Location> convertWktToLocations(final String wkt)
    {
        try
        {
            return Lists.newArrayList(COORDINATE_ARRAY_CONVERTER.backwardConvert(
                    new CoordinateArraySequence(WKT_READER.read(wkt).getCoordinates())));
        }
        catch (final ParseException parseException)
        {
            throw new CoreException("Cannot parse wkt : {}\nCaused by: ParseException {}", wkt,
                    parseException.getMessage());
        }
    }

    /**
     * Convert a Json fix suggestion to a FeatureChange if one exists for a given AtlasEntity.
     *
     * @param beforeView
     *            {@link AtlasEntity} to convert a fix suggesiton for
     * @param fixSuggestions
     *            {@link JsonObject} of fix suggestions
     * @return {@link FeatureChange} with the given atlas entity as the before view and the fix
     *         suggestion applied for the after view
     */
    private Optional<FeatureChange> getFixSuggestion(final AtlasEntity beforeView,
            final JsonObject fixSuggestions)
    {
        // See if the is a fix suggestion for the given entity
        final String uniqueIdentifier = StringUtils
                .capitalize(beforeView.getType().toString().toLowerCase())
                .concat(String.valueOf(beforeView.getIdentifier()));
        if (!fixSuggestions.has(uniqueIdentifier))
        {
            return Optional.empty();
        }

        // Get the applicable suggestion
        final JsonObject fixSuggestion = fixSuggestions.get(uniqueIdentifier).getAsJsonObject();
        final ChangeDescriptorType suggestionType = ChangeDescriptorType
                .valueOf(fixSuggestion.get(GeoJsonConstants.TYPE).getAsString().toUpperCase());
        // Not yet supported
        if (ChangeDescriptorType.ADD.equals(suggestionType))
        {
            return Optional.empty();
        }
        // Create a remove FeatureChange from a shallow copy of the before view
        if (ChangeDescriptorType.REMOVE.equals(suggestionType))
        {
            return Optional.of(new FeatureChange(ChangeType.REMOVE,
                    CompleteEntity.shallowFrom(beforeView), beforeView));
        }

        // Create a full copy for the after view
        final CompleteEntity afterView = (CompleteEntity) CompleteEntity.from(beforeView);
        // Remove start and end node identifier, else they show up as changed (and this sort of
        // change should never exist for a suggestion)
        if (afterView.getType().equals(ItemType.EDGE))
        {
            ((CompleteEdge) afterView).withStartNodeIdentifier(null);
            ((CompleteEdge) afterView).withEndNodeIdentifier(null);
        }

        // Loop thorough each change description, collecting the geometry changes and directly
        // applying the rest to the after view
        final List<JsonObject> geometryChangeDescriptors = new ArrayList<>();
        fixSuggestion.get(DESCRIPTORS).getAsJsonArray().forEach(descriptor ->
        {
            final JsonObject descriptorObject = descriptor.getAsJsonObject();

            switch (ChangeDescriptorName
                    .valueOf(descriptorObject.get("name").getAsString().toUpperCase()))
            {
                case TAG:
                    this.applyTagChange(afterView, descriptorObject);
                    break;
                case RELATION_MEMBER:
                    this.applyRelationMemberChange(afterView, descriptorObject);
                    break;
                case GEOMETRY:
                    geometryChangeDescriptors.add(descriptorObject);
                    break;
                default:
                    break;
            }
        });

        // Apply geometry changes as a group, as they must be sorted before application
        if (!geometryChangeDescriptors.isEmpty())
        {
            this.applyGeometryChanges(afterView, geometryChangeDescriptors);
        }

        return Optional.of(new FeatureChange(ChangeType.ADD, (AtlasEntity) afterView, beforeView));
    }
}
