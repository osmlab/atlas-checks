package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flags road segments that have no name tag or a name tag with a different spelling from the true
 * name of the road to which they belong. The true name is understood to be the name which is most
 * common among a road's segments. This check is primarily meant to catch small errors in spelling
 * such as a missing letter or letter accent.
 *
 * @author seancoulter
 */
public class RoadNameSpellingConsistencyCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    private static final HighwayTag MINIMUM_NAME_PRIORITY = HighwayTag.SERVICE;
    private static final String FALLBACK_INSTRUCTIONS = "The marked road segments' names are inconsistent with other road segment(s) in the same road";
    private Logger logger = LoggerFactory.getLogger(RoadNameSpellingConsistencyCheck.class);

    public RoadNameSpellingConsistencyCheck(final Configuration configuration)
    {
        super(configuration);
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
        return object instanceof Edge
                && ((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(MINIMUM_NAME_PRIORITY)
                && ((Edge) object).isMasterEdge()
                && !this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     * squid:S3655 is suppressed as the stream filters out any Optionals that are empty.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that flags an OSM way for spelling
     *         inconsistencies.
     */
    @Override
    @SuppressWarnings("squid:S3655")
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        // Collect all Edges in the parent OSM Way
        final Set<Edge> osmWay = new OsmWayWalker(edge).collectEdges();

        // If the edge is the sole member of the Way, there can't be any spelling inconsistencies
        if (osmWay.size() < 2)
        {
            return Optional.empty();
        }

        // Get the name of an Edge in the Way
        final Edge firstEdge = osmWay.stream().filter(segment -> segment.getName().isPresent())
                .findFirst().orElse(null);
        if (firstEdge == null)
        {
            return Optional.of(this.createFlag(Collections.unmodifiableSet(osmWay),
                    "Every segment of this road is unnamed"));
        }

        // NOSONAR: we know this can't throw a NoSuchElementException because of the above filter
        final String firstEdgeName = firstEdge.getName().get();

        // Collect all of the Edges in the Way that have NameTags that
        // aren't the same as firstEdgeName
        final Set<Edge> inconsistentEdgeSet = osmWay.stream()
                .filter(segment -> segment.getName().isPresent()
                        && !segment.getName().get().equals(firstEdgeName)
                        || !segment.getName().isPresent())
                .collect(Collectors.toSet());

        // this.logger.info("Set size: {}", inconsistentEdgeSet.size());
        final List<Location> markedSegments = inconsistentEdgeSet.stream()
                .map(segment -> segment.asPolyLine().middle()).collect(Collectors.toList());
        if (!inconsistentEdgeSet.isEmpty())
        {
            return Optional.of(this.createFlag(Collections.unmodifiableSet(inconsistentEdgeSet),
                    FALLBACK_INSTRUCTIONS, markedSegments));
        }

        // All name tags in the way are the same
        return Optional.empty();
    }

}
