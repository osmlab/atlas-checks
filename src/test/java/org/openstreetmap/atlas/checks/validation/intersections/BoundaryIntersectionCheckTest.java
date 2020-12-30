package org.openstreetmap.atlas.checks.validation.intersections;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author srachanski
 */
public class BoundaryIntersectionCheckTest
{

    @Rule
    public BoundaryIntersectionCheckTestRule setup = new BoundaryIntersectionCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();

    @Test
    public void testInvalidThreeCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectOneOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
    }

//    @Test
//    public void testInvalidTwoCrossingBoundariesWithOnlyWayTags()
//    {
//        this.verifier.actual(this.setup.crossingBoundariesWithOnlyTagsOnWays(),
//                new BoundaryIntersectionCheck(this.configuration));
//        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
//        this.verifier.verify(flag ->
//        {
//            Assert.assertEquals(6, flag.getFlaggedObjects().size());
//            Assert.assertEquals(1, flag.getInstructions().split("\n").length);
//        });
//    }

    @Test
    public void testInvalidTwoCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectEachOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(4, flag.getFlaggedObjects().size());
            Assert.assertEquals(1, flag.getInstructions().split("\n").length);
        });
    }

//    //TODO remove
//    @Test
//    public void test(){
//        String area1 = "POLYGON((0 0, 3 0, 3 3, 3 0, 0 0))";
//        String area2 = "POLYGON((0 0, 2 0, 2 2, 2 0, 0 0))";
//        String area3 = "POLYGON((0 0, 3 1, 4 4, 1 3, 0 0))";
//        String line1 = "LINESTRING(1 1, 4 4)";
//        String line2 = "LINESTRING(1 4, 4 1)";
//
////        BoundaryIntersectionCheck boundaryIntersectionCheck = new BoundaryIntersectionCheck(null);
//        System.out.println(isCrossingNotTouching(area1, area2));
//        System.out.println(isCrossingNotTouching(area1, area3));
//
//        System.out.println("x");
//    }
//    public boolean isCrossingNotTouching(final String wktFirst,
//                                         final String wktSecond)
//    {
//        final WKTReader wktReader = new WKTReader();
//        try
//        {
//            final Geometry geometry1 = wktReader.read(wktFirst);
//            final Geometry geometry2 = wktReader.read(wktSecond);
//            if(!geometry1.isValid() || !geometry1.isSimple() || !geometry2.isValid() || !geometry2.isSimple()){
//                return false;
//            }
//            if(geometry1.intersects(geometry2))
//            {
//                if(!this.isGeometryPairOfLineType(geometry1, geometry2))
//                {
//                    return this.isLineIntersectionNotTouch(geometry1, geometry2);
//                }
//                return this.isAreaIntersectionNotTouch(geometry1, geometry2);
//            }
//        }
//        catch (final ParseException e)
//        {
//            return false;
//        }
//        return false;
//    }
//
//    private boolean isGeometryPairOfLineType(final Geometry lineString, final Geometry lineString2)
//    {
//        return lineString.getGeometryType().equals("LineString") && lineString2.getGeometryType().equals("LineString");
//    }
//
//    private boolean isLineIntersectionNotTouch(final Geometry geometry1, final Geometry geometry2)
//    {
//        return !geometry1.overlaps(geometry2);
//    }
//
//    private boolean isAreaIntersectionNotTouch(final Geometry geometry1, final Geometry geometry2)
//    {
//        return !(geometry1.covers(geometry2) ||
//                geometry1.coveredBy(geometry2) ||
//                geometry1.touches(geometry2));
//    }
//
//    //TODO remove
//    @Test
//    public void intersectionTest() throws ParseException, IOException {
//        FileInputStream fis = new FileInputStream("src/test/resources/area1");
//        String area1 = IOUtils.toString(fis, "UTF-8");
////        String area1 =
//        FileInputStream fis2 = new FileInputStream("src/test/resources/area2");
//        String area2 = IOUtils.toString(fis2, "UTF-8");
////        String area2 =
////        String line1 = "LINESTRING(19.325022325433846 53.530197223167754,22.818674669183846 51.97440230490904,22.357248887933846 51.66200440966252)";
////        String line2 = "LINESTRING(18.885569200433846 52.19044648242124,23.389963731683842 53.51713468341888)";
//
//        Geometry a1 = new WKTReader().read(area1);
//        Geometry a2 = new WKTReader().read(area2);
////        Geometry l1 = new WKTReader().read(line1);
////        Geometry l2 = new WKTReader().read(line2);
//        System.out.println(isCrossingNotTouching(area1, area2));
//        Assert.assertEquals(isCrossingNotTouching(area1, area2), false);
//    }

    @Test
    public void testInvalidTwoCrossingItemsWithEdgesAtlas()
    {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectEachOtherWithEdges(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag ->
        {
            Assert.assertEquals(4, flag.getFlaggedObjects().size());
            Assert.assertEquals(1, flag.getInstructions().split("\n").length);
        });
    }

    @Test
    public void testTouchingObjects()
    {
        this.verifier.actual(this.setup.boundariesTouchEachOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testValidCrossingObjectsOneMissingBoundarySpecificTag()
    {
        this.verifier.actual(this.setup.crossingOneMissingBoundarySpecificTag(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testValidCrossingObjectsOneMissingType()
    {
        this.verifier.actual(this.setup.crossingOneWithWrongType(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testValidCrossingObjectsWithDifferentTypes()
    {
        this.verifier.actual(this.setup.crossingBoundariesWithDifferentTypes(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testValidNonCrossingObjects()
    {
        this.verifier.actual(this.setup.nonCrossingBoundariesTwoSeparate(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testValidNonCrossingObjectsOneContainOther()
    {
        this.verifier.actual(this.setup.nonCrossingOneContainOther(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testValidNonCrossingObjectsWithEdges()
    {
        this.verifier.actual(this.setup.nonCrossingBoundariesTwoSeparateWithEdges(),
                new BoundaryIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
