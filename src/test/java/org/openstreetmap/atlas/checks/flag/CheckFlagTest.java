package org.openstreetmap.atlas.checks.flag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;

import com.google.gson.JsonObject;

/**
 * Test for {@link CheckFlag}.
 *
 * @author mkalender
 * @author sayas01
 */
public class CheckFlagTest
{
    private static final String GEO_JSON_FEATURE_STRING = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-126.031007,31.33531],[-126.031007,36.390535],[-121.009566,36.390535],[-121.009566,31.33531],[-126.031007,31.33531]]]},\"properties\":{\"flag:type\":\"CheckFlag\",\"flag:id\":\"a-identifier\",\"flag:instructions\":\"1. first instruction\\n2. second instruction\"}}";

    @Rule
    public CheckFlagTestRule setup = new CheckFlagTestRule();

    private static CheckFlag deserialize(final byte[] flagAsBytes)
            throws IOException, ClassNotFoundException
    {
        final ByteArrayInputStream byteOutputStream = new ByteArrayInputStream(flagAsBytes);
        final ObjectInputStream objectOutputStream = new ObjectInputStream(byteOutputStream);
        return (CheckFlag) objectOutputStream.readObject();
    }

    private static byte[] serialize(final CheckFlag flag) throws IOException
    {
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
        objectOutputStream.writeObject(flag);
        objectOutputStream.close();
        return byteOutputStream.toByteArray();
    }

    private static void testSerialization(final CheckFlag flag)
            throws ClassNotFoundException, IOException
    {
        final CheckFlag deserializedFlag = deserialize(serialize(flag));
        Assert.assertEquals(flag, deserializedFlag);
    }

    @Test
    public void testAllFlaggedObjects()
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        this.setup.getAtlasWithRelations().entities().forEach(flag::addObject);
        // Tests if both the relations are added to flag
        Assert.assertEquals(2, flag.getFlaggedRelations().size());
        // Tests if entities other than relations are also flagged
        Assert.assertEquals(15, flag.getFlaggedObjects().size());
    }

    @Test
    public void testAsGeoJsonFeature()
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        flag.addInstruction("first instruction");
        flag.addInstruction("second instruction");
        this.setup.getAtlas().entities().forEach(flag::addObject);

        final JsonObject geoJsonFeature = flag.asGeoJsonFeature();
        final String geoJsonFeatureString = geoJsonFeature.toString();

        Assert.assertEquals(GEO_JSON_FEATURE_STRING, geoJsonFeatureString);
    }

    @Test
    public void testFlagToFeature()
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        this.setup.getAtlasWithRelations().entities().forEach(flag::addObject);
        Assert.assertEquals(15, CheckFlagEvent.flagToFeature(flag, new HashMap<>())
                .get("properties").getAsJsonObject().get("feature_count").getAsLong());
    }

    @Test
    public void testFlaggedRelations()
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        this.setup.getAtlasWithRelations().relations().forEach(flag::addObject);
        // Tests if both the relations are added to flag
        Assert.assertEquals(2, flag.getFlaggedRelations().size());
    }

    @Test
    public void testMakeComplete()
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        flag.addInstruction("first instruction");
        flag.addInstruction("second instruction");
        this.setup.getAtlas().entities().forEach(flag::addObject);

        final CheckFlag otherFlag = new CheckFlag("a-identifier");
        this.setup.getAtlasWithRelations().entities().forEach(otherFlag::addObject);

        flag.makeComplete();
        otherFlag.makeComplete();

        flag.getFlaggedObjects().stream().forEach(flaggedObject ->
        {
            Assert.assertTrue(flaggedObject.getObject().isPresent());
            flaggedObject.getObject().ifPresent(object ->
            {
                Assert.assertTrue(object instanceof CompleteEntity);
            });
        });

        otherFlag.getFlaggedObjects().stream().forEach(flaggedObject ->
        {
            Assert.assertTrue(flaggedObject.getObject().isPresent());
            flaggedObject.getObject().ifPresent(object ->
            {
                Assert.assertTrue(object instanceof CompleteEntity);
            });
        });
    }

    @Test
    public void testMembersOfFlaggedRelations()
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        this.setup.getAtlasWithRelations().entities().forEach(flag::addObject);
        // Checks if members of flagged relations are added
        final FlaggedRelation flaggedRelation = (FlaggedRelation) flag.getFlaggedRelations()
                .iterator().next();
        Assert.assertFalse(flaggedRelation.members().isEmpty());
        // Tests if list of geometriesWithProperties are populated for the flaggedObjects
        Assert.assertEquals(13, flag.getGeometryWithProperties().size());
    }

    @Test
    public void testSerializationWithAllFields() throws IOException, ClassNotFoundException
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        flag.setChallengeName("sample-challenge");
        flag.addInstruction("first instruction");
        flag.addInstruction("second instruction");
        this.setup.getAtlas().entities().forEach(entity -> flag.addObject(entity));
        testSerialization(flag);
    }

    @Test
    public void testSerializationWithChallenge() throws IOException, ClassNotFoundException
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        flag.setChallengeName("sample-challenge");
        testSerialization(flag);
    }

    @Test
    public void testSerializationWithIdentifier() throws IOException, ClassNotFoundException
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        testSerialization(flag);
    }

    @Test
    public void testSerializationWithInstructions() throws IOException, ClassNotFoundException
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        flag.addInstruction("first instruction");
        flag.addInstruction("second instruction");
        testSerialization(flag);
    }

    @Test
    public void testSerializationWithNullIdentifier() throws IOException, ClassNotFoundException
    {
        final CheckFlag flag = new CheckFlag(null);
        testSerialization(flag);
    }

    @Test
    public void testSerializationWithObjects() throws IOException, ClassNotFoundException
    {
        final CheckFlag flag = new CheckFlag("a-identifier");
        this.setup.getAtlas().entities().forEach(flag::addObject);
        testSerialization(flag);
    }
}
