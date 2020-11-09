package org.openstreetmap.atlas.checks.utility;

import static org.openstreetmap.atlas.checks.flag.CheckFlag.NULL_IDENTIFIERS;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlaggedObject;
import org.openstreetmap.atlas.checks.flag.FlaggedPolyline;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * Utility to convert a {@link CheckFlag} into an OpenStreetMap like {@link CheckFlag}. This
 * currently means removing {@link org.openstreetmap.atlas.geography.atlas.items.Point} based
 * {@link FlaggedObject}s and {@link FeatureChange} suggestions that are also flagged as a node, and
 * merging the geometries of {@link Edge} based {@link FlaggedObject}s and suggestions that share an
 * OSM ID.
 *
 * @author bbreithaupt
 */
public final class OpenStreetMapCheckFlagConverter
{

    /**
     * Attempt to create an OpenStreetMap CheckFlag by removing
     * {@link org.openstreetmap.atlas.geography.atlas.items.Point}s duplicatly flagged as
     * {@link org.openstreetmap.atlas.geography.atlas.items.Node}s, and merging {@link Edge}s that
     * share an OSM ID.
     *
     * @param flag
     *            {@link CheckFlag}
     * @return {@link Optional} OSM {@link CheckFlag}
     */
    public static Optional<CheckFlag> openStreetMapify(final CheckFlag flag)
    {
        // Copy the identifier and instructions
        final CheckFlag newFlag = new CheckFlag(flag.getIdentifier());
        newFlag.addInstructions(flag.getRawInstructions());

        // Map objects by their unique osm id
        final Map<String, Set<FlaggedObject>> objectsMap = new HashMap<>();
        flag.getFlaggedObjects().forEach(object ->
        {
            final String osmIdentifier = object.getUniqueIdentifier().replaceFirst("\\d{6}$",
                    CommonConstants.EMPTY_STRING);
            objectsMap.putIfAbsent(osmIdentifier, new HashSet<>());
            objectsMap.get(osmIdentifier).add(object);
        });

        // Map feature changes by their unique osm id
        final Map<String, Set<FeatureChange>> suggestionMap = new HashMap<>();
        flag.getFixSuggestions().forEach(suggestion ->
        {
            final String osmIdentifier = String.format("%s%s",
                    StringUtils.capitalize(suggestion.getBeforeView().getType().toString()),
                    suggestion.getBeforeView().getOsmIdentifier());
            suggestionMap.putIfAbsent(osmIdentifier, new HashSet<>());
            suggestionMap.get(osmIdentifier).add(suggestion);
        });

        try
        {
            // Re-add points
            objectsMap.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(NULL_IDENTIFIERS))
                    .flatMap(entry -> entry.getValue().stream()).forEach(newFlag::addObject);
            // Convert and add atlas based features
            convertFlaggedObjects(objectsMap, newFlag);
            // Convert and add suggestions
            convertFixSuggestions(suggestionMap, newFlag);
        }
        catch (final CoreException exception)
        {
            return Optional.empty();
        }

