package org.openstreetmap.atlas.checks.validation.points;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check is to detect a Node with building=< * > tag that doesn't enclose in Building Footprint and suggest drawing a building footprint (enclosed way or relation) then delete this Node.
 *
 * @author Vladimir Lemberg
 */
public class LoneBuildingNodeCheck extends BaseCheck<Long>
{
    private static final String LONE_INSTRUCTION = "Node {0,number,#} has been tagged as a building indicating that it represents a building, but it is better to represent buildings with polygons or multipolygons. Please see if there is enough satellite imagery information to replace this node with a new polygon. If there is enough detail to draw this building as a polygon then add the polygon that represents the building and remove the building tag from this node or transfer the tags from this node to the new polygon, and then delete this node. See https://wiki.openstreetmap.org/wiki/Mapping_addresses_as_separate_nodes_or_by_adding_to_building_polygons.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(LONE_INSTRUCTION);
    private static final Double BUILDING_SEARCH_DISTANCE_DEFAULT = 10.0;
    private final Distance searchDistance;

    public LoneBuildingNodeCheck(final Configuration configuration)
    {
        super(configuration);
        this.searchDistance = configurationValue(configuration, "building.search.distance",
                BUILDING_SEARCH_DISTANCE_DEFAULT, Distance::meters);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Point && object.getTag(BuildingTag.KEY).isPresent();
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Point point = (Point) object;
        final Set<AtlasEntity> buildingsAround = Iterables
                .stream(point.getAtlas()
                        .entitiesIntersecting(point.getLocation().boxAround(this.searchDistance)))
                .filter(this::validBuildingFilter).collectToSet();

        boolean enclosed = false;

        if (!buildingsAround.isEmpty())
        {
            for (final AtlasEntity building : buildingsAround)
            {
                if (building instanceof Area || building instanceof Relation)
                {
                    if (building.bounds().fullyGeometricallyEncloses(point.getLocation()))
                    {
                        enclosed = true;
                        break;
                    }
                }
            }
        }

        return enclosed ? Optional.empty()
                : Optional.of(this.createFlag(point,
                        this.getLocalizedInstruction(0, point.getOsmIdentifier())));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Helper function for filtering {@link AtlasObject}s. This is detecting that
     * {@link AtlasObject} has building tag
     *
     * @param object
     *            AtlasObject to examine
     * @return true if {@link AtlasObject} is passed validation.
     */
    private boolean validBuildingFilter(final AtlasObject object)
    {
        return object.getTag(BuildingTag.KEY).isPresent();
    }
}
