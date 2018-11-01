package org.openstreetmap.atlas.checks.utility;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Unit test for {@link BfsEdgeWalker}.
 *
 * @author bbreithaupt
 */
public class BfsEdgeWalkerTest
{
    @Rule
    public BfsEdgeWalkerTestRule setup = new BfsEdgeWalkerTestRule();
    private static final long FIRST_EDGE_IDENTIFIER = 1000000001L;

    @Test
    public void collectAllTest()
    {
        Assert.assertEquals(3, new BfsEdgeWalker((edge, queued) -> edge.outEdges().stream()
                .filter(connected -> !queued.contains(connected)).collect(Collectors.toSet()))
                        .collect(setup.mototrwayPrimaryTriangleAtlas().edge(FIRST_EDGE_IDENTIFIER))
                        .size());
    }

    @Test
    public void collectPrimaryQueueAllTest()
    {
        Assert.assertEquals(2, new BfsEdgeWalker((edge, queued) -> edge.outEdges().stream()
                .filter(connected -> !queued.contains(connected)).collect(Collectors.toSet()),
                edge -> Validators.isOfType(edge, HighwayTag.class, HighwayTag.PRIMARY))
                        .collect(setup.mototrwayPrimaryTriangleAtlas().edge(FIRST_EDGE_IDENTIFIER))
                        .size());
    }
}
