package org.openstreetmap.atlas.checks.utility;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class for {@link CompressionUtilities}.
 *
 * @author Taylor Smock
 */
@RunWith(Parameterized.class)
public class CompressionUtilitiesTest
{
    private static final String TESTING_STREAM = "Testing stream\n";

    private final String name;
    private final byte[] byteInput;

    /**
     * Get parameters for the test
     *
     * @return The parameters for the test in an array ({Name string, data byte[]})
     */
    @Parameters(name = "{index} - {0}")
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][] { { "Standard", TESTING_STREAM.getBytes() },
                { "GZipped",
                        Base64.getDecoder().decode(
                                "H4sICIADNF8AA3RoaXMAC0ktLsnMS1coLilKTczlAgBqt7NlDwAAAA==") },
                { "Zip", Base64.getDecoder()
                        .decode("UEsDBAoAAAAAAFFIDFFqt7NlDwAAAA8AAAAEABwA"
                                + "dGhpc1VUCQADigQ0XwcFNF91eAsAAQT1AQAABAAAAABUZXN0aW5nIHN0cm"
                                + "VhbQpQSwECHgMKAAAAAABRSAxRarezZQ8AAAAPAAAABAAYAAAAAAABAAAA"
                                + "pIEAAAAAdGhpc1VUBQADigQ0X3V4CwABBPUBAAAEAAAAAFBLBQYAAAAAAQ"
                                + "ABAEoAAABNAAAAAAA=") },
                { "GZipped Zip",
                        Base64.getDecoder().decode("H4sICDcFNF8AA3RoaXMuemlwAAvwZmbhYgCBQA+e"
                                + "wKztm1P5gWwQZmGQYSjJyCwODeFkYO5iMYlnZzWJL63gZmBk+coIkgaCkN"
                                + "Tiksy8dIXikqLUxFyuAG9GJjlmXMZJgMWBWhmWNIJYEMNZIYajGRzgzcoG"
                                + "Uc3I4AWkfcF6AYUTZeOtAAAA") } });
    }

    /**
     * Initialize the test for parameterized testing
     *
     * @param name
     *            The name of the test
     * @param byteInput
     *            The byte input to use for the test
     */
    public CompressionUtilitiesTest(final String name, final byte[] byteInput)
    {
        this.name = name;
        this.byteInput = byteInput;
    }

    /**
     * Test method for {@link CompressionUtilities#getUncompressedInputStream(InputStream)} where
     * the stream is already uncompressed and unarchived.
     *
     * @throws IOException
     *             If something happens (it shouldn't -- we aren't even reading a file!)
     */
    @Test
    public void testUnknownStream() throws IOException
    {
        final InputStream uncompressed = CompressionUtilities
                .getUncompressedInputStream(new ByteArrayInputStream(this.byteInput));
        assertEquals(TESTING_STREAM, new String(uncompressed.readAllBytes()));
    }
}
