package org.openstreetmap.atlas.checks.validation.tag;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LanesTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags {@link Edge}s that have the {@code highway} tag and a {@code lanes} tag with an invalid
 * value. The valid {@code lanes} values are configurable.
 *
 * @author bbreithaupt
 */
public class InvalidLanesTagCheck extends BaseCheck
{
    private static final long serialVersionUID = -1459761692833694715L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Way {0,number,#} has an invalid lanes value.");
    // Maximum number of connected edges that are checked for toll booth nodes
    private static final int MAX_TOLL_PLAZA_EDGES = 20;
    // Valid values of the lanes OSM key
    private static final String LANES_FILTER_DEFAULT = "Lanes->1,1.5,2,3,4,5,6,7,8,9,10";
    private final TaggableFilter lanesFilter;

    // Edges that can skip the toll booth test, because they have already been checked.
    private final HashSet<Long> isChecked = new HashSet<>();

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidLanesTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.lanesFilter = (TaggableFilter) configurationValue(configuration, "lanes.filter",
                LANES_FILTER_DEFAULT, value -> TaggableFilter.forDefinition(value.toString()));
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return Validators.hasValuesFor(object, LanesTag.class)
                && HighwayTag.isCarNavigableHighway(object) && object instanceof Edge
                && ((Edge) object).isMasterEdge() && !this.lanesFilter.test(object)
                && !this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (this.isChecked.contains(object.getIdentifier()) || !partOfTollBooth(object))
        {
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks for a node with {@code barrier=toll_booth} amongst the {@link Edge}s that are
     * connected to the input {@link Edge} and have an otherwise invalid {@code lanes} tag.
     *
     * @param object
     *            an {@link Edge} with an otherwise invalid {@code lanes} tag, to be checked for
     *            toll booths
     * @return a boolean that is true when a toll booth is found
     */
    private boolean partOfTollBooth(final AtlasObject object)
    {
        final HashSet<Edge> connectedInvalidEdges = connectedInvalidLanes(object);

        // check for toll booths
        for (final Edge edge : connectedInvalidEdges)
        {
            for (final Node node : edge.connectedNodes())
            {
                if (Validators.isOfType(node, BarrierTag.class, BarrierTag.TOLL_BOOTH))
                {
                    // If there is a toll booth, mark them so we don't process
                    // items twice unnecessarily, and return true
                    connectedInvalidEdges
                            .forEach(validEdge -> this.markAsFlagged(validEdge.getOsmIdentifier()));
                    return true;
                }
            }
        }
        // If not a toll booth, mark for flagging so they can skip this toll booth check.
        connectedInvalidEdges
                .forEach(invalidEdge -> this.isChecked.add(invalidEdge.getIdentifier()));
        return false;
    }

    /**
     * Gets the {@link Edge}s that are connected to the input {@link Edge} and have an otherwise
     * invalid {@code lanes} tag.
     *
     * @param object
     *            an {@link Edge} with an invalid {@code lanes} tag
     * @return a {@link HashSet} of connected invalid {@code lanes} tag {@link Edge}s
     */
    private HashSet<Edge> connectedInvalidLanes(final AtlasObject object)
    {
        // Connected edges with lanes tag values not in the lanesFilter
        final HashSet<Edge> connectedEdges = new HashSet<>();
        // Queue of edges to be processed
        final ArrayDeque<Edge> toProcess = new ArrayDeque<>();
        Edge polledEdge;
        int count = 0;

        // Add original edge
        connectedEdges.add((Edge) object);
        toProcess.add((Edge) object);

        // Get all connected edges with lanes tag values not in the lanesFilter
        while (!toProcess.isEmpty() && count < MAX_TOLL_PLAZA_EDGES)
        {
            polledEdge = toProcess.poll();
            for (final Edge edge : polledEdge.connectedEdges())
            {
                if (!connectedEdges.contains(edge) && ((Edge) object).isMasterEdge()
                        && Validators.hasValuesFor(edge, LanesTag.class)
                        && HighwayTag.isCarNavigableHighway(edge) && !this.lanesFilter.test(edge))
                {
                    toProcess.add(edge);
                    connectedEdges.add(edge);
                }
            }
            count++;
        }

        return connectedEdges;
    }
}
