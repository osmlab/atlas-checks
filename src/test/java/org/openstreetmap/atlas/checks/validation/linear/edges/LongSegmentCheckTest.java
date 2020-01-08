package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author gpogulsky
 */
public class LongSegmentCheckTest
{
    private static final Distance MIN_DISTANCE = Distance.kilometers(1);
    @Rule
    public LongSegmentCheckTestRule setup = new LongSegmentCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final Configuration configuration = ConfigurationResolver
            .resourceConfiguration("LongSegmentCheckTest.json", this.getClass());

    private static void verifyFlaggedItemIsLong(final CheckFlag flag)
    {
        final PolyLine polyLine = flag.getPolyLines().iterator().next();
        Assert.assertTrue(polyLine.length().isGreaterThanOrEqualTo(MIN_DISTANCE));
    }

    @Test
    public void testLongBidirectionalSegmentWithTwoValence2Nodes()
    {
        final LongSegmentCheck check = new LongSegmentCheck(this.configuration);
        this.verifier.actual(this.setup.longBidirectionalSegment2NodesAtlas(), check);
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsLong(flag));
    }

    @Test
    public void testLongSegment()
    {
        final LongSegmentCheck check = new LongSegmentCheck(this.configuration);
        this.verifier.actual(this.setup.longSegmentAtlas(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsLong(flag));
    }

    @Test
    public void testLongSegmentFerry()
    {
        final LongSegmentCheck check = new LongSegmentCheck(this.configuration);
        this.verifier.actual(this.setup.longSegmentFerryAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMinDistanceSegment()
    {
        final LongSegmentCheck check = new LongSegmentCheck(this.configuration);
        this.verifier.actual(this.setup.minDistanceSegmentAtlas(), check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> verifyFlaggedItemIsLong(flag));
    }

    @Test
    public void testShortSegment()
    {
        final LongSegmentCheck check = new LongSegmentCheck(this.configuration);
        this.verifier.actual(this.setup.shortSegmentAtlas(), check);
        this.verifier.verifyEmpty();
    }

}
