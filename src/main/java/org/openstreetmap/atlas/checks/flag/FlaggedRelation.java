package org.openstreetmap.atlas.checks.flag;

import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

import com.google.gson.JsonObject;

/**
 * A flag for a {@link Relation}
 *
 * @author sayas01
 */
public class FlaggedRelation extends FlaggedObject
{
    private final Relation relation;
    private final Map<String, String> properties;

    public FlaggedRelation(final Relation relation)
    {
        this.relation = relation;
        this.properties = initProperties(relation);
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
        final JsonObject feature = this.relation.asGeoJsonFeature();
        final JsonObject properties = feature.getAsJsonObject("properties");
        properties.addProperty("flag:id", flagIdentifier);
        properties.addProperty("flag:type", FlaggedPolyline.class.getSimpleName());
        return feature;
    }

    /**
     * @return The bounds of the object.
     */
    @Override
    public Rectangle bounds()
    {
        return null;
    }

    /**
     * @return list of all members of the relation
     */
    public RelationMemberList members()
    {
        return this.relation.members();
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
}
