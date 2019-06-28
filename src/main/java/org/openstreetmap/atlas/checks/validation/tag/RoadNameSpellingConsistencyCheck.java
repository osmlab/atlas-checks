package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.zookeeper.Op;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;
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
    private static final HighwayTag MINIMUM_NAME_PRIORITY_DEFAULT = HighwayTag.SERVICE;
    private static final double MAXIMUM_SEARCH_DISTANCE_DEFAULT = 100;
    private static final String FALLBACK_INSTRUCTIONS = "This segment's name is is spelled differently from a similarly named road in the area: {}";
    private Distance maximumSearchDistance;
    private int inconsistentCharacterCountThreshold = -2;
    private Logger logger = LoggerFactory.getLogger(RoadNameSpellingConsistencyCheck.class);

    public RoadNameSpellingConsistencyCheck(final Configuration configuration)
    {
        super(configuration);
        this.maximumSearchDistance = this.configurationValue(configuration, "distance.search.maximum", MAXIMUM_SEARCH_DISTANCE_DEFAULT, Distance::meters);
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
                && ((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(MINIMUM_NAME_PRIORITY_DEFAULT)
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
    protected Optional<CheckFlag> flag(final AtlasObject object) {

        final Edge edge = (Edge) object;

        // If this edge's NameTag doesn't exist, it's skipped
        if(!edge.getName().isPresent())
        {
            return Optional.empty();
        }

        // Collect all of the Edges within a search distance that have NameTags that
        // aren't the same as firstEdgeName
        final Set<Edge> inconsistentEdgeSet = new RoadNameSpellingConsistencyCheckWalker(edge, this.maximumSearchDistance, this.inconsistentCharacterCountThreshold).collectEdges();

        // If the Walker found any inconsistent NameTag spellings
        if (inconsistentEdgeSet.size() > 1) {
            return Optional.of(this.createFlag(inconsistentEdgeSet, FALLBACK_INSTRUCTIONS));
        }

        // There are no spelling inconsistencies among the road's segments
        return Optional.empty();
    }

}
