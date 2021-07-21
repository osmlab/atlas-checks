package org.openstreetmap.atlas.checks.validation.relations;

import java.util.*;
import java.io.*;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Snapper;
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

public class RoutePrintCheck extends BaseCheck<Object>
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
    private static final Logger logger = LoggerFactory.getLogger(Relation.class);


    Set<String> All_transport_Types = Set.<String>of("train", "bus", "railway", "rail", "tram",
            "horse", "dra", "road", "ferry", "boat", "hiking;mtb", "hiking", "foot",
            "detour", "bicycle", "power", "mtb");

    // Cable cars, chair lifts, gondolas, etc         //route => train], bus,
    Set<String> Public_transport_Types = Set.<String>of("train", "bus", "railway", "rail", "tram", "aircraft", "ferry");

    public RoutePrintCheck(final Configuration configuration)
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
        //route master: 1171711, 6336539, 1183420, 5793083, 2840654, 1171481,
        // 14165 14163 6685941 6683658
        // 1171711 6336539 1183420 5793083 2840654 5317705
        // 14165 14163 6685941
        //6683658
        /*return object instanceof Relation;  Validators.isOfType(object, RelationTypeTag.class,
        //                RelationTypeTag.PUBLIC_TRANSPORT) &&
        // || Validators.isOfType(object, RelationTypeTag.class,
        //                RelationTypeTag.ROUTE)
        //  && Long.toString(object.getOsmIdentifier()).equals("5317705")
                      && Long.toString(object.getOsmIdentifier()).equals("5317705")*/

        /*
        return object instanceof Relation && (Validators.isOfType(object, RelationTypeTag.class,
                               RelationTypeTag.ROUTE_MASTER) || (Validators.isOfType(object, RelationTypeTag.class,
                                       RelationTypeTag.ROUTE) && Long.toString(object.getOsmIdentifier()).equals("5317705")))
                               && !this.isFlagged(object.getOsmIdentifier());
                               */

        /*
        return object instanceof Relation && (Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.ROUTE) && Long.toString(object.getOsmIdentifier()).equals("5317705"))
                && !this.isFlagged(object.getOsmIdentifier());*/

        return object instanceof Relation && (Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.ROUTE) || Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.ROUTE_MASTER))
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
            logger.info("///???" + object.getIdentifier());
            Set<Relation> routeSet = RouteMember_Route_Rels(routeRel);
            logger.info(routeRel.toString());
            logger.info("String.valueOf(routeSet.size()): " +String.valueOf(routeSet.size())+ "size");

            // check track has no gaps. Check stops and platforms are not too far from the track
            for (Relation rel: routeSet) {
                // instructions.add(rel.toString());
                //final List<String> instructionsAdd = processRel(rel);
                logger.info(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,process route relation {} to check track gaps: " + rel.getIdentifier());
                List<String> processRelInstructions = processRel(rel);
                if (!processRelInstructions.isEmpty()) {
                    instructions.addAll(processRelInstructions);
                }
            }

            // check existing non route element
            if (routeSet.size() < routeRel.members().size()){
                instructions.add(this.getLocalizedInstruction(ROUTE_MASTER_HAS_NONROUTE_ELEMENT_INDEX,
                        routeRel.getOsmIdentifier()));
                logger.info("///???");
                //instructions.add(routeRel.toString());
                //instructions.add(String.valueOf(routeSet.size())+ "pp");
            }

            // check consistent of the network_operator_ref_colour tags
            List<String> tmpInstructions = check_network_operator_ref_colour_tag(routeRel);
            if (!tmpInstructions.isEmpty()){
                instructions.addAll(tmpInstructions);
            }

            // mark all route relation in the route master as flagged
            for (Relation rel: routeSet) {
                //this.markAsFlagged(((AtlasObject) rel).getOsmIdentifier());
                this.markAsFlagged(rel.getOsmIdentifier());
                logger.info(";;;;;;marked as flagged" + Long.toString(rel.getIdentifier())+ ";;;");
                logger.info(";;;;;;marked as flagged" + Long.toString(rel.getIdentifier())+ ";;;");
                //instructions.add(rel.toString());
            }
        }

        if (Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.ROUTE) )
        {
            logger.info("////////////////++++process route relation : " + routeRel.getIdentifier());
            // check track has no gaps. Check stops and platforms are not too far from the track
            List<String> processRelInstructions = processRel(routeRel);
            if (!processRelInstructions.isEmpty()) {
                logger.info("//////////////////process route relation {} to check track gaps: " + routeRel.getIdentifier());
                instructions.addAll(processRelInstructions);
                //instructions.add(routeRel.toString());
            }

            if (object.getTag("route").isPresent()) {
                logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> pubic or not: " + object.getTag("route").get());
                if (!Public_transport_Types.contains(object.getTag("route").get())
                        && All_transport_Types.contains(object.getTag("route").get())) {
                    logger.info("}}}}}}}}}}}}}}}}}}}}}}}} pubic or not: " + object.getTag("route").get());                }

                if (Public_transport_Types.contains(object.getTag("route").get())){

                    logger.info("}}}}}}}}}}}}}}}}}}}}}}}} check contained in route master: " + object.getTag("route").get());

                    CheckRouteMasterValues rv = relContainedInRouteMasters(object);

                    if (!rv.getFlag()){
                        instructions.add(this.getLocalizedInstruction(PUBLIC_TRANSPORT_ROUTE_NOT_IN_ROUTE_MASTER_INDEX,
                                routeRel.getOsmIdentifier(), object.getTag("route").get()));
                        //instructions.addAll(rv.getInstructions());
                        logger.info("&&&&&&&&&&&&&&&&&&&&(((((((((((( pubic: " + object.getTag("route").get());
                        logger.info("&&&&&&&&&&&&&&&&&&&&(((((((((((( pubic: " + object.getTag("route").get());
                    }
                }
            }
        }

        //instructions.addAll(routettypes);

        logger.info("--------instructions are: " + instructions);

        // mark this object as flagged
        this.markAsFlagged(object.getOsmIdentifier());
        return instructions.size() == 1 ? Optional.empty()
                : Optional.of(this.createFlag(routeRel.flatten(), instructions));
    }

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

    private CheckRouteMasterValues relContainedInRouteMasters(final AtlasObject object)
    {
        Iterable<Relation>  relationsInAtlas = object.getAtlas().relations();
        final List<String> instructions = new ArrayList<>();
        final boolean flag;

        for( Relation rel : relationsInAtlas ){
            if (Validators.isOfType(rel, RelationTypeTag.class, RelationTypeTag.ROUTE_MASTER) ){
                Set<String> allRouteRelIds = rel.members().stream()
                        .map(RelationMember::getEntity)
                        .filter(member -> member.getType().equals(ItemType.RELATION))
                        .map(member -> (Relation) member)
                        .filter(member -> Validators.isOfType(member, RelationTypeTag.class,
                                RelationTypeTag.ROUTE))
                        .map(member -> Long.toString(member.getIdentifier())).collect(Collectors.toSet());


                if (allRouteRelIds.contains(Long.toString(object.getIdentifier()))){
                    instructions.add("+++ " + rel.toString()+ " ~~~~~");
                    instructions.add("=== "+object.getIdentifier()+" ===");

                    logger.info(rel.toString());
                    logger.info("=== "+object.getIdentifier()+" ===");
                    flag = true;
                    CheckRouteMasterValues rv = new CheckRouteMasterValues(instructions, flag);
                    return rv;
                }
            }
        }

        flag = false;
        CheckRouteMasterValues rv = new CheckRouteMasterValues(instructions, flag);
        return rv;
    }

    public final class CheckRouteMasterValues {
        private final List<String> instructions;
        private final boolean flag;

        public CheckRouteMasterValues(List<String> instructions, boolean flag) {
            this.instructions = instructions;
            this.flag = flag;
        }

        public boolean getFlag(){
            return flag;
        }

        public List<String> getInstructions(){
            return instructions;
        }
    }

    private List<String> processRel(final Relation rel)
    {
        logger.info("processRel : {}", rel.getIdentifier());
        //if (logger.isWarnEnabled())

        List<String> instructionsAdd =  checkRouteForGaps(rel);

        if (StopsToofar(rel)){
            logger.info("STOP_TOOFARFROM_ROUTE_TRACK_ : {}", rel.getIdentifier());
            instructionsAdd.add(this.getLocalizedInstruction(STOP_TOOFARFROM_ROUTE_TRACK_INDEX,
                    rel.getOsmIdentifier()));
        }

        if (PlatformsToofar(rel)){
            logger.info("PLATFORM_TOOFARFROM_ROUTE_TRACK : {}", rel.getIdentifier());
            instructionsAdd.add(this.getLocalizedInstruction(PLATFORM_TOOFARFROM_ROUTE_TRACK_INDEX,
                    rel.getOsmIdentifier()));
        }

        return instructionsAdd;
    }

    private List<String> checkRouteForGaps(final Relation rel)
    {
        logger.info("checkRouteForGapscontainsGapscontainsGapscontainsGaps: " + rel.getIdentifier());
        List<String> instructionsAdd =  new ArrayList<>();

        final List<Edge> allMainEdges = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.EDGE))
                .map(member -> (Edge) member)
                .filter(member -> member.isMainEdge()).collect(Collectors.toList());

        final List<Edge> allEdges = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.EDGE))
                .map(member -> (Edge) member).collect(Collectors.toList());


        final List<Line> allLines = rel.members().stream().map(RelationMember::getEntity)
                .filter(member -> member.getType().equals(ItemType.LINE))
                .map(member -> (Line) member).collect(Collectors.toList());

        // Need to have at least one edge or line
        if (allMainEdges.isEmpty() && allLines.isEmpty())
        {
            logger.info("processRel : empty edges" + rel.getIdentifier());
            instructionsAdd.add(this.getLocalizedInstruction(EMPTY_ROUTE_INDEX,
                    rel.getOsmIdentifier()));
        }


        List<PolyLine> allPolylines = Stream.concat(allMainEdges.stream()
                        .map(member -> member.asPolyLine()),
                allLines.stream().map(member -> member.asPolyLine())).collect(Collectors.toList());


        logger.info("~~~~ allEdges.size(): " + allEdges.size() + "~~~~ allLines.size(): " + allLines.size()+
                "~~~~ allPolylines.size(): " + allPolylines.size());
        logger.info("~~~~ DM7_PER_DEGREE: " + DM7_PER_DEGREE);
        //DM7_PER_DEGREE

        logger.info("edges in : ; " + Long.toString(rel.getIdentifier())+ "===");
        if (allEdges.size()>1){
            for (Edge edge: allEdges){
                logger.info("edgestart " +Long.toString(edge.start().getIdentifier()));
                logger.info("edgeend " +Long.toString(edge.end().getIdentifier()));
                logger.info("location start " +edge.start().getLocation().toString());
                logger.info("2 latitude Lontitude" +edge.start().getLocation().getLatitude().asRadians()
                        +", "+edge.start().getLocation().getLongitude().asRadians());
                logger.info("location end " +edge.end().getLocation().toString());
                logger.info("latitude Lontitude" +edge.end().getLocation().getLatitude().asRadians()
                +", "+edge.end().getLocation().getLongitude().asRadians());
                logger.info("edgeId " +Long.toString(edge.getIdentifier()));
            }
        }

        logger.info("end~~~edges in : ; " + Long.toString(rel.getIdentifier()));

        logger.info("main edges in : ; " + Long.toString(rel.getIdentifier())+ "===");
        if (allMainEdges.size()>1){
            for (Edge edge: allMainEdges){
                logger.info("edgestart " +Long.toString(edge.start().getIdentifier()));
                logger.info("edgeend " +Long.toString(edge.end().getIdentifier()));
                logger.info("edgeId " +Long.toString(edge.getIdentifier()));
            }
        }

        logger.info("end~~~main edges in : ; " + Long.toString(rel.getIdentifier()));

        if (allLines.size()>1){
            for (Line line: allLines){
                logger.info(line.asPolyLine().first().toString());
                logger.info("latitude Lontitude first " +line.asPolyLine().first().getLatitude().asRadians()
                        +", "+line.asPolyLine().first().getLongitude().asRadians());
                logger.info(line.asPolyLine().last().toString());
                logger.info("2 latitude Lontitude last " +line.asPolyLine().last().getLatitude().asRadians()
                        +", "+line.asPolyLine().last().getLongitude().asRadians());
                //instructionsAdd.add("latitude2 " +String.valueOf(edge.end().getLocation().getLatitude().asDm7()));
                //instructionsAdd.add("latitude3 " +String.valueOf(edge.end().getLocation().getLatitude().asDegrees()));
                logger.info("edgeId " +Long.toString(line.getIdentifier()));
                logger.info(line.toString());
            }
        }
        logger.info("end~~~lines in 5317705000000: ; " + Long.toString(rel.getIdentifier()));

        if (allPolylines.size()>1) {
            logger.info("////: allPolylines");
            List<List<Location>> endPointLists = new ArrayList<>();
            for (int i = 0; i < allPolylines.size(); i++) {
                endPointLists.add(Arrays.asList(allPolylines.get(i).first(), allPolylines.get(i).last()));
                logger.info("////:" + allPolylines.get(i).first() + "\\\\\\:" + allPolylines.get(i).last());
                //PolyLine line =allPolylines.get(i);
                logger.info("latitude Lontitude" +allPolylines.get(i).first().getLatitude().asRadians()
                        +", "+allPolylines.get(i).first().getLongitude().asRadians());
                logger.info("2 latitude Lontitude" +allPolylines.get(i).last().getLatitude().asRadians()
                        +", "+allPolylines.get(i).last().getLongitude().asRadians());
            }

            logger.info("////: endPointLists");
            for (int i = 0; i < allPolylines.size(); i++) {
                endPointLists.get(i);
                logger.info("////:" + endPointLists.get(i).get(0) + "\\\\\\:" + endPointLists.get(i).get(1));
            }

            logger.info("//// over: endPointLists");


            CheckRouteMasterValues rv = containsGaps(endPointLists, rel);
            logger.info("(((((((((all edges and lines");

            if (rv.getFlag()){
                instructionsAdd.add(this.getLocalizedInstruction(GAPS_IN_ROUTE_TRACK_INDEX,
                        rel.getOsmIdentifier()));
                if (!rv.getInstructions().isEmpty()) {
                    instructionsAdd.addAll(rv.getInstructions());
                }
                logger.info("relation have gaps in track Edge case " + Long.toString(rel.getIdentifier()));
            }
        }

        return instructionsAdd;
    }

    /*
    check there are gaps in track. Check by endpoints. it endpoint can be connected then no gap. orders are
    not considered. Check two end points of an edge. Each route can at most have two edges that have one end
    that is not connected to other edges
     */
    private CheckRouteMasterValues containsGaps(final List<List<Location>>endpoints, final Relation rel)
    {
        logger.info("contains Gaps containsGapscontainsGaps: " + rel.getIdentifier());
        final List<String> instructions = new ArrayList<>();
        Distance minDisStart = null;
        Distance minDisEnd = null;
        Location start1 = null;
        Location end1 = null;
        Location start2 = null;
        Location end2 = null;
        List<Location> edge1 = null;
        List<Location> edge2Start = null;
        List<Location> edge2End = null;
        //List<Location> edge2End = null;
        int floatingNodeCnt = 0;


        for (int i = 0; i < endpoints.size(); i++) {
            edge1 = endpoints.get(i);
            start1 = edge1.get(0);
            start2 = edge1.get(1);

            if (i == endpoints.size()-1){
                edge2Start = endpoints.get(i-1);
                edge2End = endpoints.get(i-1);
            }else{
                edge2Start = endpoints.get(i+1);
                edge2End = endpoints.get(i+1);
            }

            minDisStart = edge1.get(0).distanceTo(edge2Start.get(0));
            minDisEnd = edge1.get(1).distanceTo(edge2Start.get(0));

            for (int j = 0; j < endpoints.size(); j++) {
                if (j != i) {

                    if (edge1.get(0).distanceTo(endpoints.get(j).get(0)).isLessThan(minDisStart)
                    || end1 == null){
                        minDisStart = edge1.get(0).distanceTo(endpoints.get(j).get(0));
                        end1 = endpoints.get(j).get(0);
                        edge2Start = endpoints.get(j);
                    }

                    if (edge1.get(0).distanceTo(endpoints.get(j).get(1))
                            .isLessThan(minDisStart) || end1 == null){
                        minDisStart = edge1.get(0).distanceTo(endpoints.get(j).get(1));
                        end1 = endpoints.get(j).get(1);
                        edge2Start = endpoints.get(j);
                    }

                    if (edge1.get(1).distanceTo(endpoints.get(j).get(0)).isLessThan(minDisEnd) || end2==null){
                        minDisEnd = edge1.get(1).distanceTo(endpoints.get(j).get(0));
                        end2 = endpoints.get(j).get(0);
                        edge2End = endpoints.get(j);
                    }

                    if (edge1.get(1).distanceTo(endpoints.get(j).get(1)).isLessThan(minDisEnd) || end2==null){
                        minDisEnd = edge1.get(1).distanceTo(endpoints.get(j).get(1));
                        end2 = endpoints.get(j).get(1);
                        edge2End = endpoints.get(j);
                    }
                }
            }

            logger.info("minDisStart {} for {}:" + minDisStart + Long.toString(rel.getIdentifier()) +
                    "start1 :" + start1 +"end1 :" + end1 + "}}}");
            logger.info("edge1 :" + edge1);
            logger.info("edge2Start :" + edge2Start);
            logger.info("minDisEnd {} for {}:" + minDisEnd + Long.toString(rel.getIdentifier()) +
                    "start2 :" + start2 +
                    "end2 :" + end2 + "}}}");
            logger.info("edge1 :" + edge1);
            logger.info("edge2End :" + edge2End);


            if (minDisStart.isLessThan(Distance.meters(10))){
                logger.info("minDisStart has no gap {}"+ "--" + Long.toString(rel.getIdentifier()));
                instructions.add("minDisStart has no gap {}"+ "--" + Long.toString(rel.getIdentifier()));

            }else{
                logger.info("minDisStart has gap {}"+ "--" + Long.toString(rel.getIdentifier()));
                instructions.add("minDisStart has gap {}"+ "--" + Long.toString(rel.getIdentifier()));
                floatingNodeCnt = floatingNodeCnt +1;
            }


            if (minDisEnd.isLessThan(Distance.meters(10))){
                logger.info("minDisEnd has no gap {}"+ "--" + Long.toString(rel.getIdentifier()));

            }else{
                logger.info("minDisEnd has gap {}"+ "--" + Long.toString(rel.getIdentifier()));
                floatingNodeCnt = floatingNodeCnt +1;
            }
        }


        if (floatingNodeCnt <= 2){
            logger.info("has no gap {}"+ "--" + Long.toString(rel.getIdentifier()));
            CheckRouteMasterValues rv = new CheckRouteMasterValues(instructions, false);
            return rv;
        }

        logger.info("has gap {}"+ "--" + Long.toString(rel.getIdentifier()));
        instructions.add("has gap {}"+ "--" + Long.toString(rel.getIdentifier()));
        CheckRouteMasterValues rv = new CheckRouteMasterValues(instructions, true);
        return rv;
    }

    private boolean StopsToofar(final Relation rel)
    {
       /* Set<RelationMember> allSigns = rel.members().stream()
                .filter(member -> member.getRole().equals("sign")).collect(Collectors.toSet());*/

        Set<Point> allSigns = rel.members().stream()
                .filter(member -> member.getRole().equals("stop"))
                .map(RelationMember::getEntity)
                .map(member -> (Point) member).collect(Collectors.toSet());

        logger.info("StopToofar : minSnap.getDistance() {}", this);
        Set<PolyLine> allEdges = PolylineRouteRel(rel);
        return checkStopPlatformTooFarFromTrack(allSigns, allEdges);
    }

    private boolean PlatformsToofar(final Relation rel)
    {
        Set<Point> allPlatforms = rel.members().stream()
                .filter(member -> member.getRole().equals("sign"))
                .map(RelationMember::getEntity)
                .map(member -> (Point) member).collect(Collectors.toSet());

        logger.info("PlatformToofar : minSnap.getDistance() {}", this);
        Set<PolyLine> allEdges = PolylineRouteRel(rel);
        return checkStopPlatformTooFarFromTrack(allPlatforms, allEdges);

    }

    // check distance of signs and platforms
    private boolean checkStopPlatformTooFarFromTrack(Set<Point> allSignsorPlatforms, final Set<PolyLine> allEdgePolyLines)
    {
        Set<Location> pointsLoacations = allSignsorPlatforms.stream().map(point -> point.getLocation()).collect(Collectors.toSet());

        if (allSignsorPlatforms.isEmpty() || (pointsLoacations.isEmpty())){
            return false;
        }

        Snapper.SnappedLocation minSnap = null;

        for (Location location : pointsLoacations) {

            for (PolyLine edges : allEdgePolyLines) {
                //SnappedLocation snapedTo = location.snapTo(edges);
                if (minSnap == null || location.snapTo(edges).compareTo(minSnap)<0){
                    minSnap = location.snapTo(edges);
                }
            }
        }

        logger.info("checkStopPlatformTooFarFromTrack : minSnap.getDistance() {}", minSnap.getDistance().asMillimeters());

        //isGreaterThan equals(Distance.meters(0))
        if (minSnap.getDistance().isGreaterThan(Distance.meters(1.5)))
        {
            logger.info("true checkDistance : minSnap.getDistance() {}", true);
            return true;
        }

        logger.info("false checkDistance : minSnap.getDistance() {}", false);

        return false;
    }


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



    private boolean disconnectedFromOthers(final List<Location> endpoints, final List<PolyLine> EdgePolyLines, int idxRemove)
    {
        Snapper.SnappedLocation minSnap = null;

        for (int i = 0; i < endpoints.size(); i++) {
            for (int j = 0; j < EdgePolyLines.size(); j++) {
                if (j != idxRemove) {
                    if (minSnap == null || endpoints.get(i).snapTo(EdgePolyLines.get(j)).compareTo(minSnap) < 0) {
                        minSnap = endpoints.get(i).snapTo(EdgePolyLines.get(j));
                    }
                }
            }
        }

        logger.info("disconnectedToOthers : minSnap.getDistance() {}", minSnap.getDistance().asMillimeters());

        if (minSnap.getDistance().isGreaterThan(Distance.meters(1.5)))
        {
            logger.info("disconnectedToOthers : minSnap.getDistance() true");
            return true;
        }
        logger.info("disconnectedToOthers : false minSnap.getDistance() {}", false);

        return false;
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



