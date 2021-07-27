package org.openstreetmap.atlas.checks.validation.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.multi.MultiNode;
import org.openstreetmap.atlas.geography.atlas.multi.MultiPoint;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Finds·relations·that·meet·at·least·one·of·the·following·requirements:·The·track·of·this·route
 * contains·gaps.·The·stop·or·platform·is·too·far·from·the·track·of·this·route.·Non·route·relation
 * ·member·in·route_master·relation.·Public·transport·relation·route·not·in·route_master·relation.
 * ·network,·operator,·ref,·colour·tag·should·be·the·same·on·route·and·route_master·relations"
 *
 * @author lluc
 */
public class RouteRelationCheck extends BaseCheck<Object>
{
    private static final String TEMP_RELATION_ID_INSTRUCTION = "The relation with ID={0,number,#} is problematic.";
    private static final String EMPTY_ROUTE_INSTRUCTION = "The route in relation with ID = {0,number,#} is empty. Please add this road segments(edges).";
    private static final String GAPS_IN_ROUTE_TRACK_INSTRUCTION = "The route in relation with ID = {0,number,#} has gaps in the track.";
    private static final String STOP_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION = "The stops in the route relation with ID={0,number,#} are too far from the track.";
    private static final String PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION = "The platforms in the route relation with ID={0,number,#} are too far from the track.";
    private static final String ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INSTRUCTION = "The route master relation with ID={0,number,#} contains non route element.";
    private static final String PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INSTRUCTION = "The relation with ID={0,number,#} is a public transportation route and has route type being {1}. It should be contained in a Route Master relation.";
    private static final String MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION = "The relation with ID={0,number,#} missing some tags in the category of network, operator, ref, a colour.";
    private static final String INCONSISTENT_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION = "The relation with ID={0,number,#} has inconsistent network, operator, ref, or colour tag with its route master.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            TEMP_RELATION_ID_INSTRUCTION, EMPTY_ROUTE_INSTRUCTION, GAPS_IN_ROUTE_TRACK_INSTRUCTION,
            STOP_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION,
            PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION,
            ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INSTRUCTION,
            PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INSTRUCTION,
            MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION,
            INCONSISTENT_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION);
    private static final int TEMP_RELATION_ID_INDEX = 0;
    private static final int EMPTY_ROUTE_INDEX = 1;
    private static final int GAPS_IN_ROUTE_TRACK_INDEX = 2;
    private static final int STOP_TOO_FAR_FROM_ROUTE_TRACK_INDEX = 3;
    private static final int PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INDEX = 4;
    private static final int ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INDEX = 5;
    private static final int PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INDEX = 6;
    private static final int MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX = 7;
    private static final int INCONSISTENT_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX = 8;
    private static final Set<String> Public_transport_Types = Set.of("train", "subway", "bus",
            "trolleybus", "minibus", "light_rail", "share_taxi", "railway", "rail", "tram",
            "aircraft", "ferry");
    private static final long serialVersionUID = 7671409062471623430L;

    public RouteRelationCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {

        return object instanceof Relation
                && (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE_MASTER)
                        || Validators.isOfType(object, RelationTypeTag.class,
                                RelationTypeTag.ROUTE))
                && !this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object of checkFlags
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation routeRel = (Relation) object;
        final List<String> instructions = new ArrayList<>();
        instructions.add(
                this.getLocalizedInstruction(TEMP_RELATION_ID_INDEX, routeRel.getOsmIdentifier()));

        if (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE_MASTER))
        {
            final List<String> processMasterRouteRelationInstructions = this
                    .processRouteMasterRelation(routeRel);
            if (!processMasterRouteRelationInstructions.isEmpty())
            {
                instructions.addAll(processMasterRouteRelationInstructions);
            }
        }
        else if (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE))
        {
            // check track has no gaps. Check stops and platforms are not too far from the track
            final List<String> processRouteRelationInstructions = this
                    .processRouteRelation(routeRel);
            if (!processRouteRelationInstructions.isEmpty())
            {
                instructions.addAll(processRouteRelationInstructions);
            }

            final Optional<String> transportType = routeRel.getTag("route");
            if (transportType.isPresent() && Public_transport_Types.contains(transportType.get())
                    && !this.relContainedInRouteMasters(routeRel))
            {
                instructions.add(this.getLocalizedInstruction(
                        PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INDEX,
                        routeRel.getOsmIdentifier(), transportType.get()));
            }
        }

        // mark this object as flagged
        this.markAsFlagged(object.getOsmIdentifier());
        return instructions.size() == 1 ? Optional.empty()
                : Optional.of(this.createFlag(routeRel.flatten(), instructions));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * This is the helper function for checkStopPlatformTooFarFromTrack that checks whether or not
     * stops and platforms in the route are too far from the track.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @param stopOrPlatform
     *            indicate whether we want locations for stops or platforms
     * @return a list of locations for either stops or platforms
     */
    private Set<Location> allStopsOrPlatformsLocations(final Relation rel,
            final String stopOrPlatform)
    {
        final Set<AtlasEntity> allSigns = rel.members().stream()
                .filter(member -> member.getRole().equals(stopOrPlatform))
                .map(RelationMember::getEntity).collect(Collectors.toSet());
        final Set<Location> allLocations = new HashSet<>();

        for (final AtlasEntity entity : allSigns)
        {
            if (entity instanceof MultiPoint)
            {
                allLocations.add(((MultiPoint) entity).getLocation());
            }
            else if (entity instanceof MultiNode)
            {
                allLocations.add(((MultiNode) entity).getLocation());
            }
            else if (entity instanceof Node)
            {
                allLocations.add(((Node) entity).getLocation());
            }
            else if (entity instanceof Point)
            {
                allLocations.add(((Point) entity).getLocation());
            }
        }
        return allLocations;
    }

    /**
     * Return the list of instructions that describes inconsistency of any tags in the group of
     * network, operator, ref, and colour between a route master and its member routes
     *
     * @param rel
     *            The route master relation under check
     * @return the list of instructions that describes inconsistency
     */
    private List<String> checkNetworkOperatorRefColourTag(final Relation rel)
    {
        final List<String> instructionsAdd = new ArrayList<>();
        final Optional<String> networkTag = rel.getTag("network");
        final Optional<String> operatorTag = rel.getTag("operator");
        final Optional<String> refTag = rel.getTag("ref");
        final Optional<String> colourTag = rel.getTag("colour");

        if (networkTag.isEmpty() || operatorTag.isEmpty() || refTag.isEmpty()
                || colourTag.isEmpty())
        {
            instructionsAdd.add(this.getLocalizedInstruction(
                    MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX, rel.getOsmIdentifier()));
        }

        final Set<Relation> routeSet = this.routeMemberRouteRelations(rel);

        for (final Relation relRoute : routeSet)
        {
            final Optional<String> routeNetwork = relRoute.getTag("network");
            final Optional<String> routeOperator = relRoute.getTag("operator");
            final Optional<String> routeRef = relRoute.getTag("ref");
            final Optional<String> routeColour = relRoute.getTag("colour");

            if (routeNetwork.isEmpty() || operatorTag.isEmpty() || routeRef.isEmpty()
                    || routeColour.isEmpty())
            {
                instructionsAdd.add(
                        this.getLocalizedInstruction(MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX,
                                relRoute.getOsmIdentifier()));
            }

            if ((routeNetwork.isPresent() && networkTag.isPresent()
                    && !routeNetwork.equals(networkTag))
                    || (routeOperator.isPresent() && operatorTag.isPresent()
                            && !routeOperator.equals(operatorTag))
                    || (routeRef.isPresent() && refTag.isPresent() && !routeRef.equals(refTag))
                    || (routeColour.isPresent() && colourTag.isPresent()
                            && !routeColour.equals(colourTag)))
            {
                instructionsAdd.add(this.getLocalizedInstruction(
                        INCONSISTENT_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX,
                        relRoute.getOsmIdentifier()));
            }
        }

        return instructionsAdd;
    }

    /**
     * This is the function that will check to see whether a route has gaps in the track and whether
     * or not a route contains stops and platforms that are too far from the track.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @return a list of strings that are instructions for creating flags
     */
    private List<String> checkRouteForGaps(final Relation rel)
    {
        final List<String> instructionsAdd = new ArrayList<>();
        final Set<PolyLine> allPolyLines = this.polylineRouteRel(rel);

        if (allPolyLines.isEmpty())
        {
            instructionsAdd
                    .add(this.getLocalizedInstruction(EMPTY_ROUTE_INDEX, rel.getOsmIdentifier()));
        }

        if (allPolyLines.size() > 1)
        {
            final LinkedList<PolyLine> createdRoute = this
                    .routeFormNonArrangedEdgeSet(allPolyLines);

            if (createdRoute.size() < allPolyLines.size())
            {
                instructionsAdd.add(this.getLocalizedInstruction(GAPS_IN_ROUTE_TRACK_INDEX,
                        rel.getOsmIdentifier()));
            }
        }

        return instructionsAdd;
    }

    /**
     * This is the function that will check to see whether a set of stops or platforms that are too
     * far from the track.
     *
     * @param allSignsOrPlatformsLocations
     *            the se of points representing the stops or platforms in the route.
     * @param allEdgePolyLines
     *            * the set of all PolyLines from either edge or line contained in the route
     * @return a boolean yes if stops or platforms are too far from the track. Otherwise no.
     */
    private boolean checkStopPlatformTooFarFromTrack(
            final Set<Location> allSignsOrPlatformsLocations, final Set<PolyLine> allEdgePolyLines)
    {
        final Distance threshHold = Distance.meters(1.5);

        if (allSignsOrPlatformsLocations.isEmpty() || (allEdgePolyLines.isEmpty()))
        {
            return false;
        }

        SnappedLocation minSnap = null;

        for (final Location location : allSignsOrPlatformsLocations)
        {
            for (final PolyLine edges : allEdgePolyLines)
            {
                final SnappedLocation snappedTo = location.snapTo(edges);
                if (minSnap == null || snappedTo.compareTo(minSnap) < 0)
                {
                    minSnap = snappedTo;
                }

                if (minSnap.getDistance().isLessThan(threshHold))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * This is the function that will collect all the edges and lines in the relation into one set.
     *
     * @param rel
     *            the route relation containing edges and lines forming the route track
     * @return all the edges and lines in the relation in one set.
     */
    private Set<PolyLine> polylineRouteRel(final Relation rel)
    {
        // edges in the route RelationMember::getRole)
        final Set<PolyLine> allEdges = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.EDGE)).map(Edge.class::cast)
                .filter(Edge::isMainEdge).map(Edge::asPolyLine).collect(Collectors.toSet());

        final Set<PolyLine> allLines = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.LINE)).map(Line.class::cast)
                .map(Line::asPolyLine).collect(Collectors.toSet());

        return Stream.of(allEdges, allLines).flatMap(Set<PolyLine>::stream)
                .collect(Collectors.toSet());
    }

    /**
     * This is the function that will check to see whether a route has gaps in the track and whether
     * or not a route contains stops and platforms that are too far from the track.
     *
     * @param routeMasterRelation
     *            the route master relation entity supplied by the Atlas-Checks framework for
     *            evaluation
     * @return a list of strings that are instructions for creating flags
     */
    private List<String> processRouteMasterRelation(final Relation routeMasterRelation)
    {
        final List<String> instructionsAdd = new ArrayList<>();
        final Set<Relation> routeSet = this.routeMemberRouteRelations(routeMasterRelation);

        // check track has no gaps. Check stops and platforms are not too far from the track
        for (final Relation relation : routeSet)
        {
            final List<String> processRouteRelationInstructions = this
                    .processRouteRelation(relation);
            if (!processRouteRelationInstructions.isEmpty())
            {
                instructionsAdd.addAll(processRouteRelationInstructions);
            }
        }

        // check consistent of the network_operator_ref_colour tags
        final List<String> tmpInstructions = this
                .checkNetworkOperatorRefColourTag(routeMasterRelation);
        if (!tmpInstructions.isEmpty())
        {
            instructionsAdd.addAll(tmpInstructions);
        }

        // check existing non route element
        if (routeSet.size() < routeMasterRelation.members().size())
        {
            instructionsAdd
                    .add(this.getLocalizedInstruction(ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INDEX,
                            routeMasterRelation.getOsmIdentifier()));
        }

        // mark all route relation in the route master as flagged
        for (final Relation relation : routeSet)
        {
            this.markAsFlagged(relation.getOsmIdentifier());
        }
        return instructionsAdd;
    }

    /**
     * This is the function that will check to see whether a route has gaps in the track and whether
     * or not a route contains stops and platforms that are too far from the track.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @return a list of strings that are instructions for creating flags
     */
    private List<String> processRouteRelation(final Relation rel)
    {
        final List<String> instructionsAdd = this.checkRouteForGaps(rel);
        final Set<Location> allStopsLocations = this.allStopsOrPlatformsLocations(rel, "stop");
        final Set<Location> allPlatformsLocations = this.allStopsOrPlatformsLocations(rel,
                "platform");
        final Set<PolyLine> allEdges = this.polylineRouteRel(rel);

        if (this.checkStopPlatformTooFarFromTrack(allStopsLocations, allEdges))
        {
            instructionsAdd.add(this.getLocalizedInstruction(STOP_TOO_FAR_FROM_ROUTE_TRACK_INDEX,
                    rel.getOsmIdentifier()));
        }
        if (this.checkStopPlatformTooFarFromTrack(allPlatformsLocations, allEdges))
        {
            instructionsAdd.add(this.getLocalizedInstruction(
                    PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INDEX, rel.getOsmIdentifier()));
        }
        return instructionsAdd;
    }

    /**
     * @param routeRelation
     *            the public transport route relation. this method checks whether or not the route
     *            relation is contained in a route master
     * @return an instance of CheckRouteMasterValues containing information about whether or not
     *         this public transport route is contained in a route master
     */
    private boolean relContainedInRouteMasters(final Relation routeRelation)
    {
        final Iterable<Relation> relationsInAtlas = routeRelation.getAtlas().relations();
        final Spliterator<Relation> spliterator = relationsInAtlas.spliterator();

        return StreamSupport.stream(spliterator, false)
                .filter(relation -> Validators.isOfType(relation, RelationTypeTag.class,
                        RelationTypeTag.ROUTE_MASTER))
                .flatMap(relation -> relation.members().stream().map(RelationMember::getEntity))
                .filter(member -> member.getType().equals(ItemType.RELATION))
                .filter(member -> Validators.isOfType(member, RelationTypeTag.class,
                        RelationTypeTag.ROUTE))
                .anyMatch(member -> Long.toString(member.getIdentifier())
                        .equals(Long.toString(routeRelation.getIdentifier())));
    }

    /**
     * This is the helper function that do the check contains stops and platforms that are too far
     * from the track. This method using the logic in fromNonArrangedEdgeSet(final Set<Edge>
     * candidates, final boolean shuffle) from the route.java. Check by endpoints. If endpoint can
     * be connected then no gap. orders are not considered. Check two end points of an edge to see
     * they can be connected as an edge
     *
     * @param linesInRoute
     *            the set of lines and edges from the route relation combined in a list of PolyLines
     * @return a list of strings that are instructions for creating flags
     */
    private LinkedList<PolyLine> routeFormNonArrangedEdgeSet(final Set<PolyLine> linesInRoute)
    {
        final List<PolyLine> members = new ArrayList<>(linesInRoute);
        final LinkedList<PolyLine> routeCreated = new LinkedList<>();

        // initialize routeCreated
        routeCreated.add(members.get(0));
        int numberFailures = 0;

        while (routeCreated.size() < members.size() && numberFailures <= members.size())
        {
            /* keep adding edges till no way to expand the route */
            for (final PolyLine lineMember : members)
            {
                if (routeCreated.contains(lineMember))
                {
                    continue;
                }

                if (lineMember.first().equals(routeCreated.getLast().last()))
                {
                    routeCreated.addLast(lineMember);
                }
                else if (lineMember.last().equals(routeCreated.getFirst().first()))
                {
                    routeCreated.addFirst(lineMember);
                }
            }

            // the maximal times to run for loop maximal equals to number of total lines
            numberFailures = numberFailures + 1;
        }
        return routeCreated;
    }

    /**
     * @param rel
     *            The master route relation containing members of route relation
     * @return set of route relations contained in the route master
     */
    private Set<Relation> routeMemberRouteRelations(final Relation rel)
    {
        return rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.RELATION))
                .map(Relation.class::cast).filter(member -> Validators.isOfType(member,
                        RelationTypeTag.class, RelationTypeTag.ROUTE))
                .collect(Collectors.toSet());
    }
}
