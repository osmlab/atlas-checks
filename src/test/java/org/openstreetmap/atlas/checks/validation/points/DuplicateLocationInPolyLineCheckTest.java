package org.openstreetmap.atlas.checks.validation.points;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author mgostintsev
 */
public class DuplicateLocationInPolyLineCheckTest
{
    @Rule
    public final DuplicateLocationInPolyLineCheckTestRule setup = new DuplicateLocationInPolyLineCheckTestRule();

    @Rule
    public final ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    /**
     * Reverse the polyline geometries in an atlas
     *
     * @param atlas
     *            The atlas with the lines to reverse
     * @return A new atlas
     */
    private static Atlas reverseLineItems(final Atlas atlas)
    {
        final AtlasBuilder atlasBuilder = new PackedAtlasBuilder();
        for (final Point point : atlas.points())
        {
            atlasBuilder.addPoint(point.getIdentifier(), point.getLocation(), point.getTags());
        }
        for (final Node node : atlas.nodes())
        {
            atlasBuilder.addNode(node.getIdentifier(), node.getLocation(), node.getTags());
        }
        for (final Area area : atlas.areas())
        {
            atlasBuilder.addArea(area.getIdentifier(), area.asPolygon().reversed(), area.getTags());
        }
        for (final Line line : atlas.lines())
        {
            atlasBuilder.addLine(line.getIdentifier(), line.asPolyLine().reversed(),
                    line.getTags());
        }
        for (final Edge line : atlas.edges())
        {
            atlasBuilder.addEdge(line.getIdentifier(), line.asPolyLine().reversed(),
                    line.getTags());
        }
        return atlasBuilder.get();
    }

    @Test
    public void testInvalidFigureEight()
    {
        this.verifier.verifyExpectedSize(1);
        this.verifier.actual(this.setup.getInvalidFigureEightLine(),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    /**
     * The simple area is invalid since the last location should not be the same as the first
     * location
     */
    @Test
    public void testInvalidSimpleArea()
    {
        this.verifier.verifyExpectedSize(1);
        this.verifier.actual(this.setup.getInvalidSimpleArea(),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    /**
     * The simple area is invalid since the last location should not be the same as the first
     * location
     */
    @Test
    public void testInvalidSimpleAreaReversed()
    {
        this.verifier.verifyExpectedSize(1);
        this.verifier.actual(reverseLineItems(this.setup.getInvalidSimpleArea()),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    @Test
    public void testInvalidSimpleEdge()
    {
        this.verifier.verifyExpectedSize(1);
        this.verifier.actual(this.setup.getInvalidSimpleEdge(),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    @Test
    public void testInvalidSimpleEdgeAlreadyChecked()
    {
        // Cannot use verifier -- it doesn't run the same id through twice
        final DuplicateLocationInPolyLineCheck check = new DuplicateLocationInPolyLineCheck(
                ConfigurationResolver.emptyConfiguration());
        final List<CheckFlag> found = IntStream.range(0, 10)
                .mapToObj(randomVar -> check.flags(this.setup.getInvalidSimpleEdge()))
                .flatMap(iterable -> Iterables.asList(iterable).stream())
                .collect(Collectors.toList());
        assertEquals(1, found.size());
        // Needed to avoid verifier complaints
        this.testInvalidSimpleEdge();
    }

    @Test
    public void testInvalidSimpleEdgeReversed()
    {
        this.verifier.verifyExpectedSize(1);
        this.verifier.actual(reverseLineItems(this.setup.getInvalidSimpleEdge()),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    @Test
    public void testInvalidSimpleLine()
    {
        this.verifier.verifyExpectedSize(1);
        this.verifier.actual(this.setup.getInvalidSimpleLine(),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    @Test
    public void testInvalidSimpleLineReversed()
    {
        this.verifier.verifyExpectedSize(1);
        this.verifier.actual(reverseLineItems(this.setup.getInvalidSimpleLine()),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    @Test
    public void testValidServiceHighway()
    {
        this.verifier.verifyEmpty();
        this.verifier.actual(this.setup.getValidSimpleServiceEdge(),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    @Test
    public void testValidServiceHighwayReversed()
    {
        this.verifier.verifyEmpty();
        this.verifier.actual(reverseLineItems(this.setup.getValidSimpleServiceEdge()),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    /**
     * A loop is not invalid
     */
    @Test
    public void testValidSimpleEdgeLoop()
    {
        this.verifier.verifyEmpty();
        this.verifier.actual(this.setup.getValidSimpleEdge(),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    /**
     * A loop is not invalid
     */
    @Test
    public void testValidSimpleEdgeLoopReversed()
    {
        this.verifier.verifyEmpty();
        this.verifier.actual(reverseLineItems(this.setup.getValidSimpleEdge()),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    /**
     * A loop is not invalid
     */
    @Test
    public void testValidSimpleLineItemLoop()
    {
        this.verifier.verifyEmpty();
        this.verifier.actual(this.setup.getValidSimpleLine(),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }

    /**
     * A loop is not invalid
     */
    @Test
    public void testValidSimpleLineItemLoopReversed()
    {
        this.verifier.verifyEmpty();
        this.verifier.actual(reverseLineItems(this.setup.getValidSimpleLine()),
                new DuplicateLocationInPolyLineCheck(ConfigurationResolver.emptyConfiguration()));
    }
}
