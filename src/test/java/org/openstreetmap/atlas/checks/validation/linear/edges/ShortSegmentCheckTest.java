package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author mkalender
 */
public class ShortSegmentCheckTest
{
    private static final ShortSegmentCheck check = new ShortSegmentCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public ShortSegmentCheckTestRule setup = new ShortSegmentCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private static void verifyFlaggedItemIsShort(final CheckFlag flag)
    {
        final PolyLine polyLine = flag.getPolyLines().iterator().next();
        Assert.assertTrue(polyLine.length().isLessThan(Distance.ONE_METER));
    }

    @Test
    public void testAlmostShortSegment()
    {
        this.verifier.actual(this.setup.almostShortSegmentAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testLongSegment()
    {
        this.verifier.actual(this.setup.longSegmentAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortBidirectionalSegmentWithOneValence3NodeAndOneValence2Node()
    {
        this.verifier.actual(
                this.setup.shortBidirectionalSegmentWithOneValence3NodeAndOneValence2NodeAtlas(),
                check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsShort(flag));
    }

    @Test
    public void testShortBidirectionalSegmentWithTwoValence2Nodes()
    {
        this.verifier.actual(this.setup.shortBidirectionalSegmentWithTwoValence2NodesAtlas(),
                check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsShort(flag));
    }

    @Test
    public void testShortSegmentClosedWayWithOneValence2NodeAtlas()
    {
        this.verifier.actual(this.setup.shortSegmentClosedWayWithOneValence2NodeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortSegmentClosedWayWithTwoValence2NodeAtlas()
    {
        this.verifier.actual(this.setup.shortSegmentClosedWayWithTwoValence2NodeAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortSegmentPathWithTwoValence1Nodes()
    {
        this.verifier.actual(this.setup.shortSegmentPathWithTwoValence1NodesAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortSegmentWithOneValence2Node()
    {
        this.verifier.actual(this.setup.shortSegmentWithOneValence2NodeAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsShort(flag));
    }

    @Test
    public void testShortSegmentWithOneValence2NodeOneBarrierNode()
    {
        this.verifier.actual(this.setup.shortSegmentWithOneValence2NodeOneBarrierNodeAtlas(),
                check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortSegmentWithTwoValence0Nodes()
    {
        this.verifier.actual(this.setup.shortSegmentWithTwoValence1NodesAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsShort(flag));
    }

    @Test
    public void testShortSegmentWithValence3Nodes()
    {
        this.verifier.actual(this.setup.shortSegmentWithValence3NodesAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testShortSegmentsWithTwoValence2Nodes()
    {
        this.verifier.actual(this.setup.shortSegmentsWithTwoValence2NodesAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsShort(flag));
    }

    @Test
    public void testShortSegmentsWithValence1And2Nodes()
    {
        this.verifier.actual(this.setup.shortSegmentsWithValence1And2NodesAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(4, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsShort(flag));
    }
}
