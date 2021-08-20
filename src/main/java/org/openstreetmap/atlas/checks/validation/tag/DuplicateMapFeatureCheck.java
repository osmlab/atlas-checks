package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.AmenityTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.LeisureTag;
import org.openstreetmap.atlas.tags.ShopTag;
import org.openstreetmap.atlas.tags.SportTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * This check is to detect and flag node, way or relation which have duplicate map features in areas
 * or connected locations (Osmose 4080).
 *
 * @author Xiaohong Tang
 */
public class DuplicateMapFeatureCheck extends BaseCheck<Object>
{
    private static final String DUPLICATE_FEATURE_INSTRUCTIONS = "{0} and {1} are duplicate feature {2}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(DUPLICATE_FEATURE_INSTRUCTIONS);

    private static final List<String> Features_Tags_Should_Represent_Only_Once = Arrays
            .asList(AmenityTag.KEY, LeisureTag.KEY, BuildingTag.KEY, ShopTag.KEY);

    private static final List<ItemType> NodeItemsTypesToCompare = Arrays.asList(ItemType.NODE,
            ItemType.POINT);

    private static final List<ItemType> WayNodeItemsTypesToCompare = Arrays.asList(ItemType.AREA,
            ItemType.EDGE, ItemType.LINE, ItemType.NODE, ItemType.POINT);

    private static final long serialVersionUID = 7595976166632982218L;

    private final List<String> featuresTagsShouldRepresentOnlyOnce;

    public DuplicateMapFeatureCheck(final Configuration configuration)
    {
        super(configuration);
        this.featuresTagsShouldRepresentOnlyOnce = this.configurationValue(configuration,
                "features.tags.should.represent.only.once.in.area",
                Features_Tags_Should_Represent_Only_Once);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Area || object instanceof Edge || object instanceof Relation)
                && !isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        this.markAsFlagged(object.getOsmIdentifier());

        if (object.getOsmTags().isEmpty())
        {
            return Optional.empty();
        }

        Optional<Tuple<Set<String>, Map<String, String>>> duplicateFeatures = Optional.empty();
        String objectInstructionString = "";

        if (object instanceof Area)
        {
            objectInstructionString = "Area type Way " + Long.toString(object.getOsmIdentifier());

            final Area area = (Area) object;

            duplicateFeatures = this.checkArea(area);
        }

        if (object instanceof Edge)
        {
            objectInstructionString = "Way " + Long.toString(object.getOsmIdentifier());

            final Edge edge = (Edge) object;

            duplicateFeatures = this.checkEdge(edge);
        }

        if (object instanceof Relation)
        {
            objectInstructionString = "Relation " + Long.toString(object.getOsmIdentifier());

            final Relation relation = (Relation) object;

            duplicateFeatures = this.checkRelation(relation);
        }

