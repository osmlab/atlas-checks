package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.boundaries.ComplexBoundaryFinder;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author srachanski
 */
public class BoundaryIntersectionsCheckTest {
    
    
//    private static final Logger logger = LoggerFactory.getLogger(BoundaryIntersectionsCheckTest.class);
    
    @Rule
    public BoundaryIntersectionsCheckTestRule setup = new BoundaryIntersectionsCheckTestRule();
    
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    
    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();

    @Test
    public void testInvalidTwoCrossingItemsAtlas() {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectEachOther(),
                new BoundaryIntersectionsCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(5, flag.getFlaggedObjects().size()));
    }
    
    @Test
    public void testInvalidThreeCrossingItemsAtlas() {
        this.verifier.actual(this.setup.crossingBoundariesTwoAreasIntersectOneOther(),
                new BoundaryIntersectionsCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(3, flags.size()));
    }
    
    @Test
    public void testValidNonCrossingObjects() {
        this.verifier.actual(this.setup.nonCrossingBoundariesTwoSeparate(),
                new BoundaryIntersectionsCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

}
