package org.openstreetmap.atlas.checks.validation.linear;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.MultiplePolyLineToPolygonsConverter;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.MilitaryTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags {@link Edge}s and {@link Line}s that include the highway tag and an access tag
 * with a value of no. It checks for locations where this breaks a road network and does not have
 * supporting tags. Supporting tags declare what is or is not included in {@code access=no}. For
 * example a supporting tag of {@code public_transport=yes} would mean only public transport
 * vehicles are allowed.
 *
 * @author bbreithaupt
 */

public class HighwayAccessTagCheck extends BaseCheck
{

    private static final String MINIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.RESIDENTIAL.toString();
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Make proper adjustments to the access tag of way {0,number,#}, and associated tag combinations.");

    private final HighwayTag minimumHighwayType;

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public HighwayAccessTagCheck(final Configuration configuration)
    {
        super(configuration);

        final String highwayType = (String) this.configurationValue(configuration,
                "minimum.highway.type", MINIMUM_HIGHWAY_TYPE_DEFAULT);
        this.minimumHighwayType = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
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
        return ((object instanceof Edge) || (object instanceof Line))
                && Edge.isMasterEdgeIdentifier(object.getIdentifier())
                && !this.isFlagged(object.getOsmIdentifier()) && AccessTag.isNo(object)
                && isMinimumHighway(object) && !isInMilitaryArea((LineItem) object);
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
        this.markAsFlagged(object.getOsmIdentifier());
        return Optional.of(this.createFlag(object,
                this.getLocalizedInstruction(0, object.getOsmIdentifier())));
    }

    /**
     * Checks if {@link LineItem} is inside an {@link Area} or {@link Relation} with tag
     * {@code landuse=MILITARY} or tag key {@code military}.
     *
     * @param object
     *            {@link LineItem} to check
     * @return {@code true} if input {@link LineItem} is in an {@link Area} or {@link Relation} with
     *         tag {@code landuse=MILITARY} or tag key {@code military}
     */
    private boolean isInMilitaryArea(final LineItem object)
    {
        for (final Area area : object.getAtlas()
                .areas(area -> area.getOsmTags().getOrDefault(LandUseTag.KEY, "na").toUpperCase()
                        .equals(LandUseTag.MILITARY.toString())
                        || area.getOsmTags().containsKey(MilitaryTag.KEY)))
        {
            final Polygon areaPolygon = area.asPolygon();
            if (object.intersects(areaPolygon)
                    || areaPolygon.fullyGeometricallyEncloses(object.asPolyLine()))
            {
                return true;
            }
        }
        for (final Relation relation : object.getAtlas()
                .relations(relation -> (relation.getOsmTags().getOrDefault(LandUseTag.KEY, "na")
                        .toUpperCase().equals(LandUseTag.MILITARY.toString())
                        || relation.getOsmTags().containsKey(MilitaryTag.KEY))
                        && relation.isMultiPolygon()))
        {
            try
            {
                final MultiPolygon relationPolygon = new RelationOrAreaToMultiPolygonConverter()
                        .convert(relation);
                if (object.intersects(relationPolygon)
                        || relationPolygon.fullyGeometricallyEncloses(object.asPolyLine()))
                {
                    return true;
                }
            }
            catch (final MultiplePolyLineToPolygonsConverter.OpenPolygonException e)
            {
                continue;
            }
        }
        return false;
    }

    /**
     * Checks if an {@link AtlasObject} is of an equal or greater priority than the minimum. The
     * minimum is supplied as a configuration parameter, the default is {@code "tertiary"}.
     *
     * @param object
     *            an {@link AtlasObject}
     * @return {@code true} if this object is >= the minimum
     */
    private boolean isMinimumHighway(final AtlasObject object)
    {
        final Optional<HighwayTag> result = HighwayTag.highwayTag(object);
        return result.isPresent()
                && result.get().isMoreImportantThanOrEqualTo(this.minimumHighwayType);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
