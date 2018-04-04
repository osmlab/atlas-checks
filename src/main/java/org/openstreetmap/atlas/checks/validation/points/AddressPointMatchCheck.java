package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.common.base.Strings;

/**
 * This check identifies Point objects in OSM that have a specified street number (addr:housenumber)
 * but no specified street name (addr:street) and are not part of an associated street Relation. No
 * specified street name refers to either having a null value for the street name key, or no street
 * name key present at all.
 *
 * @author savannahostrowski
 */

public class AddressPointMatchCheck extends BaseCheck
{
    private static final long serialVersionUID = -756695185133616997L;
    public static final String NO_STREET_NAME_POINT_INSTRUCTIONS = "This Node, {0,number,#}, has "
            + "no street name specified in the address. The street name should likely "
            + "be one of {1}. These names were derived from nearby Nodes.";
    public static final String NO_STREET_NAME_EDGE_INSTRUCTIONS = "This Node, {0,number,#}, has "
            + "no street name specified in the address. The street name should likely "
            + "be one of {1}. These names were derived from nearby Ways.";
    public static final String NO_SUGGESTED_NAMES_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. No suggestions names were found as there were no "
            + "nearby Nodes or Ways with street name key tags.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            NO_STREET_NAME_POINT_INSTRUCTIONS, NO_STREET_NAME_EDGE_INSTRUCTIONS,
            NO_SUGGESTED_NAMES_INSTRUCTIONS);
    private static final String STREET_RELATION_ROLE = "street";
    private static final String ASSOCIATED_STREET_RELATION = "associatedStreet";
    private static final double BOUNDS_SIZE_DEFAULT = 75.0;

    private final Distance boundsSize;

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public AddressPointMatchCheck(final Configuration configuration)
    {
        super(configuration);
        this.boundsSize = Distance.meters(
                (Double) configurationValue(configuration, "bounds.size", BOUNDS_SIZE_DEFAULT));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Object is an instance of Point
        return object instanceof Point
                // And does not have an Associated Street Relation
                && !hasAssociatedStreetRelation(object)
                // And either doesn't have the addr:street tag, has the tag but has a null value,
                // or has the tag but has no value
                && Strings.isNullOrEmpty(object.tag(AddressStreetTag.KEY));
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
        final Point point = (Point) object;

        // Get a bounding box around the Point of interest
        final Rectangle box = point.getLocation().boxAround(boundsSize);

        // Get all Points in the bounding box, remove Points that have null as their
        // street name or do not have the street name key tag, and get a set of candidate street
        // names
        final Set<String> points = Iterables.stream(point.getAtlas().pointsWithin(box))
                .map(nearbyPoint -> nearbyPoint.tag(AddressStreetTag.KEY)).filter(Objects::nonNull)
                .collectToSet();

        // Get all Edges intersecting the bounding box, remove Edges that have null as their
        // street name or do not have the street name key tag, and get a set of candidate street
        // names
        final Set<String> edges = Iterables.stream(point.getAtlas().edgesIntersecting(box))
                .map(nearbyEdge -> nearbyEdge.tag(NameTag.KEY)).filter(Objects::nonNull)
                .collectToSet();

        // If there are no Points or Edges in the bounding box
        if (points.isEmpty() && edges.isEmpty())
        {
            // Flag Point with instruction indicating that there are are no suggestions
            return Optional.of(this.createFlag(point,
                    this.getLocalizedInstruction(2, point.getOsmIdentifier())));
        }
        // If there are Points in the bounding box
        else if (!points.isEmpty())
        {
            // Add all interior Point street names to the list of candidate street names
            return Optional.of(this.createFlag(point,
                    this.getLocalizedInstruction(0, point.getOsmIdentifier(), points)));
        }
        // If there are Edges intersecting or contained by the bounding box
        else
        {
            return Optional.of(this.createFlag(point,
                    this.getLocalizedInstruction(1, point.getOsmIdentifier(), edges)));
        }
    }

    private boolean hasAssociatedStreetRelation(final AtlasObject object)
    {
        final Point point = (Point) object;

        return point.relations().stream().filter(
                relation -> relation.tag(RelationTypeTag.KEY).equals(ASSOCIATED_STREET_RELATION))
                .anyMatch(relation -> relation.members().stream()
                        .anyMatch(member -> member.getRole().equals(STREET_RELATION_ROLE)
                                && member.getEntity().getType().equals(ItemType.EDGE)));
    }
}