        return Optional.of(newFlag);
    }

    /**
     * Merge {@link FeatureChange} fix suggestions to make them OSMified.
     *
     * @param suggestionMap
     *            {@link Map} of {@link FeatureChange}s by unique OSM ID
     * @param newFlag
     *            {@link CheckFlag} to add the converted suggestions to
     */
    private static void convertFixSuggestions(final Map<String, Set<FeatureChange>> suggestionMap,
            final CheckFlag newFlag)
    {
        // Go through each entry
        suggestionMap.entrySet().stream().flatMap(entry ->
        {
            // Use the first feature change as a base for the merged one
            final FeatureChange firstFeatureChange = entry.getValue().iterator().next();
            switch (entry.getValue().iterator().next().getBeforeView().getType())
            {
                // Merge edge FeatureChanges by concatenating the geometries of the before and after
                // views and applying them to a copy of the first feature change
                case EDGE:
                    // Sort the before views and concatenate their geometries
                    final Optional<PolyLine> concatenatedBeforePolyline = entry.getValue().stream()
                            .map(FeatureChange::getBeforeView)
                            .sorted(Comparator.comparing(AtlasEntity::getIdentifier))
                            .map(entity -> ((Edge) entity).asPolyLine()).reduce(PolyLine::append);
                    // Sort the after views and concatenate their geometries
                    final Optional<PolyLine> concatenatedAfterPolyline = entry.getValue().stream()
                            .map(FeatureChange::getAfterView)
                            .sorted(Comparator.comparing(AtlasEntity::getIdentifier))
                            .map(entity -> ((Edge) entity).asPolyLine()).reduce(PolyLine::append);
                    if (concatenatedBeforePolyline.isPresent()
                            && concatenatedAfterPolyline.isPresent())
                    {
                        // Copy the first feature change and apply the merged geometries
                        return Stream.of(new FeatureChange(firstFeatureChange.getChangeType(),
                                (AtlasEntity) CompleteEdge
                                        .from((Edge) firstFeatureChange.getAfterView())
                                        .withGeometry(concatenatedAfterPolyline.get()),
                                (AtlasEntity) CompleteEdge
                                        .from((Edge) firstFeatureChange.getBeforeView())
                                        .withGeometry(concatenatedBeforePolyline.get())));
                    }
                    throw new CoreException("Unable to concatenate Edges.");
                // Ignore suggestions for points that are duplicated for nodes
                case POINT:
                    return suggestionMap.containsKey(entry.getKey().replace("POINT", "NODE"))
                            ? Stream.empty()
                            : Stream.of(firstFeatureChange);
                default:
                    return Stream.of(firstFeatureChange);
            }
        }).forEach(newFlag::addFixSuggestion);
    }

    /**
     * Merge {@link FlaggedObject}s to make them OSMified.
     *
     * @param objectsMap
     *            {@link Map} of {@link FlaggedObject}s by a {@link String} unique OSM ID key
     * @param newFlag
     *            {@link CheckFlag} to add the converted {@link FlaggedObject}s to
     */
    private static void convertFlaggedObjects(final Map<String, Set<FlaggedObject>> objectsMap,
            final CheckFlag newFlag)
    {
        // Go through each entry, ignoring synthetic points
        objectsMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase(NULL_IDENTIFIERS))
                .flatMap(entry ->
                {
                    switch (ItemType.valueOf(entry.getValue().iterator().next().getProperties()
                            .get(FlaggedObject.ITEM_TYPE_TAG).toUpperCase()))
                    {
                        // Merge edges creating a new edge with a osm+000000 id, the properties of
                        // the first edge, and a concatenated geometry from all the edges
                        case EDGE:
                            // Sort the edges and concatenate their geometries
                            final Optional<PolyLine> concatenatedPolyline = entry.getValue()
                                    .stream()
                                    .sorted(Comparator
                                            .comparing(FlaggedObject::getUniqueIdentifier))
                                    .map(flaggedObject -> new PolyLine(flaggedObject.getGeometry()))
                                    .reduce(PolyLine::append);
                            if (concatenatedPolyline.isPresent())
                            {
                                // Use the properties of the first edge, assuming they ar the same
                                // for all the edges
                                final FlaggedObject object = entry.getValue().iterator().next();
                                final Map<String, String> properties = new HashMap<>(
                                        object.getProperties());
                                // Remove meta data properties that will be automatically re-added.
                                properties.remove(FlaggedObject.OSM_IDENTIFIER_TAG);
                                properties.remove(FlaggedObject.ITEM_TYPE_TAG);
                                properties.remove(FlaggedObject.ITEM_IDENTIFIER_TAG);
                                return Stream.of(new FlaggedPolyline(new CompleteEdge(
                                        Long.valueOf(object.getProperties()
                                                .get(FlaggedObject.OSM_IDENTIFIER_TAG) + "000000"),
                                        concatenatedPolyline.get(), properties, null, null, null)));
                            }
                            throw new CoreException("Unable to concatenate Edges.");
                        // Rmove points that are already flagged as nodes
                        case POINT:
                            return objectsMap.containsKey(entry.getKey().replace("Point", "Node"))
                                    ? Stream.empty()
                                    : entry.getValue().stream();
                        default:
                            return entry.getValue().stream();
                    }
                }).forEach(newFlag::addObject);
    }

    private OpenStreetMapCheckFlagConverter()
    {
    }
}
