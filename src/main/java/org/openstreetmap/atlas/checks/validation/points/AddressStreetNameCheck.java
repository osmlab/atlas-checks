package org.openstreetmap.atlas.checks.validation.points;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This flags {@link Point}s where their addr:street tag value does not match any of the name tag
 * values of {@link Edge}s within a configurable search distance.
 *
 * @author bbreithaupt
 */
public class AddressStreetNameCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = 5401402333350044455L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "Address node {0,number,#} has an addr:street value that does not match the name of any roads within {1,number,#} meters.");
    private static final Double SEARCH_DISTANCE_DEFAULT = 100.0;

    // Distance to search for Edges around a Point
    private final Distance searchDistance;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public AddressStreetNameCheck(final Configuration configuration)
    {
        super(configuration);
        this.searchDistance = configurationValue(configuration, "bounds.size",
                SEARCH_DISTANCE_DEFAULT, Distance::meters);
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
        return object instanceof Point && Validators.hasValuesFor(object, AddressStreetTag.class);
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
        // Gather the values of all name tags of all edges that are within the search distance
        final Set<String> streetNameValues = Iterables
                .stream(object.getAtlas().edgesIntersecting(
                        ((Point) object).getLocation().boxAround(this.searchDistance),
                        Edge::isMainEdge))
                .flatMap(edge -> edge.getTags(tag -> tag.startsWith(NameTag.KEY)).values())
                .collectToSet();

        // Flag the object if there are edges within the search distance and the addr:street values
        // is not present in the set of Edge name tag values
        return !streetNameValues.isEmpty()
                && !streetNameValues.contains(object.tag(AddressStreetTag.KEY))
                        ? Optional
                                .of(this.createFlag(object,
                                        this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                                                this.searchDistance.asMeters())))
                        : Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
