package org.openstreetmap.atlas.checks.flag.serializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.geography.geojson.parser.domain.feature.Feature;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.LineString;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiPolygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Point;

/**
 * A GeoJson {@link Feature} to an {@link AtlasEntity} converter.
 *
 * @author bbreithaupt
 */
public final class GeoJsonFeatureToAtlasEntityConverter
{
    private static final List<String> metaProperties = Arrays.asList(GeoJsonUtils.ITEM_TYPE,
            GeoJsonUtils.IDENTIFIER, GeoJsonUtils.OSM_IDENTIFIER, "relations", "members", "flag:id",
            "flag:type");
    private static final Long DEFAULT_IDENTIFIER = 0L;

    /**
     * Attempt to convert a GeoJson {@link Feature} to an {@link AtlasEntity}.
     *
     * @param feature
     *            {@link Feature}
     * @return an {@link Optional} {@link AtlasEntity}
     */
    public static Optional<AtlasEntity> convert(final Feature feature)
    {
        switch (ItemType.valueOf(feature.getProperties().asMap().get(GeoJsonUtils.ITEM_TYPE)
                .toString().toUpperCase()))
        {
            case AREA:
                return Optional.of(convertArea(feature));
            case EDGE:
                return Optional.of(convertEdge(feature));
            case LINE:
                return Optional.of(convertLine(feature));
            case NODE:
                return Optional.of(convertNode(feature));
            case POINT:
                return Optional.of(convertPoint(feature));
            case RELATION:
                return Optional.of(convertRelation(feature));
            default:
                return Optional.empty();
        }
    }

    /**
     * Convert a {@link Feature} to an {@link Area}. The are will not include an relation
     * identifiers.
     *
     * @param feature
     *            {@link Feature}
     * @return {@link Area}
     */
    private static Area convertArea(final Feature feature)
    {
        return new CompleteArea(
                Long.valueOf((String) feature.getProperties().asMap().get(GeoJsonUtils.IDENTIFIER)),
                new Polygon(((LineString) feature.getGeometry()).toAtlasGeometry()),
                getProperties(feature), Collections.emptySet());
    }

    /**
     * Convert a {@link Feature} to an {@link Edge}. The edge will have fake start and end node
     * identifiers and no relation identifiers.
     *
     * @param feature
     *            {@link Feature}
     * @return {@link Edge}
     */
    private static Edge convertEdge(final Feature feature)
    {
        return new CompleteEdge(
                Long.valueOf((String) feature.getProperties().asMap().get(GeoJsonUtils.IDENTIFIER)),
                ((LineString) feature.getGeometry()).toAtlasGeometry(), getProperties(feature),
                DEFAULT_IDENTIFIER, DEFAULT_IDENTIFIER, Collections.emptySet());
    }

    /**
     * Convert a {@link Feature} to a {@link Line}. The line will not have any relation identifiers.
     *
     * @param feature
     *            {@link Feature}
     * @return {@link Line}
     */
    private static Line convertLine(final Feature feature)
    {
        return new CompleteLine(
                Long.valueOf((String) feature.getProperties().asMap().get(GeoJsonUtils.IDENTIFIER)),
                ((LineString) feature.getGeometry()).toAtlasGeometry(), getProperties(feature),
                Collections.emptySet());
    }

    /**
     * Convert a {@link Feature} to a {@link Node}. The node will not have any in edge, out edge, or
     * relation identifiers.
     *
     * @param feature
     *            {@link Feature}
     * @return {@link Node}
     */
    private static Node convertNode(final Feature feature)
    {
        return new CompleteNode(
                Long.valueOf((String) feature.getProperties().asMap().get(GeoJsonUtils.IDENTIFIER)),
                ((Point) feature.getGeometry()).toAtlasGeometry(), getProperties(feature),
                new TreeSet<>(), new TreeSet<>(), Collections.emptySet());
    }

    /**
     * Convert a {@link Feature} to a {@link org.openstreetmap.atlas.geography.atlas.items.Point}.
     * The point will not have any relation identifiers.
     *
     * @param feature
     *            {@link Feature}
     * @return {@link org.openstreetmap.atlas.geography.atlas.items.Point}
     */
    private static org.openstreetmap.atlas.geography.atlas.items.Point convertPoint(
            final Feature feature)
    {
        return new CompletePoint(
                Long.valueOf((String) feature.getProperties().asMap().get(GeoJsonUtils.IDENTIFIER)),
                ((Point) feature.getGeometry()).toAtlasGeometry(), getProperties(feature),
                Collections.emptySet());
    }

    /**
     * Convert a {@link Feature} to a {@link Relation}. The relation will not have any parent
     * relation identifiers.
     *
     * @param feature
     *            {@link Feature}
     * @return {@link Relation}
     */
    private static Relation convertRelation(final Feature feature)
    {
        final RelationBean members = new RelationBean();
        ((List) feature.getProperties().asMap().get("members")).forEach(member ->
        {
            final Map<String, Object> memberMap = (Map<String, Object>) member;
            members.add(new RelationBean.RelationBeanItem(
                    ((Double) memberMap.get(GeoJsonUtils.IDENTIFIER)).longValue(),
                    (String) memberMap.get("role"),
                    ItemType.valueOf((String) memberMap.get(GeoJsonUtils.ITEM_TYPE))));
        });

        final Long identifier = Long
                .valueOf((String) feature.getProperties().asMap().get(GeoJsonUtils.IDENTIFIER));

        return new CompleteRelation(identifier, getProperties(feature),
                ((MultiPolygon) feature.getGeometry()).toAtlasGeometry().bounds(), members,
                Collections.singletonList(identifier), members,
                Long.valueOf(
                        (String) feature.getProperties().asMap().get(GeoJsonUtils.OSM_IDENTIFIER)),
                Collections.emptySet());
    }

    /**
     * Get the properties of a {@link Feature} with certain meta properties removed.
     *
     * @param feature
     *            {@link Feature}
     * @return {@link Map} of {@Link String} keys and values
     */
    private static Map<String, String> getProperties(final Feature feature)
    {
        return feature.getProperties().asMap().entrySet().stream()
                .filter(entry -> !metaProperties.contains(entry.getKey()))
                .filter(entry -> entry.getValue() instanceof String).collect(HashMap::new,
                        (map, entry) -> map.put(entry.getKey(), (String) entry.getValue()),
                        Map::putAll);
    }

    private GeoJsonFeatureToAtlasEntityConverter()
    {
    }
}
