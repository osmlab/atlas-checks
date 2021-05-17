package org.openstreetmap.atlas.checks.validation.points;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check verifies lone {@link Node}s with highway tag that they follow the tagging principles.
 *
 * @author mm-ciub
 */
public class LoneNodeCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = -1489101405354234053L;

    private static final List<String> DEFAULT_HIGHWAY_VALUES_CHECKED = List.of("crossing",
            "turning_circle", "traffic_signals", "stop", "give_way", "motorway_junction",
            "mini_roundabout", "passing_place", "turning_loop");
    private static final String LONE_NODE_INSTRUCTION = "This node {0,number,#} has a Highway tag but is not part of any way that has a highway or railway tag. Either add such a tag to the appropriate parent or remove the highway tag from the node.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(LONE_NODE_INSTRUCTION);

    private List<String> highwayValuesToCheck;

    /**
     * Default constructor.
     *
     * @param configuration
     *            the JSON configuration for this check
     */

    public LoneNodeCheck(final Configuration configuration)
    {
        super(configuration);
        this.highwayValuesToCheck = this
                .configurationValue(configuration, "valid.highway.tag",
                        DEFAULT_HIGHWAY_VALUES_CHECKED)
                .stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    /**
     * The object is valid if it is a Node and has a highway tag with specific values.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        final Optional<String> highwayValue = object.getTag(HighwayTag.KEY);
        return object instanceof Point && highwayValue.isPresent()
                && this.highwayValuesToCheck.contains(highwayValue.get())
                && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        this.markAsFlagged(object.getOsmIdentifier());

        if (this.isLoneNode(object))
        {
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
     * Check if there are no connected ways with highway or railway tag.
     *
     * @param object
     *            to be checked
     * @return true if no way is found
     */
    @SuppressWarnings("unchecked")
    private boolean isLoneNode(final AtlasObject object)
    {
        final Point point = (Point) object;
        final Predicate<? extends AtlasObject> hasHighwayRailway = obj -> obj.getTag(HighwayTag.KEY)
                .isPresent() || obj.getTag(RailwayTag.KEY).isPresent();

        final Iterable<LineItem> connectedLines = object.getAtlas()
                .lineItemsContaining(point.getLocation(), (Predicate<LineItem>) hasHighwayRailway);
        final Iterable<Area> connectedAreas = object.getAtlas().areasCovering(point.getLocation(),
                (Predicate<Area>) hasHighwayRailway);
        return StreamSupport.stream(connectedAreas.spliterator(), false).count() == 0
                && StreamSupport.stream(connectedLines.spliterator(), false).count() == 0;
    }
}
