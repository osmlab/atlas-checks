package org.openstreetmap.atlas.checks.flag;

import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * A flag for a {@link Relation}
 *
 * @author sayas01
 */
public class FlaggedRelation extends FlaggedObject
{
    private static final long serialVersionUID = 81887932468503688L;
    private static final RelationOrAreaToMultiPolygonConverter MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final Logger logger = LoggerFactory.getLogger(FlaggedRelation.class);
    private final String country;
    private final Map<String, String> properties;
    private final Relation relation;
    private final MultiPolygon multipolygonGeometry;

    public FlaggedRelation(final Relation relation)
    {
        this.relation = relation;
        this.properties = initProperties(relation);
        this.country = initCountry(relation);
        this.multipolygonGeometry = this.relationGeometry(relation);
    }

    public FlaggedRelation(final Relation relation, final MultiPolygon geoJsonGeometry)
    {
        this.relation = relation;
        this.properties = initProperties(relation);
        this.country = initCountry(relation);
        this.multipolygonGeometry = geoJsonGeometry;
    }

    /**
     * A GeoJSON representation of the flagged object.
     *
     * @param flagIdentifier
     *            We always will want to know the id of the flag associated with this flag object.
     * @return GeoJSON representation of the flagged object.
     */
    @Override
    public JsonObject asGeoJsonFeature(final String flagIdentifier)
    {
        final JsonObject featureProperties = this.relation.getGeoJsonProperties();
        featureProperties.addProperty("flag:id", flagIdentifier);
        featureProperties.addProperty("flag:type", FlaggedRelation.class.getSimpleName());
        return GeoJsonUtils.feature(this.multipolygonGeometry.asGeoJsonGeometry(),
                featureProperties);
    }

    /**
     * @return The bounds of the object.
     */
    @Override
    public Rectangle bounds()
    {
        return this.relation.bounds();
    }

    @Override
    public boolean equals(final Object other)
    {
        return super.equals(other);
    }

    @Override
    public FlaggedObject getAsCompleteFlaggedObject()
    {
        return new FlaggedRelation(CompleteRelation.from(this.relation), this.multipolygonGeometry);
    }

    /**
     * @return ISO country code of the country
     */
    @Override
    public String getCountry()
    {
        return this.country;
    }

    /**
     * As relations itself do not have a geometry, the geometry of FlaggedRelation is set to null.
     *
     * @return flagged geometry
     */
    @Override
    public Iterable<Location> getGeometry()
    {
        return null;
    }

    /**
     * @return flag key-value property map
     */
    @Override
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * Helper method to check FlaggedRelation is of multipolygon type
     *
     * @return true if the flagged relation is a multipolygon
     */
    public boolean isMultipolygonRelation()
    {
        return Validators.isOfType(this.relation, RelationTypeTag.class,
                RelationTypeTag.MULTIPOLYGON);
    }

    /**
     * @return list of all members of the relation
     */
    public RelationMemberList members()
    {
        return this.relation.members();
    }

    @Override
    protected Optional<AtlasObject> getObject()
    {
        return Optional.of(this.relation);
    }

    private String initCountry(final AtlasObject object)
    {
        final Map<String, String> tags = object.getTags();
        if (tags.containsKey(ISOCountryTag.KEY))
        {
            return tags.get(ISOCountryTag.KEY);
        }
        return ISOCountryTag.COUNTRY_MISSING;
    }

    /**
     * Populate properties of relation
     *
     * @param relation
     * @return map of relation tags with additional key-value properties
     */
    private Map<String, String> initProperties(final Relation relation)
    {
        final Map<String, String> tags = relation.getTags();
        tags.put(ITEM_IDENTIFIER_TAG, relation.getIdentifier() + "");
        tags.put(OSM_IDENTIFIER_TAG, relation.getOsmIdentifier() + "");
        tags.put(ITEM_TYPE_TAG, "Relation");
        return tags;
    }

    private MultiPolygon relationGeometry(final Relation relation)
    {
        if (relation.isMultiPolygon())
        {
            try
            {
                return MULTI_POLYGON_CONVERTER.convert(relation);
            }
            catch (final CoreException exception)
            {
                final String message = String.format("%s - %s",
                        exception.getClass().getSimpleName(), exception.getMessage());
                logger.error("Unable to recreate multipolygon for relation {}. {}",
                        relation.getIdentifier(), message);
                return MultiPolygon.forOuters(relation.bounds());
            }
        }
        // Otherwise, we'll fall back to just providing the properties of the relation with the
        // bounding box as a polygon geometry.
        else
        {
            return MultiPolygon.forOuters(relation.bounds());
        }
    }
}
