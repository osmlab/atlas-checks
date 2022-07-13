package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.AddressHousenumberTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check is to detect Point with building=yes tag.
 *
 * @author Vladimir Lemberg
 */
public class EnclosedBuildingNodeCheck extends BaseCheck<Long>
{
    private static final String ENCLOSED_INSTRUCTION = "Node {0,number,#} has been tagged as a building, but it does not appear to be a building. Please either merge the tags associated with this node to the building that surrounds this node and delete this node or replace the building=< * > tag and replace it with the correct tag that better describes what the node represents.";
    private static final String ENCLOSED_ADDRESS_INSTRUCTION = "Node {0,number,#} has been tagged as a building but it appears to be an address. Please remove the building=< * > tag.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(ENCLOSED_ADDRESS_INSTRUCTION, ENCLOSED_INSTRUCTION);
    private static final Double BUILDING_SEARCH_DISTANCE_DEFAULT = 10.0;
    private final Distance searchDistance;

    public EnclosedBuildingNodeCheck(final Configuration configuration)
    {
        super(configuration);
        this.searchDistance = configurationValue(configuration, "sidewalk.search.distance",
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

        final Set<Area> buildingsAround = Iterables
                .stream(point.getAtlas()
                        .areasIntersecting(point.getLocation().boxAround(this.searchDistance)))
                .filter(this::validBuildingFilter).collectToSet();

        if (!buildingsAround.isEmpty())
        {
            for (final Area building : buildingsAround)
            {
                if (building.asPolygon().fullyGeometricallyEncloses(point.getLocation()))
                {
                    return (point.getTag(AddressHousenumberTag.KEY).isPresent())
                            ? Optional.of(this.createFlag(point,
                                    this.getLocalizedInstruction(0, point.getOsmIdentifier())))
                            : Optional.of(this.createFlag(point,
                                    this.getLocalizedInstruction(1, point.getOsmIdentifier())));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Helper function for filtering {@link Edge}s. This is detecting that {@link Edge} is
     * separately mapped sidewalk https://wiki.openstreetmap.org/wiki/Sidewalks or designated
     * pedestrian road https://wiki.openstreetmap.org/wiki/Key:foot.
     *
     * @param area
     *            Edge to examine
     * @return true if {@link Edge} is passed validation.
     */
    private boolean validBuildingFilter(final Area area)
    {
        return area.getTag(BuildingTag.KEY).isPresent();
    }
}
