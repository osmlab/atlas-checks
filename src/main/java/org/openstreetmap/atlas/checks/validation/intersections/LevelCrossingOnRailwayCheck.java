package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.ConstructionDateTag;
import org.openstreetmap.atlas.tags.ConstructionTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
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
public class LevelCrossingOnRailwayCheck extends BaseCheck<Long>
{
    /**
     * NodeCheck is used to indicate the output of the isValidLevelCrossingNode function.
     * NODE_IGNORE indicates that something about the node or ways connected should just skip this
     * node NODE_VALID indicates a valid level crossing at this node. NODE_NO_RAILWAY indicates an
     * invalid level crossing because no valid railway exists NODE_NO_HIGHWAY indicates an invalid
     * level crossing because no valid highway exists NODE_NO_LAYERS indicates that no highway and
     * railway intersect at the same layer
     */
    private enum NodeCheck
    {
        NODE_IGNORE,
        NODE_VALID,
        NODE_NO_RAILWAY,
        NODE_NO_HIGHWAY,
        NODE_NO_LAYERS;
    }

    private static final String RAILWAY_FILTER_DEFAULT = "railway->rail,tram,disused,preserved,miniature,light_rail,subway,narrow_gauge";
    private final TaggableFilter railwayFilter;
    private static final Long OSM_LAYER_DEFAULT = 0L;
    private final Long layerDefault;
    private static final String INVALID_TAGGED_OBJECT = "The object (OSM ID: {0,number,#}) has `railway=level_crossing` "
            + "but is not a node. To fix: Remove `railway=level_crossing` tag.";
    private static final int INVALID_TAGGED_OBJECT_INDEX = 0;
    private static final String NODE_MISSING_LC_TAG = "The intersection node (OSM ID: {0,number,#}) is "
            + "missing a `railway=level_crossing` tag. This means that there are at least one valid railway and one "
            + "car navigable highway on the same layer at this node. To fix: If the two ways should be on different "
            + "layers then adjust the layer tags for each way appropriately. If the two ways do intersect on the same "
            + "layer then add the `railway=level_crossing` tag to this node.";
    private static final int NODE_MISSING_LC_TAG_INDEX = 1;
    private static final String NODE_INVALID_LC_TAG_NO_HIGHWAY = "The node (OSM ID: {0,number,#}) has "
            + "`railway=level_crossing` tag, but there is no car navigable highway at this intersection. "
            + "To fix: Remove railway=level_crossing tag.";
    private static final int NODE_INVALID_LC_TAG_NO_HIGHWAY_INDEX = 2;
    private static final String NODE_INVALID_LC_TAG_NO_RAILWAY = "The node (OSM ID: {0,number,#}) has "
            + "`railway=level_crossing` tag, but there are no existing rails at this intersection. "
            + "To fix: Remove railway=level_crossing tag.";
    private static final int NODE_INVALID_LC_TAG_NO_RAILWAY_INDEX = 3;
    private static final String NODE_INVALID_LC_TAG_LAYERS = "The node (OSM ID: {0,number,#}) has `railway=level_crossing` "
            + "tag, but there are no railway and highway intersection on the same layer. "
            + "To fix: If the railway and highway should be on the same layer then update the layer tags for both ways "
            + "to be equal. If the ways are on different layers then remove railway=level_crossing tag.";
    private static final int NODE_INVALID_LC_TAG_LAYERS_INDEX = 4;
    private static final String INTERSECTION_MISSING_NODE = "The railway (OSM ID: {0,number,#}) has one or more car "
            + "navigable intersections on the same layer that are missing intersection nodes. To fix: "
            + "If highway and railway do cross at the same layer then add appropriate intersection node(s) with "
            + "`railway=level_crossing` tag. If highway and railway are on different layers then update the "
            + "appropriate layer tag for the way that goes under or over the other way.";
    private static final int INTERSECTION_MISSING_NODE_INDEX = 5;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INVALID_TAGGED_OBJECT,
            NODE_MISSING_LC_TAG, NODE_INVALID_LC_TAG_NO_HIGHWAY, NODE_INVALID_LC_TAG_NO_RAILWAY,
            NODE_INVALID_LC_TAG_LAYERS, INTERSECTION_MISSING_NODE);
    private static final List<String> CONSTRUCTION_TAGS = List.of(HighwayTag.KEY, RailwayTag.KEY);
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
        this.layerDefault = this.configurationValue(configuration, "layer.default",
                OSM_LAYER_DEFAULT);
        this.railwayFilter = this.configurationValue(configuration, "railway.filter",
                RAILWAY_FILTER_DEFAULT, TaggableFilter::forDefinition);
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
         *  3) Any object that is tagged as a railway as indicted in railway.filter.
         */
        return object instanceof Node
                || Validators.isOfType(object, RailwayTag.class, RailwayTag.LEVEL_CROSSING)
                || this.railwayFilter.test(object);
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

            final NodeCheck nodeCheck = this.isValidLevelCrossingNode(node);
            if (Validators.isOfType(node, RailwayTag.class, RailwayTag.LEVEL_CROSSING)
                    && nodeCheck != NodeCheck.NODE_VALID && nodeCheck != NodeCheck.NODE_IGNORE)
            {
                // This is a node that is tagged with railway=level_crossing and is not a
                // railway/highway intersection
                final int instructIndex;
                switch (nodeCheck)
                {
                    case NODE_NO_RAILWAY:
                        instructIndex = NODE_INVALID_LC_TAG_NO_RAILWAY_INDEX;
                        break;
                    case NODE_NO_HIGHWAY:
                        instructIndex = NODE_INVALID_LC_TAG_NO_HIGHWAY_INDEX;
                        break;
                    default:
                        instructIndex = NODE_INVALID_LC_TAG_LAYERS_INDEX;
                        break;
                }
                return Optional.of(this
                        .createFlag(object,
                                this.getLocalizedInstruction(instructIndex,
                                        object.getOsmIdentifier()))
                        .addFixSuggestion(FeatureChange.add(
                                (AtlasEntity) ((CompleteEntity) CompleteEntity
                                        .from((AtlasEntity) object)).withRemovedTag(RailwayTag.KEY),
                                object.getAtlas())));
            }
            if (!Validators.isOfType(node, RailwayTag.class, RailwayTag.LEVEL_CROSSING)
                    && nodeCheck == NodeCheck.NODE_VALID)
            {
                // This is a valid railway/highway intersect node that is not tagged with
                // railway=level_crossing
                return Optional.of(this
                        .createFlag(object,
                                this.getLocalizedInstruction(NODE_MISSING_LC_TAG_INDEX,
                                        object.getOsmIdentifier()))
                        .addFixSuggestion(FeatureChange.add(
                                (AtlasEntity) ((CompleteEntity) CompleteEntity
                                        .from((AtlasEntity) object)).withAddedTag(RailwayTag.KEY,
                                                RailwayTag.LEVEL_CROSSING.toString().toLowerCase()),
                                object.getAtlas())));

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
        if (object instanceof Line && this.railwayFilter.test(object))
        {
            final Line railway = (Line) object;
            final Atlas atlas = railway.getAtlas();
            final List<Location> badIntersectingHighways = new ArrayList<>();

            Iterables.asList(atlas.edgesIntersecting(railway.bounds()))
                    .forEach(highway -> badIntersectingHighways
                            .addAll(this.missingNodesAtIntersectionOnSameLayer(railway, highway)));
            if (!badIntersectingHighways.isEmpty())
            {
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(INTERSECTION_MISSING_NODE_INDEX,
                                railway.getOsmIdentifier()),
                        badIntersectingHighways));
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
        if (!(object instanceof LocationItem)
                && Validators.isOfType(object, RailwayTag.class, RailwayTag.LEVEL_CROSSING))
        {
            return Optional.of(this
                    .createFlag(object,
                            this.getLocalizedInstruction(INVALID_TAGGED_OBJECT_INDEX,
                                    object.getOsmIdentifier()))
                    .addFixSuggestion(FeatureChange.add(
                            (AtlasEntity) ((CompleteEntity) CompleteEntity
                                    .from((AtlasEntity) object)).withRemovedTag(RailwayTag.KEY),
                            object.getAtlas())));
        }
        return Optional.empty();
    }

    /**
     * Checks if the tags of an object indicate a way that is invalid. Invalid ways for this check
     * are under construction or have both rail and highway tags.
     *
     * @param object
     *            Object to check
     * @return true if the object is under construction, otherwise false
     */
    private boolean ignoreWay(final AtlasObject object)
    {
        return object.getTags().keySet().stream()
                .anyMatch(tag -> tag.equals(ConstructionTag.KEY)
                        || tag.startsWith("construction:") && !tag.equals(ConstructionDateTag.KEY))
                || CONSTRUCTION_TAGS.stream()
                        .anyMatch(tag -> ConstructionTag.KEY.equals(object.getTags().get(tag)))
                || (HighwayTag.highwayTag(object).isPresent() && RailwayTag.isRailway(object))
                || Validators.isOfType(object, RailwayTag.class, RailwayTag.PROPOSED);
    }

    /**
     * Indicate if a node is a valid level_crossing intersection.
     *
     * @param node
     *            A node to check for all intersecting ways to see if a railway and highway
     *            intersect on the same layer
     * @return an int that indicates an invalid intersection, valid intersection, or failure. 0 -
     *         indicates the node is a valid level crossing. Positive values indicate invalid level
     *         crossing. Negative values indicates that an intersecting way is under construction
     */
    private NodeCheck isValidLevelCrossingNode(final Node node)
    {
        final Atlas atlas = node.getAtlas();

        // check for any ways at this node to ignore.
        if (Iterables.asList(atlas.itemsContaining(node.getLocation())).stream()
                .anyMatch(this::ignoreWay))
        {
            return NodeCheck.NODE_IGNORE;
        }
        // Get railway connections to this node
        final List<AtlasItem> connectedRailways = Iterables
                .asList(atlas.itemsContaining(node.getLocation())).stream()
                .filter(this.railwayFilter::test).collect(Collectors.toList());
        if (connectedRailways.isEmpty())
        {
            // Node has no railways through it
            return NodeCheck.NODE_NO_RAILWAY;
        }
        // Get car navigable connections to this node
        final List<AtlasItem> connectedHighways = Iterables
                .asList(atlas.itemsContaining(node.getLocation())).stream()
                .filter(HighwayTag::isCarNavigableHighway).collect(Collectors.toList());
        if (connectedHighways.isEmpty())
        {
            // Node has no highways through it
            return NodeCheck.NODE_NO_HIGHWAY;
        }

        // For each railway, check that there is a highway on the same layer that
        // is not the same way as the railway.
        for (final AtlasObject railway : connectedRailways)
        {
            final Long railwayLayer = LayerTag.getTaggedOrImpliedValue(railway, this.layerDefault);
            for (final AtlasObject highway : connectedHighways)
            {
                final Long highwayLayer = LayerTag.getTaggedOrImpliedValue(highway,
                        this.layerDefault);
                if (railwayLayer.equals(highwayLayer)
                        && railway.getOsmIdentifier() != highway.getOsmIdentifier())
                {
                    return NodeCheck.NODE_VALID;
                }
            }

        }
        return NodeCheck.NODE_NO_LAYERS;
    }

    /**
     * Flag an invalid intersection of a railway and highway at a specific location
     *
     * @param railway
     *            the Line that represents the railway for evaluation
     * @param highway
     *            the Edge that represents the highway for evaluation
     * @return an optional {@link CheckFlag} object that contains flagged issue details
     */
    private List<Location> missingNodesAtIntersectionOnSameLayer(final Line railway,
            final Edge highway)
    {
        final Long railwayLayer = LayerTag.getTaggedOrImpliedValue(railway, this.layerDefault);
        final Long highwayLayer = LayerTag.getTaggedOrImpliedValue(highway, this.layerDefault);

        if (Edge.isMainEdgeIdentifier(highway.getIdentifier())
                && HighwayTag.isCarNavigableHighway(highway) && !this.ignoreWay(railway)
                && !this.ignoreWay(highway) && railwayLayer.equals(highwayLayer))
        {
            return railway.asPolyLine().intersections(highway.asPolyLine()).stream()
                    .filter(location -> !(railway.asPolyLine().contains(location))
                            || !(highway.asPolyLine().contains(location)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