        if (duplicateFeatures.isPresent())
        {
            final Set<String> duplicateFeatureIds = duplicateFeatures.get().getFirst();
            final Map<String, String> duplicateFeaturesTags = duplicateFeatures.get().getSecond();
            final String duplicateFeatureString = this.getStringForList(duplicateFeatureIds);

            return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                    objectInstructionString, duplicateFeatureString, duplicateFeaturesTags)));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if the {@link Area} has duplicate feature.
     *
     * @param area
     *            the area to check
     * @return duplicate feature and tags if the area has duplicate feature.
     */
    private Optional<Tuple<Set<String>, Map<String, String>>> checkArea(final Area area)
    {
        final Set<String> duplicateFeaturesIds = new HashSet<>();
        final Map<String, String> duplicateFeaturesTags = new HashMap<>();

        try
        {
            final Iterable<AtlasEntity> entities = area.getAtlas().entitiesWithin(area.bounds(),
                    entity -> NodeItemsTypesToCompare.contains(entity.getType()));

            for (final AtlasEntity entity : entities)
            {
                final Optional<Tuple<String, Map<String, String>>> duplicateFeature = this
                        .verifyDuplicateFeature(area, entity);

                if (duplicateFeature.isPresent())
                {
                    duplicateFeaturesIds.add(duplicateFeature.get().getFirst());
                    duplicateFeaturesTags.putAll(duplicateFeature.get().getSecond());
                }
            }

            if (!duplicateFeaturesIds.isEmpty())
            {
                final Tuple<Set<String>, Map<String, String>> duplicateFeatures = Tuple
                        .createTuple(duplicateFeaturesIds, duplicateFeaturesTags);

                return Optional.of(duplicateFeatures);
            }
        }
        catch (final Exception ignored)
        {
            // Do Nothing
        }
        return Optional.empty();
    }

    /**
     * Checks if the {@link Edge} has duplicate feature.
     *
     * @param edge
     *            the edge to check
     * @return duplicate feature and tags if the edge has duplicate feature.
     */
    private Optional<Tuple<Set<String>, Map<String, String>>> checkEdge(final Edge edge)
    {
        final Set<String> duplicateFeaturesIds = new HashSet<>();
        final Map<String, String> duplicateFeaturesTags = new HashMap<>();

        final Set<Node> nodes = edge.connectedNodes();
        for (final Node node : nodes)
        {
            final Optional<Tuple<String, Map<String, String>>> duplicateFeature = this
                    .verifyDuplicateFeature(edge, node);

            if (duplicateFeature.isPresent())
            {
                duplicateFeaturesIds.add(duplicateFeature.get().getFirst());
                duplicateFeaturesTags.putAll(duplicateFeature.get().getSecond());
            }
        }

        if (!duplicateFeaturesIds.isEmpty())
        {
            final Tuple<Set<String>, Map<String, String>> duplicateFeatures = Tuple
                    .createTuple(duplicateFeaturesIds, duplicateFeaturesTags);

            return Optional.of(duplicateFeatures);
        }

        return Optional.empty();
    }

    /**
     * Checks if the {@link Relation} has duplicate feature.
     *
     * @param relation
     *            the relation to check
     * @return duplicate feature and tags if the relation has duplicate feature.
     */
    private Optional<Tuple<Set<String>, Map<String, String>>> checkRelation(final Relation relation)
    {
        final Set<String> duplicateFeaturesIds = new HashSet<>();
        final Map<String, String> duplicateFeaturesTags = new HashMap<>();

        final List<RelationMember> members = relation.members().stream()
                .filter(m -> WayNodeItemsTypesToCompare.contains(m.getEntity().getType()))
                .collect(Collectors.toList());

        for (final RelationMember member : members)
        {
            final Optional<Tuple<String, Map<String, String>>> duplicateFeature = this
                    .verifyDuplicateFeature(relation, member.getEntity());

            if (duplicateFeature.isPresent())
            {
                duplicateFeaturesIds.add(duplicateFeature.get().getFirst());
                duplicateFeaturesTags.putAll(duplicateFeature.get().getSecond());
            }
        }

        if (relation.isGeometric())
        {
            try
            {
                final Iterable<AtlasEntity> entities = relation.getAtlas().entitiesWithin(
                        relation.bounds(),
                        entity -> WayNodeItemsTypesToCompare.contains(entity.getType()));

                for (final AtlasEntity entity : entities)
                {
                    if (this.isRelationMember(relation, entity))
                    {
                        continue;
                    }

                    final Optional<Tuple<String, Map<String, String>>> duplicateFeature = this
                            .verifyDuplicateFeature(relation, entity);

                    if (duplicateFeature.isPresent())
                    {
                        duplicateFeaturesIds.add(duplicateFeature.get().getFirst());
                        duplicateFeaturesTags.putAll(duplicateFeature.get().getSecond());
                    }
                }
            }
            catch (final Exception ignored)
            {
                // Do Nothing
            }
        }

        if (!duplicateFeaturesIds.isEmpty())
        {
            final Tuple<Set<String>, Map<String, String>> duplicateFeatures = Tuple
                    .createTuple(duplicateFeaturesIds, duplicateFeaturesTags);

            return Optional.of(duplicateFeatures);
        }

        return Optional.empty();
    }

    private String getStringForList(final Set<String> list)
    {
        return list.toString().replace("[", "").replace("]", "");
    }

    /**
     * Checks if two set tags have the key but different value
     *
     * @param firstTags
     *            the first Osm tags to check
     * @param secondTags
     *            the second Osm tags to check
     * @param key
     *            the key to check
     * @return true if two set tags have the key but different value.
     */
    private boolean hasDifferentValueOfKey(final Map<String, String> firstTags,
            final Map<String, String> secondTags, final String key)
    {
        return firstTags.get(key) != null && secondTags.get(key) != null
                && !firstTags.get(key).equals(secondTags.get(key));
    }

    /**
     * Checks if two set tags have only building tag without name for features which should
     * represent once
     *
     * @param firstTags
     *            the first Osm tags to check
     * @param secondTags
     *            the second Osm tags to check
     * @param featuresTaggedTwice
     *            the features tags in both sets of tags for features which should represent once
     * @return true if two set tags have only building tag without name for features which should
     *         represent once.
     */
    private boolean hasOnlyBuildingWithoutName(final Map<String, String> firstTags,
            final Map<String, String> secondTags, final Map<String, String> featuresTaggedTwice)
    {
        return featuresTaggedTwice.keySet().contains(BuildingTag.KEY)
                && featuresTaggedTwice.keySet().size() == 1
                && (firstTags.get(NameTag.KEY) == null || secondTags.get(NameTag.KEY) == null);
    }

    /**
     * Checks if the {@link AtlasEntity} is a member of the {@link Relation}.
     *
     * @param relation
     *            {@link Relation} to check
     * @param entity
     *            {@link AtlasEntity} to check
     * @return true if the AtlasEntity is a member of the Relation
     */
    private boolean isRelationMember(final Relation relation, final AtlasEntity entity)
    {
        final List<RelationMember> members = relation.members().stream()
                .collect(Collectors.toList());

        for (final RelationMember relationMember : members)
        {
            if (relationMember.getEntity().getOsmIdentifier() == entity.getOsmIdentifier())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * check if two entities are duplicate feature
     *
     * @param firstEntity
     *            the first entity to check
     * @param secondEntity
     *            the second entity to check
     * @return duplicate feature and tags if the two entities are duplicate feature.
     */
    private Optional<Tuple<String, Map<String, String>>> verifyDuplicateFeature(
            final AtlasEntity firstEntity, final AtlasEntity secondEntity)
    {
        final String duplicateFeatureStr;

        if (firstEntity.getOsmTags().isEmpty() || secondEntity.getOsmTags().isEmpty())
        {
            return Optional.empty();
        }

        if (firstEntity.getOsmTags().equals(secondEntity.getOsmTags()))
        {
            final String firstEntityString = ((firstEntity instanceof Relation) ? "Relation "
                    : "Way ") + Long.toString(firstEntity.getOsmIdentifier());

            if (secondEntity instanceof LocationItem)
            {
                duplicateFeatureStr = "Node " + Long.toString(secondEntity.getOsmIdentifier())
                        + " with the same tags as " + firstEntityString;
            }
            else
            {
                duplicateFeatureStr = "Way " + Long.toString(secondEntity.getOsmIdentifier())
                        + " with the same tags as " + firstEntityString;
            }

            final Tuple<String, Map<String, String>> duplicateFeature = Tuple
                    .createTuple(duplicateFeatureStr, firstEntity.getOsmTags());

            return Optional.of(duplicateFeature);

        }
        else
        {
            final Optional<Map<String, String>> featuresTaggedTwice = this
                    .verifyObjectTaggedTwice(firstEntity.getTags(), secondEntity.getOsmTags());

            if (featuresTaggedTwice.isPresent())
            {
                if (secondEntity instanceof LocationItem)
                {
                    duplicateFeatureStr = "Node " + Long.toString(secondEntity.getOsmIdentifier());
                }
                else
                {
                    duplicateFeatureStr = "Way " + Long.toString(secondEntity.getOsmIdentifier());
                }

                final Tuple<String, Map<String, String>> duplicateFeature = Tuple
                        .createTuple(duplicateFeatureStr, featuresTaggedTwice.get());

                return Optional.of(duplicateFeature);
            }
        }

        return Optional.empty();
    }

    /**
     * Checks if two set tags have object tagged twice
     *
     * @param firstTags
     *            the first Osm tags to check
     * @param secondTags
     *            the second Osm tags to check
     * @return duplicate feature tags if two set tags have object tagged twice.
     */
    private Optional<Map<String, String>> verifyObjectTaggedTwice(
            final Map<String, String> firstTags, final Map<String, String> secondTags)
    {
        final Map<String, String> inFirstTagsFeatureAllowRepresentOnce = firstTags.entrySet()
                .stream()
                .filter(map -> this.featuresTagsShouldRepresentOnlyOnce.contains(map.getKey()))
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
        final Map<String, String> inSecondTagsFeatureAllowRepresentOnce = secondTags.entrySet()
                .stream()
                .filter(map -> this.featuresTagsShouldRepresentOnlyOnce.contains(map.getKey()))
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

        final Map<String, String> featuresTaggedTwice = inFirstTagsFeatureAllowRepresentOnce
                .entrySet().stream()
                .filter(map -> map.getValue()
                        .equals(inSecondTagsFeatureAllowRepresentOnce.get(map.getKey())))
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

        if (featuresTaggedTwice.isEmpty())
        {
            return Optional.empty();
        }

        // only same building tags without name are not duplicate feature
        if (this.hasOnlyBuildingWithoutName(firstTags, secondTags, featuresTaggedTwice))
        {
            return Optional.empty();
        }

        // amenity or leisure with different building tags are not duplicate feature
        if (this.hasDifferentValueOfKey(firstTags, secondTags, BuildingTag.KEY))
        {
            return Optional.empty();
        }

        // leisure=pitch and different sport=* tags are not duplicate feature
        // leisure=track and different sport=* tags are not duplicate feature
        if (LeisureTag.PITCH.name().toLowerCase().equals(featuresTaggedTwice.get(LeisureTag.KEY))
                || LeisureTag.TRACK.name().toLowerCase()
                        .equals(featuresTaggedTwice.get(LeisureTag.KEY)))
        {
            if (Objects.equals(firstTags.get(SportTag.KEY), secondTags.get(SportTag.KEY)))
            {
                if (firstTags.get(SportTag.KEY) != null)
                {
                    featuresTaggedTwice.put(SportTag.KEY, firstTags.get(SportTag.KEY));
                }
                return Optional.of(featuresTaggedTwice);
            }
            else if (this.hasDifferentValueOfKey(firstTags, secondTags, SportTag.KEY))
            {
                return Optional.empty();
            }
        }

        if (Objects.equals(firstTags.get(NameTag.KEY), secondTags.get(NameTag.KEY)))
        {
            if (firstTags.get(NameTag.KEY) != null)
            {
                featuresTaggedTwice.put(NameTag.KEY, firstTags.get(NameTag.KEY));
            }
            return Optional.of(featuresTaggedTwice);
        }
        return Optional.empty();
    }
}
