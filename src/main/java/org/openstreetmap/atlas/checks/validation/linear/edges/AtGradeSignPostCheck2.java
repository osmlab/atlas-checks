package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import static org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates.*;

/**
 * Auto generated Check template
 *
 * @author sayas01
 */
public class AtGradeSignPostCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = -7428641176420422187L;

    private static final String HIGHWAY_FILTER_DEFAULT = "highway->trunk,primary,secondary";
    private static final List<String> CONNECTIONS_TO_PRIMARY = Arrays.asList("trunk","primary","secondary");
    private static final List<String> CONNECTIONS_TO_TRUNK = Arrays.asList("primary");
    private static final List<String> CONNECTIONS_TO_SECONDARY = Arrays.asList("primary");
    private static final ImmutableMap<String, List<String>> CONNECTED_HIGHWAY_TYPES_MAP = ImmutableMap
            .of("primary", CONNECTIONS_TO_PRIMARY, "trunk", CONNECTIONS_TO_TRUNK, "secondary", CONNECTIONS_TO_SECONDARY);
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "OSM way {0} forms at-grade intersection with node  {1} and edge(s) {2}. Create ");

    private final TaggableFilter highwayFilter;
    private Map<String, List<String>> connectedHighwayTypes;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public AtGradeSignPostCheck(final Configuration configuration)
    {
        super(configuration);
        this.highwayFilter = configurationValue(configuration, "highway.filter", HIGHWAY_FILTER_DEFAULT,
                TaggableFilter::forDefinition);
        this.connectedHighwayTypes =
                this.configurationValue(configuration, "connected.highway.types", CONNECTED_HIGHWAY_TYPES_MAP);
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
        return object instanceof Node && !this.isFlagged(String.valueOf(object.getOsmIdentifier()))
                && this.isConnectedToValidHighways((Node) object);
                //&& HighwayTag.highwayTag(object).isPresent() && this.highwayFilter.test(object);
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
        Edge currentEdge = (Edge) object;
        final String highwayTypeOfCurrentEdge = HighwayTag.highwayTag(currentEdge).get().getTagValue();
        final List<String> listOfValidConnectedHighways = this.connectedHighwayTypes.get(highwayTypeOfCurrentEdge);
        if(!this.connectedHighwayTypes.containsKey(highwayTypeOfCurrentEdge))
        {
            return Optional.empty();
        }
        final Set<AtlasEntity> atlasObjectsToBeFlagged = currentEdge.outEdges().stream()
                .filter(outEdge -> outEdge.isMasterEdge()
                                && LevelTag.areOnSameLevel(currentEdge, outEdge)
                                && HighwayTag.highwayTag(outEdge).isPresent()
                                && this.highwayFilter.test(outEdge)
                                && listOfValidConnectedHighways.contains(HighwayTag.highwayTag(outEdge).get().getTagValue())
                        ).collect(Collectors.toSet());
        final Set<Relation> destinationSignRelations = currentEdge.relations().stream()
                .filter(relation -> RelationTypeTag.DESTINATION_SIGN.toString().equalsIgnoreCase
                        (relation.tag(RelationTypeTag.KEY))).collect(Collectors.toSet());
//        if(atlasObjectsToBeFlagged.isEmpty())
//        {
//            return Optional.empty();
//        }
        // Check if the edge is member of any destination sign relation. If yes, check if the
        // out edges and the node is also members of that destination
        // relation. If not, add the out edge and node along with the edge to be flagged.
        Set<AtlasEntity> toBeRemoved = new HashSet<>();
        destinationSignRelations.forEach(destinationSignRelation -> {
            final RelationMemberList relationMembers = destinationSignRelation.allKnownOsmMembers();
            relationMembers.forEach(relationMember -> {
                if(atlasObjectsToBeFlagged.contains(relationMember.getEntity()))
                {
                    toBeRemoved.add(relationMember.getEntity());
                }
            });
        });
        toBeRemoved.forEach(atlasObjectsToBeFlagged::remove);
        if(atlasObjectsToBeFlagged.isEmpty())
        {
            return Optional.empty();
        }
        final List<String> identifiers = this.getIdentifiers(atlasObjectsToBeFlagged);
        // Add current edge to set of objects to be flagged
        atlasObjectsToBeFlagged.add(currentEdge);
        // Add end node of current edge to be flagged
        atlasObjectsToBeFlagged.add(currentEdge.end());
        this.markAsFlagged(String.valueOf(currentEdge.getMasterEdgeIdentifier()));
        this.createFlag(atlasObjectsToBeFlagged,
                this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                        new StringList(identifiers).join(", ")));
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Collects all atlas identifiers of given set of {@link AtlasObject}s
     *
     * @param objects
     *            set of {@link AtlasObject}s
     * @return {@link Iterable<String>} containing the atlas identifiers of input objects
     */
    private List<String> getIdentifiers(final Set<AtlasEntity> objects)
    {
        return Iterables.stream(objects).map(AtlasEntity::getIdentifier).map(String::valueOf)
                .collectToList();
    }

    private boolean isConnectedToValidHighways(final Node node)
    {
        return node.connectedEdges().stream().anyMatch(this.highwayFilter);
    }
}
