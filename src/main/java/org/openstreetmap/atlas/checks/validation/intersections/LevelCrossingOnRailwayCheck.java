package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check is to detect and flag nodes under the three scenarios below: 1) When a railway crosses
 * a highway, but intersection node is missing. 2) When railway/highway intersection node exists,
 * but railway=level_crossing tag is missing. 3) When tag railway=level_crossing exists, on a node,
 * but is lacking of either highway or railway going through the node (osmose 7090), or not on a
 * node, instead, on the related way features (osmose 9015)
 *
 * @author aiannicelli
 */
public class LevelCrossingOnRailwayCheck extends BaseCheck
{
    private static final Long OSM_LAYER_DEFAULT = 0L;
    private final Long layerDefault;
    private static final String INVALID_TAGGED_OBJECT = "The object (OSM ID: {0,number,#}) is tagged with "
            + "railway=level_crossing but is not a node. Remove tag.";
    private static final int INVALID_TAGGED_OBJECT_INDEX = 0;
    private static final String INVALID_TAGGED_INTERSECTION = "The intersection node (OSM ID: {0,number,#}) is "
            + "missing a railway=level_crossing tag. Add the appropriate tag to the node.";
    private static final int INVALID_TAGGED_INTERSECTION_INDEX = 1;
    private static final String INVALID_TAGGED_NODE = "The node (OSM ID: {0,number,#}) is tagged with "
            + "railway=level_crossing but is not the intersection of a railway and highway. Remove tag or add missing way.";
    private static final int INVALID_TAGGED_NODE_INDEX = 2;
    private static final String INVALID_TUNNEL_INTERSECTION = "The railway (OSM ID: {0,number,#}) and highway "
            + "(OSM ID: {1,number,#}) tunnel intersection are on the same layer without a railway=level_crossing tag. "
            + "Suggest setting the tunnel's layer=-1.";
    private static final int INVALID_TUNNEL_INTERSECTION_INDEX = 3;
    private static final String INVALID_BRIDGE_INTERSECTION = "The railway (OSM ID: {0,number,#}) and highway "
            + "(OSM ID: {1,number,#}) bridge intersection are on the same layer without a railway=level_crossing tag. "
            + "Suggest setting the bridge's layer=1.";
    private static final int INVALID_BRIDGE_INTERSECTION_INDEX = 4;
    private static final String INVALID_INTERSECTION = "The intersection of railway (OSM ID: {0,number,#}) and "
            + "highway (OSM ID: {1,number,#}) is missing an intersection node. Add an appropriate intersection node.";
    private static final int INVALID_INTERSECTION_INDEX = 5;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INVALID_TAGGED_OBJECT,
            INVALID_TAGGED_INTERSECTION, INVALID_TAGGED_NODE, INVALID_TUNNEL_INTERSECTION,
            INVALID_BRIDGE_INTERSECTION, INVALID_INTERSECTION);
    private static final long serialVersionUID = -2063033332877849846L;

    /**
     * constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public LevelCrossingOnRailwayCheck(final Configuration configuration)
    {

        super(configuration);
        this.layerDefault = (Long) this.configurationValue(configuration, "layer.default",
                OSM_LAYER_DEFAULT);

    }

    /**
     * Object check looks for the vaild objects to check for level_crossing tag.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        /*-
         * The following objects should be checked:
         *  1) Any node.
         *  2) Any object that is tagged with railway=level_crossing.
         *  3) Any object that is tagged as a railway (rail, tram, disused, preserved).
         */
        return object instanceof Node
                || Validators.isOfType(object, RailwayTag.class, RailwayTag.LEVEL_CROSSING)
                || Validators.isOfType(object, RailwayTag.class, RailwayTag.RAIL, RailwayTag.TRAM,
                        RailwayTag.DISUSED, RailwayTag.PRESERVED);
    }

    /**
     * Create a Return Flags for level_crossing objects.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that contains flagged issue details
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        /*-
         * The following invalid situations are to be flagged:
         *  1) object is node and
         *     a) is marked as a level crossing but does in not an intersection of highway and railway
         *     b) is not tagged as a level crossing and is an intersection of highway and railway.
         *  2) object is not a node or point and is tagged with railway=level_crossing.
         *  3) object is railway and intersects a highway on the same layer but there is no node.
         */

        final Optional<CheckFlag> flagIncorrectlyTagged = this.flagIncorrectlyTagged(object);
        if (!flagIncorrectlyTagged.isEmpty())
        {
            return flagIncorrectlyTagged;
        }
        final Optional<CheckFlag> flagNonNodeTagged = this.flagNonNodeTagged(object);
        if (!flagNonNodeTagged.isEmpty())
        {
            return flagNonNodeTagged;
        }
        final Optional<CheckFlag> flagInvalidIntersections = this.flagInvalidIntersections(object);
        if (!flagInvalidIntersections.isEmpty())
        {
            return flagInvalidIntersections;
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Flag nodes incorrectly tagged with level_crossing or missing level_crossing tag.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that contains flagged issue details
     */
    private Optional<CheckFlag> flagIncorrectlyTagged(final AtlasObject object)
    {
        if (object instanceof Node)
        {
            final Node node = (Node) object;
            final Atlas atlas = node.getAtlas();
            // Count railway connections to this node
            final List<AtlasItem> connectedRailways = Iterables
                    .asList(atlas.itemsContaining(node.getLocation())).stream()
                    .filter(item -> Validators.isOfType(item, RailwayTag.class, RailwayTag.RAIL,
                            RailwayTag.TRAM, RailwayTag.DISUSED, RailwayTag.PRESERVED))
                    .collect(Collectors.toList());
            // Count car navigable connections to this node
            final List<AtlasItem> connectedHighways = Iterables
                    .asList(atlas.itemsContaining(node.getLocation())).stream()
                    .filter(HighwayTag::isCarNavigableHighway).collect(Collectors.toList());
            final List<AtlasItem> connectedRailHighways = Iterables
                    .asList(atlas.itemsContaining(node.getLocation())).stream()
                    .filter(item -> HighwayTag.isCarNavigableHighway(item)
                            && Validators.isOfType(item, RailwayTag.class, RailwayTag.RAIL,
                                    RailwayTag.TRAM, RailwayTag.DISUSED, RailwayTag.PRESERVED))
                    .collect(Collectors.toList());
            // If some highways and railways are connected then it should be a level_crossing
            // but if all connected ways are highways AND railways then skip it.
            if (!connectedHighways.isEmpty() && !connectedRailways.isEmpty()
                    && (connectedHighways.size() > connectedRailHighways.size()
                            || connectedRailways.size() > connectedRailHighways.size())
                    && !Validators.isOfType(node, RailwayTag.class, RailwayTag.LEVEL_CROSSING))
            {
                // This is a railway/highway intersect node that is not tagged with
                // railway=level_crossing
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(
                        INVALID_TAGGED_INTERSECTION_INDEX, object.getOsmIdentifier())));
            }
            if ((connectedHighways.isEmpty() || connectedRailways.isEmpty())
                    && Validators.isOfType(node, RailwayTag.class, RailwayTag.LEVEL_CROSSING))
            {
                // This is a node that is tagged with railway=level_crossing and is not a
                // railway/highway intersection
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(
                        INVALID_TAGGED_NODE_INDEX, object.getOsmIdentifier())));
            }
        }
        return Optional.empty();
    }

    /**
     * Flag all railway/highway intersections that are missing an intersection node
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that contains flagged issue details
     */
    private Optional<CheckFlag> flagInvalidIntersections(final AtlasObject object)
    {
        if (object instanceof Line && Validators.isOfType(object, RailwayTag.class, RailwayTag.RAIL,
                RailwayTag.TRAM, RailwayTag.DISUSED, RailwayTag.PRESERVED))
        {
            final Line railway = (Line) object;
            final Atlas atlas = railway.getAtlas();
            final Optional<Long> railwayLayer = Validators.hasValuesFor(railway, LayerTag.class)
                    ? LayerTag.getTaggedValue(railway)
                    : Optional.of(this.layerDefault);

            final List<Edge> intersectingHighways = Iterables
                    .asList(atlas.edgesIntersecting(railway.bounds())).stream().filter(item ->
                    {
                        final Optional<Long> edgeLayer = Validators.hasValuesFor(item,
                                LayerTag.class) ? LayerTag.getTaggedValue(item)
                                        : Optional.of(this.layerDefault);
                        return Edge.isMainEdgeIdentifier(item.getIdentifier())
                                && HighwayTag.isCarNavigableHighway(item)
                                && edgeLayer.get().equals(railwayLayer.get());
                    }).collect(Collectors.toList());
            for (final Edge highway : intersectingHighways)
            {
                final Set<Location> locations = railway.asPolyLine()
                        .intersections(highway.asPolyLine());
                for (final Location location : locations)
                {
                    final Optional<CheckFlag> flagIntersection = this.flaggedIntersection(railway,
                            highway, location);
                    if (!flagIntersection.isEmpty())
                    {
                        return flagIntersection;
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Flag all objects that are not nodes or points that are tagged with railway=level_crossing
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that contains flagged issue details
     */
    private Optional<CheckFlag> flagNonNodeTagged(final AtlasObject object)
    {
        if (!(object instanceof Node || object instanceof Point)
                && Validators.isOfType(object, RailwayTag.class, RailwayTag.LEVEL_CROSSING))
        {
            return Optional.of(this.createFlag(object, this.getLocalizedInstruction(
                    INVALID_TAGGED_OBJECT_INDEX, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    /**
     * Flag an invalid intersection of a railway and highway at a specific location
     *
     * @param railway
     *            the Line that represents the railway for evaluation
     * @param highway
     *            the Edge that represents the highway for evaluation
     * @param location
     *            the Location to check for valid intersection.
     * @return an optional {@link CheckFlag} object that contains flagged issue details
     */
    private Optional<CheckFlag> flaggedIntersection(final Line railway, final Edge highway,
            final Location location)
    {
        if (!(railway.asPolyLine().contains(location) && highway.asPolyLine().contains(location))
                && Iterables.asList(railway.getAtlas().pointsAt(location)).isEmpty())
        {
            if (Validators.isOfType(railway, TunnelTag.class, TunnelTag.YES) != Validators
                    .isOfType(highway, TunnelTag.class, TunnelTag.YES))
            {
                return Optional.of(this.createFlag(railway,
                        this.getLocalizedInstruction(INVALID_TUNNEL_INTERSECTION_INDEX,
                                railway.getOsmIdentifier(), highway.getOsmIdentifier())));
            }
            if (Validators.isOfType(railway, BridgeTag.class, BridgeTag.YES) != Validators
                    .isOfType(highway, BridgeTag.class, BridgeTag.YES))
            {
                return Optional.of(this.createFlag(railway,
                        this.getLocalizedInstruction(INVALID_BRIDGE_INTERSECTION_INDEX,
                                railway.getOsmIdentifier(), highway.getOsmIdentifier())));
            }
            return Optional.of(this.createFlag(railway,
                    this.getLocalizedInstruction(INVALID_INTERSECTION_INDEX,
                            railway.getOsmIdentifier(), highway.getOsmIdentifier())));
        }
        return Optional.empty();
    }

}
