package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Flags long segments/edges (length is more than minimumLength)
 *
 * @author gpogulsky
 */
public class LongSegmentCheck extends BaseCheck<Long>
{
    public static final double DISTANCE_MINIMUM_KILOMERTERS_DEFAULT = 10;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Way {0,number,#} has a very long stretch with no nodes in it (length = {1} km). This may not be an accurate representation of ground truth.");
    private static final long serialVersionUID = -3153236856904455996L;
    // Length for an edge not to be defined as short
    private final Distance minimumLength;

    public LongSegmentCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumLength = configurationValue(configuration, "length.minimum.kilometers",
                DISTANCE_MINIMUM_KILOMERTERS_DEFAULT, Distance::kilometers);
    }

    /**
     * Validate if given {@link AtlasObject} is actually an {@link Edge}, which is a Main Edge and
     * is not a Ferry
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // TODO replace FERRY check with Predicate
        return object instanceof Edge && ((Edge) object).isMainEdge()
                && !Validators.isOfType(object, RouteTag.class, RouteTag.FERRY);
    }

    /**
     * Flags long segments/edges (length is more than {@link LongSegmentCheck#minimumLength})
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        final Optional<Segment> segment = edge.asPolyLine().segments().stream()
                .filter(s -> s.length().isGreaterThanOrEqualTo(this.minimumLength)).findFirst();

        if (segment.isPresent())
        {
            return Optional.of(createFlag(object, this.getLocalizedInstruction(0,
                    edge.getOsmIdentifier(), segment.get().length().asKilometers())));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
