package org.openstreetmap.atlas.checks.flag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link CheckFlag}.
 *
 * @author mkalender
 */
public class CheckFlagTest
{
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
        this.setup.getAtlas().entities().forEach(entity -> flag.addObject(entity));
        testSerialization(flag);
    }
}
