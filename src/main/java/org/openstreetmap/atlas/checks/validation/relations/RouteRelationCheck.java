package org.openstreetmap.atlas.checks.validation.relations;

import java.util.*;
import java.io.*;
import java.util.stream.*;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.multi.MultiPoint;
import org.openstreetmap.atlas.geography.atlas.multi.MultiNode;
import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;
import org.openstreetmap.atlas.geography.atlas.items.*;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import org.openstreetmap.atlas.tags.AddressHousenumberTag;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.base.Strings;


/**
 * This check identifies route objects in OSM that have gaps in tracks. a specified street number (addr:housenumber)
 * but no specified street name (addr:street) and are not part of an associated street Relation. No
 * specified street name refers to either having a null value for the street name key, or no street
 * name key present at all.
 *
 * @author Lluc
 */

public class RouteRelationCheck extends BaseCheck<Object>
{

    private static final String EMPTY_ROUTE_INSTRUCTION = "The route in relation with ID = {0,number,#} is empty. Please add this road segments(edges).";
    private static final String MIXED_ROUTE_INSTRUCTION = "The route in relation with ID = {0,number,#} contains both lines and edges, should be either lines or edges.";
    private static final String GAPS_IN_ROUTE_TRACK_INSTRUCTION = "The route in relation with ID = {0,number,#} has gaps in the track.";
    private static final String STOP_TOOFARFROM_ROUTE_TRACK_INSTRUCTION = "The stops in the route relation with ID={0,number,#} are too far from the track.";
    private static final String PLATFORM_TOOFARFROM_ROUTE_TRACK_INSTRUCTION = "The platforms in the route relation with ID={0,number,#} are too far from the track.";
    private static final String ROUTE_MASTER_HAS_NONROUTE_ELEMENT_INSTRUCTION = "The route master relation with ID={0,number,#} contains non route element.";
    private static final String PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INSTRUCTION = "The relation with ID={0,number,#} is a public transportation route and " +
            "has route type being {1}. It should be contained in a Route Master relation.";
    private static final String MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION = "The relation with ID={0,number,#} missing some tags in the category of " +
            "network, operator, ref, a colour.";
    private static final String INCONSISTENT_NETWORK_TAGS_INSTRUCTION = "The relation with ID={0,number,#} has inconsistent network tag with " +
            "its route master.";
    private static final String INCONSISTENT_OPERATOR_TAGS_INSTRUCTION = "The relation with ID={0,number,#} has inconsistent operator tag with " +
            "its route master.";
    private static final String INCONSISTENT_REF_TAGS_INSTRUCTION = "The relation with ID={0,number,#} has inconsistent ref tag with " +
            "its route master.";
    private static final String INCONSISTENT_COLOUR_TAGS_INSTRUCTION = "The relation with ID={0,number,#} has inconsistent colour tag with " +
            "its route master.";
    private static final String TEMP_RELATION_ID_INSTRUCTION = "The relation with ID={0,number,#} is problematic.";


    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            EMPTY_ROUTE_INSTRUCTION, MIXED_ROUTE_INSTRUCTION, GAPS_IN_ROUTE_TRACK_INSTRUCTION,
            STOP_TOOFARFROM_ROUTE_TRACK_INSTRUCTION, PLATFORM_TOOFARFROM_ROUTE_TRACK_INSTRUCTION,
            ROUTE_MASTER_HAS_NONROUTE_ELEMENT_INSTRUCTION, PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INSTRUCTION,
            MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INSTRUCTION , INCONSISTENT_NETWORK_TAGS_INSTRUCTION,
            INCONSISTENT_OPERATOR_TAGS_INSTRUCTION, INCONSISTENT_REF_TAGS_INSTRUCTION, INCONSISTENT_COLOUR_TAGS_INSTRUCTION,
            TEMP_RELATION_ID_INSTRUCTION);


    private static final int EMPTY_ROUTE_INDEX = 0;
    private static final int MIXED_ROUTE_INDEX = 1;
    private static final int GAPS_IN_ROUTE_TRACK_INDEX = 2;
    private static final int STOP_TOOFARFROM_ROUTE_TRACK_INDEX = 3;
    private static final int PLATFORM_TOOFARFROM_ROUTE_TRACK_INDEX = 4;
    private static final int ROUTE_MASTER_HAS_NONROUTE_ELEMENT_INDEX = 5;
    private static final int PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INDEX = 6;
    private static final int MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX = 7;
    private static final int INCONSISTENT_NETWORK_TAGS_INDEX = 8;
    private static final int INCONSISTENT_OPERATOR_TAGS_INDEX = 9;
    private static final int INCONSISTENT_REF_TAGS_INDEX = 10;
    private static final int INCONSISTENT_COLOUR_TAGS_INDEX = 11;
    private static final int TEMP_RELATION_ID_INDEX = 12;


    public static final int DM7_PER_DEGREE = 10_000_000;

    private static final long serialVersionUID = 7671409062471623430L;  //7761409062471623430L
    private static final Logger logger = LoggerFactory.getLogger(RouteRelationCheck.class);


    private static final Set<String> All_transport_Types = Set.<String>of("train", "bus", "railway", "rail", "tram",
            "horse", "dra", "road", "ferry", "boat", "hiking;mtb", "hiking", "foot",
            "detour", "bicycle", "power", "mtb");

    // Cable cars, chair lifts, gondolas, etc         //route => train], bus, 	bus
    private static final Set<String> Public_transport_Types = Set.<String>of("train", "subway", "bus", "trolleybus", "minibus", "light_rail", "share_taxi",
            "railway", "rail", "tram", "aircraft", "ferry");

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
/*
        return object instanceof Relation && (Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.ROUTE_MASTER) || Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.ROUTE))
                && !this.isFlagged(object.getOsmIdentifier());
*/


       //all route masters 1171711000000, 6336539000000, 1183420000000, 5793083000000, 2840654000000 1171481000000
        // 14165000000 14163000000 6685941000000 6683658000000
        return object instanceof Relation && (Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.ROUTE_MASTER))
                && Long.toString(object.getIdentifier()).equals("1171711000000") //6685941000000
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
        instructions.add(this.getLocalizedInstruction(TEMP_RELATION_ID_INDEX, routeRel.getOsmIdentifier()));

        //5793083000000 && Long.toString(object.getIdentifier()).equals("5793083000000")
        if (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE_MASTER))
        {
            logger.info("object.getIdentifier()" + object.getIdentifier());
            logger.info("------------++++process route relation : " + routeRel.toString());

            Set<Relation> routeSet = RouteMember_Route_Rels(routeRel);
            //instructions.add(routeRel.toString());
            //logger.info(routeRel.toString());
            logger.info("String.valueOf(routeSet.size()): " + routeSet.size()+ "size");

            // check track has no gaps. Check stops and platforms are not too far from the track
            for (Relation rel: routeSet) {
                //logger.info("process route relation {} to check track gaps: " + rel.getIdentifier());
                List<String> processRelInstructions = processRel(rel);
                if (!processRelInstructions.isEmpty()) {
                    instructions.addAll(processRelInstructions);
                }
            }

            // check existing non route element
            if (routeSet.size() < routeRel.members().size()){
                instructions.add(this.getLocalizedInstruction(ROUTE_MASTER_HAS_NONROUTE_ELEMENT_INDEX,
                        routeRel.getOsmIdentifier()));
                logger.info("non route elements");
            }

            // check consistent of the network_operator_ref_colour tags
            List<String> tmpInstructions = check_network_operator_ref_colour_tag(routeRel);
            if (!tmpInstructions.isEmpty()){
                instructions.addAll(tmpInstructions);
            }

            // mark all route relation in the route master as flagged
            for (Relation rel: routeSet) {
                this.markAsFlagged(rel.getOsmIdentifier());
                //logger.info(";;;;;;marked as flagged" + Long.toString(rel.getIdentifier())+ ";;;");
            }
        }

        if (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE) )
        {
            logger.info("===////////////////++++process route relation : " + routeRel.toString());
            // check track has no gaps. Check stops and platforms are not too far from the track
            List<String> processRelInstructions = processRel(routeRel);
            if (!processRelInstructions.isEmpty()) {
                logger.info("//////////////////process route relation {} to check track gaps: " + routeRel.getIdentifier());
                instructions.addAll(processRelInstructions);
                //instructions.add(routeRel.toString());
            }

            final Optional<String> transportType = object.getTag("route");
            if (transportType.isPresent()) {

                if (Public_transport_Types.contains(transportType.get())){

                    logger.info("}}}}}}}}}}}}}}}}}}}}}}}} check contained in route master: " + object.getTag("route").get());

                    if (!relContainedInRouteMasters(object)){
                        instructions.add(this.getLocalizedInstruction(PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INDEX,
                                routeRel.getOsmIdentifier(), object.getTag("route").get()));
                        logger.info("&&&&&&&&&&&&&&&&&&&&It should be contained in a Route Master relation: " + object.getIdentifier());
                    }
                }
            }
        }


        logger.info("--------instructions are: " + instructions);
        logger.info("--------instructions end ***: ");
        logger.info("--------routeRel.toString() : " + routeRel.toString());

        // mark this object as flagged
        this.markAsFlagged(object.getOsmIdentifier());
        return instructions.size() == 1 ? Optional.empty()
                : Optional.of(this.createFlag(routeRel.flatten(), instructions));
    }


    /**
     * This is the function that will check to see whether a route has gaps in the track and whether or not a route
     * contains stops and platforms that are too far from the track.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @return a list of strings that are instructions for creating flags
     */
    private List<String> processRel(final Relation rel)
    {
        logger.info("processRel : {}", rel.getIdentifier());
        List<String> instructionsAdd =  checkRouteForGaps(rel);
        //List<String> instructionsAdd =  new ArrayList<>();
        logger.info("&&&&&&&&&&&&&&&&processRel : {}"+rel.toString());

        Set<Location> allStopsLocations = allStopsOrPlatformLocations(rel, "stop");
        Set<Location> allPlatformsLocations = allStopsOrPlatformLocations(rel, "platform");
        Set<PolyLine>  allEdges = PolylineRouteRel(rel);

        logger.info("check stops are too far from track", rel.getIdentifier());
        if (checkStopPlatformTooFarFromTrack(allStopsLocations, allEdges)){
            logger.info("check stops");
            instructionsAdd.add(this.getLocalizedInstruction(STOP_TOOFARFROM_ROUTE_TRACK_INDEX,
                    rel.getOsmIdentifier()));
        }

        logger.info("platforms are too far from track", rel.getIdentifier());
        if (checkStopPlatformTooFarFromTrack(allPlatformsLocations, allEdges)){
            instructionsAdd.add(this.getLocalizedInstruction(PLATFORM_TOOFARFROM_ROUTE_TRACK_INDEX,
                    rel.getOsmIdentifier()));
        }

        return instructionsAdd;
    }

    /**
     * This is the function that will check to see whether a route has gaps in the track and whether or not a route
     * contains stops and platforms that are too far from the track.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     * @return a list of strings that are instructions for creating flags
     */
    private List<String> checkRouteForGaps(final Relation rel)
    {
        logger.info("checkRouteForGapscontainsGapscontainsGapscontainsGaps: " + rel.getIdentifier());
        List<String> instructionsAdd =  new ArrayList<>();

        final List<Edge> allMainEdges = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.EDGE))
                .map(member -> (Edge) member)
                .filter(member -> member.isMainEdge()).collect(Collectors.toList());


        final List<Line> allLines = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.LINE))
                .map(member -> (Line) member).collect(Collectors.toList());

        // Need to have at least one edge or line
        if (allMainEdges.isEmpty() && allLines.isEmpty())
        {
            //logger.info("processRel : empty edges" + rel.getIdentifier());
            instructionsAdd.add(this.getLocalizedInstruction(EMPTY_ROUTE_INDEX,
                    rel.getOsmIdentifier()));
        }

        List<PolyLine> allPolylines = Stream.concat(allMainEdges.stream()
                        .map(member -> member.asPolyLine()),
                allLines.stream().map(member -> member.asPolyLine())).collect(Collectors.toList());

        if (allPolylines.size()>1) {

            LinkedList<PolyLine> createdRoute = RouteFromNonArrangedEdgeSet2(allPolylines);

            logger.info("createdRoute.size(): " + createdRoute.size() + " allPolylines.size():" + allPolylines.size() );


            if (createdRoute.size() < allPolylines.size()) {
                instructionsAdd.add(this.getLocalizedInstruction(GAPS_IN_ROUTE_TRACK_INDEX,
                        rel.getOsmIdentifier()));
            }

            logger.info("come to end/ check gaps");
        }

        return instructionsAdd;
    }

    /**
     * This is the helper function that do the check
     * contains stops and platforms that are too far from the track. This method using the logic in
     * fromNonArrangedEdgeSet(final Set<Edge> candidates, final boolean shuffle) from the route.java.
     * Check by endpoints. If endpoint can be connected then no gap. orders are not considered.
     * Check two end points of an edge to see they can be connected as an edge
     *
     * @param linesInRoute
     *            the set of lines and edges from the route relation combined in a list of PolyLines
     * @return a list of strings that are instructions for creating flags
     */
    private LinkedList<PolyLine> RouteFromNonArrangedEdgeSet(final List<PolyLine> linesInRoute) {
        int numberFailures = 0;
        final List<PolyLine> members = new ArrayList<>();
        members.addAll(linesInRoute);
        LinkedList<PolyLine> routeCreated = new LinkedList<>();
        // initialize routeCreated
        routeCreated.add(members.get(0));

        logger.info("numberFailures at creating: "+ numberFailures);


            //logger.info("numberFailures: "+ numberFailures);
        int previousRouteSize = -1;
        int currentRouteSize = 0;
        while (routeCreated.size() < members.size()
                && previousRouteSize < currentRouteSize
                && numberFailures < members.size()) {

            /* keep adding edges till no way to expand the route*/
            logger.info("/* keep adding edges till no way to expand the route*/");
            for (PolyLine lineMember : members) {
                previousRouteSize = routeCreated.size();

                //Location fistLineStart = routeCreated.getFirst().first();
                //Location lastLineEnd = routeCreated.getLast().last();

                if (routeCreated.contains(lineMember)) {
                    continue;
                }

                logger.info("lineMember: " + lineMember);
                logger.info("routeCreated: " + routeCreated);
                logger.info("routeCreated.getLast().last(): " + routeCreated.getLast().last());
                logger.info("routeCreated.getFirst().first(): " + routeCreated.getFirst().first());

                if (lineMember.first().equals(routeCreated.getLast().last())) {
                    routeCreated.addLast(lineMember);
                    logger.info("addLast(lineMember)");
                    //break;
                } else if (lineMember.last().equals(routeCreated.getFirst().first())) {
                    routeCreated.addFirst(lineMember);
                    logger.info("addFirst(lineMember)");
                    //break;
                }

                currentRouteSize = routeCreated.size();
                logger.info("/* previousRouteSize*/" + previousRouteSize + "routeCreated.size(): " + routeCreated.size());
                //logger.info("/* currentRouteSize*/" + currentRouteSize + "routeCreated.size(): " + routeCreated.size());
            }

            /* the maximal times to run for loop maximal equals to number of total lines
             */
            numberFailures = numberFailures + 1;
        }

        logger.info("/* currentRouteSize routeCreated.size(): " + routeCreated.size()+"   /* numberFailures*/" + numberFailures
                    + "   routeCreated.getFirst(): "+ routeCreated.getFirst()+ "   members.size(): " + members.size());
        logger.info("  members: " + members);
        for (PolyLine lineMember : members){
            logger.info("lineMember.first(): "+ lineMember.first()+ " lineMember.last(): "+ lineMember.last());
        }


        logger.info("exit numberFailures: "+ numberFailures + "members.size(): " + members.size() + "routeCreated.size(): " + routeCreated.size());

        return routeCreated;
    }

    private LinkedList<PolyLine> RouteFromNonArrangedEdgeSet2(List<PolyLine> linesInRoute) {
        int numberFailures = 0;
        final List<PolyLine> members = new ArrayList<>();
        members.addAll(linesInRoute);
        LinkedList<PolyLine> routeCreated = new LinkedList<>();
        // initialize routeCreated
        routeCreated.add(members.get(0));


        logger.info("here check");
        logger.info("numberFailures at creating: " + numberFailures);
        logger.info("members: "+ members);


        int previousRouteSize = -1;
        int currentRouteSize = 0;
        PolyLine startConectLine = null;
        PolyLine endConectLine = null;
        // check to append both start and end of the created route
        Distance startMinDistance = null;
        Distance endMinDistance = null;

        while (routeCreated.size() < members.size()
                && previousRouteSize < currentRouteSize
                && numberFailures < members.size()) {


            /* keep adding edges till no way to expand the route*/
            logger.info("/* keep adding edges till no way to expand the route*/");
            for (PolyLine lineMember : members) {
                previousRouteSize = routeCreated.size();

                if (routeCreated.contains(lineMember)) {
                    continue;
                }

                if (lineMember.first().equals(routeCreated.getLast().last())) {
                    routeCreated.addLast(lineMember);
                    //ableToAdd = true;
                    logger.info("addLast(lineMember)");
                    //break;
                } else if (lineMember.last().equals(routeCreated.getFirst().first())) {
                    routeCreated.addFirst(lineMember);
                    //ableToAdd = true;
                    logger.info("addFirst(lineMember)");
                    //break;
                }
                // append at beginning
                Distance tmpStartDistance = lineMember.last().distanceTo(routeCreated.getFirst().first());
                if (startMinDistance==null || tmpStartDistance.isLessThan(startMinDistance)){
                    startMinDistance = tmpStartDistance;
                    startConectLine = lineMember;
                    logger.info("startMinDistance " + startMinDistance + "lineMember " + lineMember);
                }

                // append at end
                Distance tmpEndDistance = lineMember.first().distanceTo(routeCreated.getLast().last());
                if (endMinDistance==null || tmpEndDistance.isLessThan(startMinDistance)){
                    endMinDistance = tmpEndDistance;
                    endConectLine = lineMember;
                }

                currentRouteSize = routeCreated.size();
                logger.info("/* previousRouteSize*/" + previousRouteSize + "routeCreated.size(): " + routeCreated.size());
            }

            logger.info(" in while loop end startMinDistance: " + startMinDistance
                    + " startConectLine.first(): " + startConectLine.first()
                    + " startConectLine.last(): " + startConectLine.last()
                    + " endMinDistance: " + endMinDistance
                    + " endConectLine.first(): " + endConectLine.first()
                    + " endConectLine.last(): " + endConectLine.last()
                    + " routeCreated.size(): " + routeCreated.size()
                    + " numberFailures: " + numberFailures);


            // check if route can be expanded with allowed distance

            if (routeCreated.size() < members.size()){

                if (startMinDistance.isLessThan(Distance.meters(10))){
                    routeCreated.addFirst(startConectLine);
                }

                if (endMinDistance.isLessThan(Distance.meters(10))){
                    routeCreated.addLast(endConectLine);
                }

            }

            //the maximal times to run loop equals to number of total lines
            numberFailures = numberFailures + 1;
        }

        logger.info(" loop end startMinDistance: " + startMinDistance
                + " startConectLine.first(): " + startConectLine.first()
                + " startConectLine.last(): " + startConectLine.last()
                + " endMinDistance: " + endMinDistance
                + " endConectLine.first(): " + endConectLine.first()
                + " endConectLine.last(): " + endConectLine.last()
                + " routeCreated.size(): " + routeCreated.size()
                + " numberFailures: " + numberFailures);

        return routeCreated;

    }


    /**
     * This is the function that will check to see whether a set of stops or platforms that are too far from the track.
     *
     * @param allSignsOrPlatformsLocations
     *            the se of points representing the stops or platforms in the route.
     * @param allEdgePolyLines
     *      *     the set of polylines from either edge or line contained in the route
     * @return a boolean yes if stops or platforms are too far from the track. Otherwise no.
     */
    private boolean checkStopPlatformTooFarFromTrack(final Set<Location> allSignsOrPlatformsLocations, final Set<PolyLine> allEdgePolyLines) {
        logger.info("checkStopPlatformTooFarFromTrack");

        if (allSignsOrPlatformsLocations.isEmpty() || (allEdgePolyLines.isEmpty())) {
            return false;
        }

        SnappedLocation minSnap = null;

        for (Location location : allSignsOrPlatformsLocations) {


            for (PolyLine edges : allEdgePolyLines) {
                SnappedLocation snappedTo = location.snapTo(edges);
                if (minSnap == null || snappedTo.compareTo(minSnap) < 0) {
                    minSnap = snappedTo;
                }

                if (minSnap.getDistance().isLessThan(Distance.meters(1.5))) {
                    //logger.info("true checkDistance : minSnap.getDistance() {}", true);
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * This is the helper function for checkStopPlatformTooFarFromTrack that checks whether or not
     * stops and platforms in the route are too far from the track.
     *
     * @param rel
     *            the relation entity supplied by the Atlas-Checks framework for evaluation
     *  @param stopOrPlatform
     *             indicate whether we want locations for stops or platforms
     * @return a list of locations for either stops or platforms
     */
    private Set<Location> allStopsOrPlatformLocations(final Relation rel, final String stopOrPlatform){

        logger.info("start check stops: ");
        Set<AtlasEntity> allSigns = rel.members().stream()
                .filter(member -> member.getRole().equals(stopOrPlatform))
                .map(RelationMember::getEntity).collect(Collectors.toSet());

        logger.info("rel"+rel.toString());

        Set<Location> allLocations  = new HashSet<Location>();

        for (AtlasEntity entity : allSigns) {

            logger.info("--allLocations0:" + allLocations);
            if (entity instanceof MultiPoint) {
                logger.info("--allLocations1:" + allLocations);
                allLocations.add(((MultiPoint) entity).getLocation());
            } else if (entity instanceof MultiNode) {
                logger.info("--allLocations2:" + allLocations);
                allLocations.add(((MultiNode) entity).getLocation());
            } else if (entity instanceof Node) {
                logger.info("--allLocations3:" + allLocations);
                allLocations.add(((Node) entity).getLocation());
            } else if (entity instanceof Point) {
                logger.info("--allLocations4:" + allLocations);
                allLocations.add(((Point) entity).getLocation());
            }
        }

        logger.info("--allLocations:" + allLocations);
        return allLocations;
    }


    /**
     * @param object
     *
     * @return an instance of CheckRouteMasterValues containing information about
     * whether or not this public transport route is contained in a route master
     */
    private boolean relContainedInRouteMasters(final AtlasObject object)
    {
        Iterable<Relation>  relationsInAtlas = object.getAtlas().relations();
        final List<String> instructions = new ArrayList<>();
        final boolean flag;

        logger.info("+++<<<<<<<<<<< relContainedInRouteMasters"+object.getIdentifier());
        Spliterator<Relation>
                spliterator = relationsInAtlas.spliterator();

        Set<String> matchedRouteID = StreamSupport.stream(spliterator, false)
                .filter(relation -> Validators.isOfType(relation, RelationTypeTag.class, RelationTypeTag.ROUTE_MASTER))
                .flatMap(relation -> relation.members().stream()
                        .map(RelationMember::getEntity)
                        .filter(member -> member.getType().equals(ItemType.RELATION))
                        .filter(member -> Validators.isOfType(member, RelationTypeTag.class,
                                RelationTypeTag.ROUTE))
                        .filter(member -> Long.toString(member.getIdentifier()).equals(Long.toString(object.getIdentifier())))
                        .map(member -> Long.toString(member.getIdentifier())))
                .collect(Collectors.toSet());

        logger.info("matchedRouteID" + matchedRouteID);

        if (matchedRouteID.size()>0){
            return true;
        }

        return false;
    }


    /**
     * Return the list of instructions that describes inconsistency of any tags in the group of
     * network, operator, ref, and colour between a route master and its member routes
     *
     * @param rel
     *            The route master relation under check
     * @return the list of instructions that describes inconsistency
     */
    private List<String> check_network_operator_ref_colour_tag(final Relation rel)
    {
        List<String> instructionsAdd =  new ArrayList<>();
        final Optional<String> networkTag = rel.getTag("network");
        final Optional<String> operatorTag = rel.getTag("operator");
        final Optional<String> refTag = rel.getTag("ref");
        final Optional<String> colourTag = rel.getTag("colour");

        if (!networkTag.isPresent() || !operatorTag.isPresent()  || !refTag.isPresent() || !colourTag.isPresent()) {
            Optional.of(this.getLocalizedInstruction(MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX,
                    rel.getOsmIdentifier()));
        }

        Set<Relation> routeSet = RouteMember_Route_Rels(rel);

        logger.info("rel:"+rel);

        for (Relation relRoute: routeSet) {
            final Optional<String> routeNetwork = relRoute.getTag("network");
            final Optional<String> routeOperator = relRoute.getTag("operator");
            final Optional<String> routeRef = relRoute.getTag("ref");
            final Optional<String> routeColour = relRoute.getTag("colour");

            logger.info("relRoute:"+relRoute);

            if (!routeNetwork.isPresent() || !operatorTag.isPresent()
                    || !routeRef.isPresent() || !routeColour.isPresent()) {
                Optional.of(this.getLocalizedInstruction(MISSING_NETWORK_OPERATOR_REF_COLOUR_TAGS_INDEX,
                        rel.getOsmIdentifier()));
            }

            if (routeNetwork.isPresent() && networkTag.isPresent()) {
                if (!routeNetwork.equals(networkTag)) {
                    instructionsAdd.add(this.getLocalizedInstruction(INCONSISTENT_NETWORK_TAGS_INDEX, rel.getOsmIdentifier()));
                }
            }

            if (routeOperator.isPresent() && operatorTag.isPresent()) {
                if (!routeOperator.equals(operatorTag)) {
                    instructionsAdd.add(this.getLocalizedInstruction(INCONSISTENT_OPERATOR_TAGS_INDEX, rel.getOsmIdentifier()));
                }
            }

            if (routeRef.isPresent() && refTag.isPresent()) {
                if (!routeRef.equals(refTag)) {
                    instructionsAdd.add(this.getLocalizedInstruction(INCONSISTENT_REF_TAGS_INDEX, rel.getOsmIdentifier()));
                }
            }

            if (routeColour.isPresent() && colourTag.isPresent()) {
                if (!routeColour.equals(colourTag)) {
                    instructionsAdd.add(this.getLocalizedInstruction(INCONSISTENT_COLOUR_TAGS_INDEX, rel.getOsmIdentifier()));
                }
            }
        }

        return instructionsAdd;
    }

    /**
     * This is the function that will collect all the edges and lines in the relation into one set.
     *
     * @param rel
     *
     * @return all the edges and lines in the relation in one set.
     */
    private Set<PolyLine> PolylineRouteRel(final Relation rel)
    {
        //edges in the route RelationMember::getRole)
        Set<PolyLine> allEdges = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.EDGE))
                .map(member -> (Edge) member)
                .map(member -> member.asPolyLine()).collect(Collectors.toSet());

        Set<PolyLine> allLines = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.LINE))
                .map(member -> (Line) member)
                .map(member -> member.asPolyLine()).collect(Collectors.toSet());

        return Stream.of(allEdges, allLines).flatMap(x -> x.stream())
                .collect(Collectors.toSet());
    }

    /**
     * @param rel
     *
     * @return set of route relations contained in the route master
     */
    private Set<Relation> RouteMember_Route_Rels(final Relation rel)
    {
        Set<Relation> routeSet = rel.members().stream()
                .map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.RELATION))
                .map(member -> (Relation) member)
                .filter(member -> Validators.isOfType(member, RelationTypeTag.class,
                        RelationTypeTag.ROUTE)).collect(Collectors.toSet());

        return routeSet;
    }

    // Function to get the Stream
    public static <T> Stream<T>
    getStreamFromIterable(Iterable<T> iterable)
    {

        // Convert the Iterable to Spliterator
        Spliterator<T>
                spliterator = iterable.spliterator();

        // Get a Sequential Stream from spliterator
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}

/*

"RoutePrintCheck": {
    "enabled": true,
    "surface": {
      "maximum": 1000.0,
      "minimum": 50.0
    }
  },


Details
Class 1 "The track of this route contains gaps" :
Class 2 "The stop or platform is too far from the track of this route" :
Class 3 "Non route relation member in route_master relation" :
Class 4 "Public transport relation route not in route_master relation" :
Class 5 "network, operator, ref, colour tag should be the same on route and route_master relations" :
Fix
Class 1 "The track of this route contains gaps" :
Class 2 "The stop or platform is too far from the track of this route" :
Class 3 "Non route relation member in route_master relation" :
Class 4 "Public transport relation route not in route_master relation" :
Class 5 "network, operator, ref, colour tag should be the same on route and route_master relations" :

*/



