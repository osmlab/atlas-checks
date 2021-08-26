package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link DuplicateMapFeatureCheck}
 *
 * @author Xiaohong Tang
 */
public class DuplicateMapFeatureCheckTest
{
    private static final DuplicateMapFeatureCheck check = new DuplicateMapFeatureCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"DuplicateMapFeatureCheck\": {\"features.tags.should.represent.only.once.in.area\": [\"amenity\", \"leisure\", \"building\", \"shop\"]}}"));

    @Rule
    public final DuplicateMapFeatureCheckTestRule setup = new DuplicateMapFeatureCheckTestRule();

    @Rule
    public final ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testAreaNodeDuplicateFeature()
    {
        this.verifier.actual(this.setup.getAreaNodeDuplicateFeature(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testAreaNodeNotDuplicateFeature()
    {
        this.verifier.actual(this.setup.getAreaNodeNotDuplicateFeature(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testEdgeNodeDuplicateFeature()
    {
        this.verifier.actual(this.setup.getEdgeNodeDuplicateFeature(), check);
        this.verifier.verifyExpectedSize(3);
    }

    @Test
    public void testEdgeNodeNotDuplicateFeature()
    {
        this.verifier.actual(this.setup.getEdgeNodeNotDuplicateFeature(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testRelationEdgeDuplicateFeature()
    {
        this.verifier.actual(this.setup.getRelationEdgeDuplicateFeature(), check);
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void testRelationEdgeNotDuplicateFeature()
    {
        this.verifier.actual(this.setup.getRelationEdgeNotDuplicateFeature(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testRelationNodeDuplicateFeature()
    {
        this.verifier.actual(this.setup.getRelationNodeDuplicateFeature(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testRelationNodeNotDuplicateFeature()
    {
        this.verifier.actual(this.setup.getRelationNodeNotDuplicateFeature(), check);
        this.verifier.verifyEmpty();
    }
}
