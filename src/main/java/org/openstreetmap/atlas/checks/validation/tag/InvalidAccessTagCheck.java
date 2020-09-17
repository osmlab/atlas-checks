package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.geography.converters.MultiplePolyLineToPolygonsConverter;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.MilitaryTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags {@link Edge}s and {@link Line}s that include an access tag with a value of no,
 * and does not have any supporting tags. Supporting tags declare what is or is not included in
 * {@code access=no}. For example a supporting tag of {@code public_transport=yes} would mean only
 * public transport vehicles are allowed. Items with supporting tags are filtered out through the
 * use of the {@code tags.filter} configurable.
 *
 * @author bbreithaupt
 */

public class InvalidAccessTagCheck extends BaseCheck<Long>
{

    private static final String MINIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.RESIDENTIAL.toString();
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "This way {0,number,#} has an invalid access tag value, resulting from improper tag combinations. Investigate ground truth and properly correct them.");
    private static final long serialVersionUID = 5197703822744690835L;
    private final HighwayTag minimumHighwayType;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidAccessTagCheck(final Configuration configuration)
    {
        super(configuration);

        final String highwayType = (String) this.configurationValue(configuration,
                "minimum.highway.type", MINIMUM_HIGHWAY_TYPE_DEFAULT);
        this.minimumHighwayType = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check. Objects
     * passed to this function have already been filtered by the tags.filter parameter in the
     * configuration file.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return AccessTag.isNo(object) && ((object instanceof Edge) || (object instanceof Line))
                && Edge.isMainEdgeIdentifier(object.getIdentifier())
                && !this.isFlagged(object.getOsmIdentifier()) && isMinimumHighway(object);
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
        if (!isInMilitaryArea((LineItem) object))
        {
            this.markAsFlagged(object.getOsmIdentifier());

            // If this is an Edge, grab all edges from the original way.
            final String instruction = this.getLocalizedInstruction(0, object.getOsmIdentifier());
            if (object instanceof Edge)
            {
                return Optional.of(this.createFlag(new OsmWayWalker((Edge) object).collectEdges(),
                        instruction));
            }
            return Optional.of(this.createFlag(object, instruction));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
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
        final PolyLine objectAsPolyLine = object.asPolyLine();
        if (object.getAtlas().areasIntersecting(object.bounds(),
                area -> (Validators.isOfType(area, LandUseTag.class, LandUseTag.MILITARY)
                        || Validators.hasValuesFor(area, MilitaryTag.class))
                        && (object.intersects(area.asPolygon())
                                || area.asPolygon().fullyGeometricallyEncloses(objectAsPolyLine)))
                .iterator().hasNext())
        {
            return true;
        }
        for (final Relation relation : object.getAtlas().relationsWithEntitiesIntersecting(
                object.bounds(),
                relation -> (Validators.isOfType(relation, LandUseTag.class, LandUseTag.MILITARY)
                        || Validators.hasValuesFor(relation, MilitaryTag.class))
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
}
