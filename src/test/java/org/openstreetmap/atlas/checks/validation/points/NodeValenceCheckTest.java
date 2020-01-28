package org.openstreetmap.atlas.checks.validation.points;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.tags.HighwayTag;

/**
 * {@link NodeValenceCheck} unit tests.
 *
 * @author matthieun
 * @author mkalender
 */
public class NodeValenceCheckTest
{
    private static final NodeValenceCheck CHECK = new NodeValenceCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public NodeValenceCheckTestRule setup = new NodeValenceCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testAllHighwayTagsWithElevenConnections()
    {
        for (final HighwayTag tag : HighwayTag.values())
        {
            this.verifier.actual(this.setup.generateAtlas(11, true, tag), CHECK);
        }

        // There are 16 navigable highway tag types
        this.verifier.verifyExpectedSize(16);
    }

    @Test
    public void testAllHighwayTagsWithTenConnections()
    {
        for (final HighwayTag tag : HighwayTag.values())
        {
            this.verifier.actual(this.setup.generateAtlas(10, true, tag), CHECK);
        }

        this.verifier.verifyEmpty();
    }

    @Test
    public void testElevenCarNavigableConnectionAtlas()
    {
        this.verify(11, false, HighwayTag.MOTORWAY);
    }

    @Test
    public void testElevenCarNavigableOneWayConnectionAtlas()
    {
        this.verify(11, true, HighwayTag.MOTORWAY);
    }

    @Test
    public void testElevenNonCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(11, false, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testElevenNonCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(11, true, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testHundredNonCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(100, false, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testHundredNonCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(100, true, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNineCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(9, false, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNineCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(9, true, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNineNonCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(9, false, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNineNonCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(9, true, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNoConnectionAtlas()
    {
        this.verifier.actual(this.setup.noConnectionAtlas(), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testOneCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(1, false, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testOneCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(1, true, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testOneNonCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(1, false, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testOneNonCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(1, true, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTenCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(10, false, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTenCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(10, true, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTenNonCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(10, false, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTenNonCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(10, true, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testThreeCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(3, false, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testThreeCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(3, true, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testThreeNonCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(3, false, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testThreeNonCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(3, true, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTwoCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(2, false, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTwoCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(2, true, HighwayTag.MOTORWAY), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTwoNonCarNavigableConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(2, false, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTwoNonCarNavigableOneWayConnectionAtlas()
    {
        this.verifier.actual(this.setup.generateAtlas(2, true, HighwayTag.PATH), CHECK);
        this.verifier.verifyEmpty();
    }

    private void verify(final int connectionCount, final boolean onlyOneWay,
            final HighwayTag highwayTag)
    {
        this.verifier.actual(this.setup.generateAtlas(connectionCount, onlyOneWay, highwayTag),
                CHECK);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(connectionCount + 1, flag.getFlaggedObjects().size());
        });
    }
}
