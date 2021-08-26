package org.openstreetmap.atlas.checks.validation.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Finds·relations·that·meet·at·least·one·of·the·following·requirements:·The·track·of·this·route
 * contains·gaps.·The·stop·or·platform·is·too·far·from·the·track·of·this·route.·Non·route·relation
 * ·member·in·route_master·relation.·Public·transport·relation·route·not·in·route_master·relation.
 * ·network,·operator,·ref,·colour·tag·should·be·the·same·on·route·and·route_master·relations
 *
 * @author lluc
 */
public class RouteRelationCheck extends BaseCheck<Object>
{
    private static final String TEMP_RELATION_ID_INSTRUCTION = "The relation with ID={0,number,#} is problematic.";
    private static final String GAPS_IN_ROUTE_TRACK_INSTRUCTION = "The route in relation with ID = {0,number,#} has gaps in the track.";
    private static final String STOP_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION = "The stops in the route relation with ID={0,number,#} are too far from the track.";
    private static final String PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION = "The platforms in the route relation with ID={0,number,#} are too far from the track.";
    private static final String ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INSTRUCTION = "The route master relation with ID={0,number,#} contains non route element.";
    private static final String PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INSTRUCTION = "The relation with ID={0,number,#} is a public transportation route and has route type being {1}. It should be contained in a Route Master relation.";
    private static final String INCONSISTENT_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION = "The relation with ID={0,number,#} has inconsistent network, operator, ref, or colour tag with its route master.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            TEMP_RELATION_ID_INSTRUCTION, GAPS_IN_ROUTE_TRACK_INSTRUCTION,
            STOP_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION,
            PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INSTRUCTION,
            ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INSTRUCTION,
            PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INSTRUCTION,
            INCONSISTENT_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION);
    private static final int TEMP_RELATION_ID_INDEX = 0;
    private static final int GAPS_IN_ROUTE_TRACK_INDEX = 1;
    private static final int STOP_TOO_FAR_FROM_ROUTE_TRACK_INDEX = 2;
    private static final int PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INDEX = 3;
    private static final int ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INDEX = 4;
    private static final int PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INDEX = 5;
    private static final int INCONSISTENT_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX = 6;
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
        return object instanceof Relation && Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.ROUTE_MASTER, RelationTypeTag.ROUTE)
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
        TestStructureData routeSignInstructions = null;

        instructions.add(
                this.getLocalizedInstruction(TEMP_RELATION_ID_INDEX, routeRel.getOsmIdentifier()));

        if (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE_MASTER))
        {
            routeSignInstructions = this.processRouteMasterRelation(routeRel);
        }

        if (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE))
        {
            routeSignInstructions = this.processRouteRelation(routeRel);
        }

        final Set<AtlasEntity> atlasObjectFlagged = new HashSet<>();

        if (Objects.nonNull(routeSignInstructions))
        {
            instructions.addAll(routeSignInstructions.getInstructions());
            atlasObjectFlagged.addAll(routeSignInstructions.getEdgesLines());
            atlasObjectFlagged.addAll(routeSignInstructions.getAllSigns());

            if (routeSignInstructions.getNonRouteMembers() != null)
            {
                atlasObjectFlagged.addAll(routeSignInstructions.getNonRouteMembers());
            }

            // add relation itself when none of the edges and signs are flagged
            if (atlasObjectFlagged.isEmpty())
            {
                // if no edges and points are flagged, just add one edge to
                // the flagged set or add one member
                final AtlasEntity firstMember = routeRel.members().stream()
                        .map(RelationMember::getEntity).findFirst()
                        // if stream is empty
                        // null is returned
                        .orElse(null);

                try
                {
                    atlasObjectFlagged.add(firstMember);
                }
                catch (final Exception error)
                {
                    /* Do Nothing */
                }
            }
        }

        // mark this object as flagged
        this.markAsFlagged(object.getOsmIdentifier());
        return instructions.size() == 1 ? Optional.empty()
                : Optional.of(this.createFlag(atlasObjectFlagged, instructions));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * A wrapper class for transferring edges and lines information generated for route relations.
     */
    private static final class EdgeLineData
    {
        private final List<PolyLine> allPolyLines;
        private final List<AtlasEntity> edgesLines;

        EdgeLineData(final List<PolyLine> allPolyLines, final List<AtlasEntity> edgesLines)
        {
            this.allPolyLines = allPolyLines;
            this.edgesLines = edgesLines;
        }

        public List<PolyLine> getAllPolyLines()
        {
            return this.allPolyLines;
        }

        public List<AtlasEntity> getEdgesLines()
        {
            return this.edgesLines;
        }
    }

    /**
     * This is the function that will collect all the edges and lines in the route relation.
     *
     * @param rel
     *            the route relation containing edges and lines forming the route track
     * @return the set of PolyLine representations for the edges and lines in the relation.
     */
    private EdgeLineData polylineRouteRel(final Relation rel)
    {
        final List<Edge> allEdges = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.EDGE)).map(Edge.class::cast)
                .filter(Edge::isMainEdge).collect(Collectors.toList());

        final List<Line> allLines = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.LINE)).map(Line.class::cast)
                .collect(Collectors.toList());
        final List<LineItem> edgesLines = new ArrayList<>();
        final List<PolyLine> allPolyLines = new ArrayList<>();
        
        edgesLines.addAll(allEdges);
        edgesLines.addAll(allLines);
        edgesLines.forEach(lineItem -> allPolyLines.add(lineItem.asPolyLine()))

        return new EdgeLineData(allPolyLines, edgesLines);
    }

    /**
     * This is the function that will check to see whether a route has gaps in the track and whether
     * a route contains stops and platforms that are too far from the track.
     *
     * @param routeMasterRelation
     *            the route master relation entity supplied by the Atlas-Checks framework for
     *            evaluation
     * @return a TestStructureData class containing instructions for creating flags
     */
    private TestStructureData processRouteMasterRelation(final Relation routeMasterRelation)
    {
        final List<String> instructionsAdd = new ArrayList<>();
        final List<AtlasEntity> allEdgesLinesFlagged = new ArrayList<>();
        final List<AtlasEntity> allSignsEntitiesFlagged = new ArrayList<>();
        final List<Location> allSignsLocationsFlagged = new ArrayList<>();
        final Set<AtlasEntity> nonRouteMembers = new HashSet<>();
        final Set<Relation> routeSet = this.routeSetMemberRelations(routeMasterRelation);

        // check track has no gaps. Check stops and platforms are not too far from the track
        for (final Relation relation : routeSet)
        {
            final TestStructureData routeSignInstructions = this
                    .processRouteRelationHelper(relation);
            instructionsAdd.addAll(routeSignInstructions.getInstructions());
            allEdgesLinesFlagged.addAll(routeSignInstructions.getEdgesLines());
            allSignsEntitiesFlagged.addAll(routeSignInstructions.getAllSigns());
            allSignsLocationsFlagged.addAll(routeSignInstructions.getAllSignsLocations());
        }

        // check consistent of the network_operator_ref_colour tags
        final List<String> tmpInstructions = this
                .testNetworkOperatorRefColourTag(routeMasterRelation);

        if (!tmpInstructions.isEmpty())
        {
            instructionsAdd.addAll(tmpInstructions);
        }

        // check existing non route element
        if (routeSet.size() < routeMasterRelation.members().size())
        {
            final Set<AtlasEntity> otherMembers = routeMasterRelation.members().stream()
                    .map(RelationMember::getEntity).filter(member -> !(Validators.isOfType(member,
                            RelationTypeTag.class, RelationTypeTag.ROUTE)))
                    .collect(Collectors.toSet());
            nonRouteMembers.addAll(otherMembers);
            instructionsAdd
                    .add(this.getLocalizedInstruction(ROUTE_MASTER_HAS_NON_ROUTE_ELEMENT_INDEX,
                            routeMasterRelation.getOsmIdentifier()));
        }

        return new TestStructureData(instructionsAdd, allEdgesLinesFlagged, allSignsEntitiesFlagged,
                allSignsLocationsFlagged, nonRouteMembers);
    }

    /**
     * This is the function that will check to see whether a route has gaps in the track and whether
     * a route contains stops and platforms that are too far from the track.
     *
     * @param rel
     *            the route relation entity supplied by the Atlas-Checks framework for evaluation
     * @return a TestStructureData class containing instructions for creating flags
     */
    private TestStructureData processRouteRelation(final Relation relation)
    {
        final TestStructureData routeSignInstructions = this.processRouteRelationHelper(rel);
        final List<String> instructionsAdd = routeSignInstructions.getInstructions();
        final List<AtlasEntity> allEdgesLinesFlagged = routeSignInstructions.getEdgesLines();
        final List<AtlasEntity> allSignsEntitiesFlagged = routeSignInstructions.getAllSigns();
        final List<Location> allSignsLocationsFlagged = routeSignInstructions
                .getAllSignsLocations();

        final Optional<String> transportType = rel.getTag("route");
        if (transportType.isPresent() && Public_transport_Types.contains(transportType.get())
                && !this.testContainedInRouteMasters(rel))
        {
            instructionsAdd.add(
                    this.getLocalizedInstruction(PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INDEX,
                            rel.getOsmIdentifier(), transportType.get()));
        }

        return new TestStructureData(instructionsAdd, allEdgesLinesFlagged, allSignsEntitiesFlagged,
                allSignsLocationsFlagged, null);
    }

    /**
     * This the function that identifies gaps in the route track and the stops and platforms that
     * are too far from the track.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @return a TestStructureData class containing instructions for creating flags
     */
    private TestStructureData processRouteRelationHelper(final Relation rel)
    {
        final TestStructureData routeResults = this.routeForGaps(rel);
        final List<String> instructionsAdd = routeResults.getInstructions();
        final List<AtlasEntity> edgesLinesFlagged = routeResults.getEdgesLines();
        final List<AtlasEntity> allSignsFlagged = new ArrayList<>();
        final List<Location> allStopsPlatformsFlagged = new ArrayList<>();

        final TestStructureData stopsFlaggedInfo = this.stopPlatformTooFarFromTrack(rel, "stop");
        final List<AtlasEntity> stopsEntitiesFlagged = stopsFlaggedInfo.getAllSigns();

        if (!stopsEntitiesFlagged.isEmpty())
        {
            allSignsFlagged.addAll(stopsEntitiesFlagged);
            instructionsAdd.add(this.getLocalizedInstruction(STOP_TOO_FAR_FROM_ROUTE_TRACK_INDEX,
                    rel.getOsmIdentifier()));
        }

        final TestStructureData platformsFlaggedInfo = this.stopPlatformTooFarFromTrack(rel,
                "platform");
        final List<AtlasEntity> platformsEntitiesFlagged = platformsFlaggedInfo.getAllSigns();

        if (!platformsEntitiesFlagged.isEmpty())
        {
            allSignsFlagged.addAll(platformsEntitiesFlagged);
            instructionsAdd.add(this.getLocalizedInstruction(
                    PLATFORM_TOO_FAR_FROM_ROUTE_TRACK_INDEX, rel.getOsmIdentifier()));
        }

        return new TestStructureData(instructionsAdd, edgesLinesFlagged, allSignsFlagged,
                allStopsPlatformsFlagged, null);
    }

    /**
     * This is the function that will check to see whether a route has gaps.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @return a TestStructureData for creating flags CheckRouteForGaps
     */
    private TestStructureData routeForGaps(final Relation rel)
    {
        final List<String> instructionsAdd = new ArrayList<>();
        final EdgeLineData edgesPolyLines = this.polylineRouteRel(rel);
        final List<AtlasEntity> edgesLines = edgesPolyLines.getEdgesLines();
        final List<PolyLine> allPolyLines = edgesPolyLines.getAllPolyLines();
        final List<AtlasEntity> edgesLinesFlagged = new ArrayList<>();

        if (allPolyLines.size() > 1)
        {
            final List<PolyLine> disconnectedMembers = this
                    .routeFromNonArrangedEdgeSet(allPolyLines);

            if (!disconnectedMembers.isEmpty())
            {
                // add the edges and lines that are flagged
                for (int i = 0; i < allPolyLines.size(); i++)
                {
                    if (disconnectedMembers.contains(allPolyLines.get(i)))
                    {
                        edgesLinesFlagged.add(edgesLines.get(i));
                    }
                }

                instructionsAdd.add(this.getLocalizedInstruction(GAPS_IN_ROUTE_TRACK_INDEX,
                        rel.getOsmIdentifier()));
            }
        }

        return new TestStructureData(instructionsAdd, edgesLinesFlagged, null, null, null);
    }

    /**
     * This is the function that create a route from the edges in a route relation.
     *
     * @param linesInRoute
     *            the set of lines and edges from the route relation combined in a list of PolyLines
     * @return a list containing the edges that are not contained in the created route
     */
    private List<PolyLine> routeFromNonArrangedEdgeSet(final List<PolyLine> linesInRoute)
    {
        // list to store all routes created
        final List<LinkedList<PolyLine>> routes = new ArrayList<>();
        // form the first route segment
        final List<LinkedList<PolyLine>> routeInformation = this.routeSet(linesInRoute);
        routes.add(routeInformation.get(0));
        List<PolyLine> membersLeft = new ArrayList<>(routeInformation.get(1));

        while (!membersLeft.isEmpty())
        {
            final List<LinkedList<PolyLine>> routeInformationTmp = this.routeSet(membersLeft);
            final LinkedList<PolyLine> routeAdded = routeInformationTmp.get(0);
            membersLeft = new ArrayList<>(routeInformationTmp.get(1));
            routes.add(routeAdded);
        }

        final List<PolyLine> disconnectedMembers = new ArrayList<>();

        if (routes.size() > 1)
        {
            disconnectedMembers.addAll(this.routeSetDisconnected(routes));
        }

        return disconnectedMembers;
    }

    /**
     * This is the function that create a single route from the edges in a route relation.
     *
     * @param linesInRoute
     *            the current set of lines and edges from the route relation combined in a list of
     *            PolyLines
     * @return a list of linked list for created route and the rest of the edges currently not
     *         contained in any sub route
     */
    private List<LinkedList<PolyLine>> routeSet(final List<PolyLine> linesInRoute)
    {
        final List<PolyLine> members = new ArrayList<>(linesInRoute);
        final LinkedList<PolyLine> membersLeft = new LinkedList<>();
        final LinkedList<PolyLine> routeCreated = new LinkedList<>();
        // initialize routeCreated
        routeCreated.add(members.get(0));
        int previousSize = -1;
        int currentSize = routeCreated.size();

        while (routeCreated.size() < members.size() && previousSize < currentSize)
        {
            /* keep adding edges till no way to expand the route */
            previousSize = routeCreated.size();

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

            currentSize = routeCreated.size();
        }

        for (final PolyLine lineMember : members)
        {
            if (!routeCreated.contains(lineMember))
            {
                membersLeft.add(lineMember);
            }
        }

        return Arrays.asList(routeCreated, membersLeft);
    }

    /**
     * This is the function that check whether two sub routes are connected at a certain point.
     *
     * @param routeOne
     *            sub route one
     * @param routeTwo
     *            * sub route two
     * @return true of the two routes are connected. otherwise false
     */
    private boolean routeSetConnectedCheck(final LinkedList<PolyLine> routeOne,
            final LinkedList<PolyLine> routeTwo)
    {
        for (final PolyLine lineOne : routeOne)
        {
            for (final PolyLine lineTwo : routeTwo)
            {
                if (lineOne.intersects(lineTwo))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This is the helper function that check the edges that are closest to the two endpoints of a
     * created route from connected edges of a route relation
     *
     * @param routes
     *            the set of PolyLines forming a connected route
     * @return a list of strings that are instructions for creating flags
     */
    private List<PolyLine> routeSetDisconnected(final List<LinkedList<PolyLine>> routes)
    {
        final Set<PolyLine> disconnectedRoutes = new HashSet<>();
        final Set<PolyLine> routeCreated = new HashSet<>();
        final Set<Integer> connectedIndex = new HashSet<>();

        for (int i = 0; i < routes.size(); i++)
        {
            if (connectedIndex.contains(i))
            {
                continue;
            }

            boolean connected = false;
            final LinkedList<PolyLine> routeOne = routes.get(i);
            for (int j = 0; j < routes.size(); j++)
            {
                final LinkedList<PolyLine> routeTwo = routes.get(j);
                if (j != i && !connected && this.routeSetConnectedCheck(routeOne, routeTwo))
                {
                    connected = true;
                    connectedIndex.add(i);
                    connectedIndex.add(j);
                    routeCreated.addAll(routes.get(i));
                    routeCreated.addAll(routes.get(j));
                }
            }

            if (!connected)
            {
                disconnectedRoutes.addAll(routeOne);
            }
        }

        final List<PolyLine> disconnectedMembersMinimal = new ArrayList<>();

        if (!disconnectedRoutes.isEmpty())
        {
            disconnectedMembersMinimal
                    .addAll(this.routeSetDisconnectedClosest(routeCreated, disconnectedRoutes));
        }

        return disconnectedMembersMinimal;
    }

    /**
     * This is the function that find two edges from the created route and the rest of the edges.
     * These two edges represent the shortest distance among the two sets.
     *
     * @param routeCreated
     *            the set of create sub routes that are connected to each other
     * @param disconnectedMembers
     *            the set of edges that are not connected to the route created
     * @return a list of PolyLines representing two edges closest to the created route
     */
    private List<PolyLine> routeSetDisconnectedClosest(final Set<PolyLine> routeCreated,
            final Set<PolyLine> disconnectedMembers)
    {
        final List<PolyLine> disconnectedMembersMinimal = new ArrayList<>();
        PolyLine closestDisconnectedEdge = null;
        PolyLine closestRouteEdge = null;
        Distance minDis = null;

        if (!routeCreated.isEmpty() && !disconnectedMembers.isEmpty())
        {
            for (final PolyLine lineOne : disconnectedMembers)
            {
                for (final PolyLine lineTwo : routeCreated)
                {
                    final Distance tmpMin = this.routeSetDisconnectedClosestHelper(lineOne,
                            lineTwo);

                    if (minDis == null || tmpMin.isLessThan(minDis))
                    {
                        minDis = tmpMin;
                        closestDisconnectedEdge = lineOne;
                        closestRouteEdge = lineTwo;
                    }
                }
            }
        }
        else if (!disconnectedMembers.isEmpty())
        {
            for (final PolyLine lineOne : disconnectedMembers)
            {
                // set the two ends of the same element fo the disconnectedMembers
                closestDisconnectedEdge = lineOne;
                closestRouteEdge = lineOne;
            }
        }

        // add two edges that describes the gap
        disconnectedMembersMinimal.add(closestDisconnectedEdge);
        disconnectedMembersMinimal.add(closestRouteEdge);

        return disconnectedMembersMinimal;
    }

    /**
     * This is the helper function to find the closest edges from the route and the rest of the
     * edges. It is created to reduce complexity.
     *
     * @param lineOne
     *            the first sub routes to check
     * @param lineTwo
     *            the second sub routes to check
     * @return a minimal distance between ends of the two sub routes
     */
    private Distance routeSetDisconnectedClosestHelper(final PolyLine lineOne,
            final PolyLine lineTwo)
    {
        final Location lineOneStart = lineOne.first();
        final Location lineOneEnd = lineOne.last();
        final Location lineTwoStart = lineTwo.first();
        final Location lineTwoEnd = lineTwo.last();
        final Distance startDistanceStart = lineOneStart.distanceTo(lineTwoStart);
        final Distance startDistanceEnd = lineOneStart.distanceTo(lineTwoEnd);
        final Distance endDistanceStart = lineOneEnd.distanceTo(lineTwoStart);
        final Distance endDistanceEnd = lineOneEnd.distanceTo(lineTwoEnd);
        Distance tmpMin = startDistanceStart;

        if (startDistanceEnd.isLessThan(tmpMin))
        {
            tmpMin = startDistanceEnd;
        }
        if (endDistanceStart.isLessThan(tmpMin))
        {
            tmpMin = endDistanceStart;
        }
        if (endDistanceEnd.isLessThan(tmpMin))
        {
            tmpMin = endDistanceEnd;
        }

        return tmpMin;
    }

    /**
     * @param rel
     *            The master route relation containing members of route relation
     * @return set of route relations contained in the route master
     */
    private Set<Relation> routeSetMemberRelations(final Relation rel)
    {
        return rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.RELATION))
                .map(Relation.class::cast).filter(member -> Validators.isOfType(member,
                        RelationTypeTag.class, RelationTypeTag.ROUTE))
                .collect(Collectors.toSet());
    }

    /**
     * This is the function that will check to see whether a set of stops or platforms that are too
     * far from the track.
     *
     * @param rel
     *            the route relation to check.
     * @param stopOrPlatform
     *            * indicate to check either stops of platforms
     * @return a TestStructureData for creating the flag.
     */
    private TestStructureData stopPlatformTooFarFromTrack(final Relation rel,
            final String stopOrPlatform)
    {
        final List<AtlasEntity> allSigns = rel.members().stream()
                .filter(member -> member.getRole().equals(stopOrPlatform))
                .map(RelationMember::getEntity).collect(Collectors.toList());

        final List<PolyLine> allEdgePolyLines = this.polylineRouteRel(rel).getAllPolyLines();
        final List<Location> signLocationsFlagged = new ArrayList<>();
        final List<AtlasEntity> allSignsEntitiesFlagged = new ArrayList<>();

        for (final AtlasEntity entity : allSigns)
        {
            final List<Location> signLocations = new ArrayList<>();
            if (entity instanceof AtlasItem)
            {
                final AtlasItem signPositions = (AtlasItem) entity;
                signPositions.getRawGeometry().forEach(signLocations::add);
            }

            if (this.stopPlatformTooFarFromTrackCheck(signLocations, allEdgePolyLines))
            {
                allSignsEntitiesFlagged.add(entity);
            }

        }

        return new TestStructureData(null, null, allSignsEntitiesFlagged, signLocationsFlagged,
                null);
    }

    /**
     * This is the helper function for checkStopPlatformTooFarFromTrack that do the check.
     *
     * @param signLocations
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @param allEdgePolyLines
     *            indicate whether we want locations for stops or platforms
     * @return a boolean indicating whether the stop or platform is too far from the track
     */

    private boolean stopPlatformTooFarFromTrackCheck(final List<Location> signLocations,
            final List<PolyLine> allEdgePolyLines)
    {
        final Distance threshHold = Distance.meters(15.0);
        SnappedLocation minSnap = null;
        // Location minLoc = null

        for (final Location location : signLocations)
        {
            for (final PolyLine edges : allEdgePolyLines)
            {
                final SnappedLocation snappedTo = location.snapTo(edges);
                if (minSnap == null || snappedTo.compareTo(minSnap) < 0)
                {
                    minSnap = snappedTo;
                }
            }
        }

        return minSnap != null && minSnap.getDistance().isGreaterThan(threshHold);
    }

    /**
     * @param routeRelation
     *            the public transport route relation. this method checks whether the route relation
     *            is contained in a route master
     * @return an instance of CheckRouteMasterValues containing information about whether this
     *         public transport route is contained in a route master
     */
    private boolean testContainedInRouteMasters(final Relation routeRelation)
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
     * Return the list of instructions that describes inconsistency of any tags in the group of
     * network, operator, ref, and colour between a route master and its member routes
     *
     * @param rel
     *            The route master relation under check
     * @return the list of instructions that describes inconsistency
     */
    private List<String> testNetworkOperatorRefColourTag(final Relation rel)
    {
        final List<String> instructionsAdd = new ArrayList<>();
        final Optional<String> networkTag = rel.getTag("network");
        final Optional<String> operatorTag = rel.getTag("operator");
        final Optional<String> refTag = rel.getTag("ref");
        final Optional<String> colourTag = rel.getTag("colour");

        final Set<Relation> routeSet = this.routeSetMemberRelations(rel);
        for (final Relation relRoute : routeSet)
        {
            final Optional<String> routeNetwork = relRoute.getTag("network");
            final Optional<String> routeOperator = relRoute.getTag("operator");
            final Optional<String> routeRef = relRoute.getTag("ref");
            final Optional<String> routeColour = relRoute.getTag("colour");

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
     * A wrapper class for transferring information when processing route and master route relations
     */
    private static final class TestStructureData
    {
        private final List<String> instructions;
        private final List<AtlasEntity> edgesLines;
        private final List<AtlasEntity> allSigns;
        private final List<Location> allSignsLocations;
        private final Set<AtlasEntity> nonRouteMembers;

        TestStructureData(final List<String> instructions, final List<AtlasEntity> edgesLines,
                final List<AtlasEntity> allSigns, final List<Location> allSignsLocations,
                final Set<AtlasEntity> nonRouteMembers)
        {
            this.instructions = instructions;
            this.edgesLines = edgesLines;
            this.allSigns = allSigns;
            this.allSignsLocations = allSignsLocations;
            this.nonRouteMembers = nonRouteMembers;
        }

        public List<AtlasEntity> getAllSigns()
        {
            return this.allSigns;
        }

        public List<Location> getAllSignsLocations()
        {
            return this.allSignsLocations;
        }

        public List<AtlasEntity> getEdgesLines()
        {
            return this.edgesLines;
        }

        public List<String> getInstructions()
        {
            return this.instructions;
        }

        public Set<AtlasEntity> getNonRouteMembers()
        {
            return this.nonRouteMembers;
        }
    }

}
