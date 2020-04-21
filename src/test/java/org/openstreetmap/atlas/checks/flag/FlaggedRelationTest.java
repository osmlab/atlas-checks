package org.openstreetmap.atlas.checks.flag;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.COORDINATES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.GEOMETRY;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.google.gson.JsonArray;

/**
 * Unit tests for {@link FlaggedRelation}.
 *
 * @author bbreithaupt
 */
public class FlaggedRelationTest
{
    @Rule
    public final FlaggedRelationTestRule setup = new FlaggedRelationTestRule();

    @Test
    public void completeGeometryBadMultipolygonTest()
    {
        final FlaggedRelation flaggedRelation = new FlaggedRelation(
                this.setup.badMultipolygonAtlas().relation(1000000L));
        Assert.assertTrue(flaggedRelation.isMultipolygonRelation());
        final JsonArray atlasBackedGeometry = flaggedRelation.asGeoJsonFeature("1").get(GEOMETRY)
                .getAsJsonObject().get(COORDINATES).getAsJsonArray();
        final JsonArray completeGeometry = flaggedRelation.getAsCompleteFlaggedObject()
                .asGeoJsonFeature("1").get(GEOMETRY).getAsJsonObject().get(COORDINATES)
                .getAsJsonArray();
        Assert.assertEquals(atlasBackedGeometry, completeGeometry);
    }

    @Test
    public void completeGeometryBoundsTest()
    {
        final FlaggedRelation flaggedRelation = new FlaggedRelation(
                this.setup.circuitAtlas().relation(1000000L));
        final JsonArray atlasBackedGeometry = flaggedRelation.asGeoJsonFeature("1").get(GEOMETRY)
                .getAsJsonObject().get(COORDINATES).getAsJsonArray();
        final JsonArray completeGeometry = flaggedRelation.getAsCompleteFlaggedObject()
                .asGeoJsonFeature("1").get(GEOMETRY).getAsJsonObject().get(COORDINATES)
                .getAsJsonArray();
        Assert.assertEquals(atlasBackedGeometry, completeGeometry);
    }

    @Test
    public void completeGeometryMultipolygonTest()
    {
        final FlaggedRelation flaggedRelation = new FlaggedRelation(
                this.setup.multipolygonAtlas().relation(1000000L));
        Assert.assertTrue(flaggedRelation.isMultipolygonRelation());
        final JsonArray atlasBackedGeometry = flaggedRelation.asGeoJsonFeature("1").get(GEOMETRY)
                .getAsJsonObject().get(COORDINATES).getAsJsonArray();
        final JsonArray completeGeometry = flaggedRelation.getAsCompleteFlaggedObject()
                .asGeoJsonFeature("1").get(GEOMETRY).getAsJsonObject().get(COORDINATES)
                .getAsJsonArray();
        Assert.assertEquals(atlasBackedGeometry, completeGeometry);
    }
}
