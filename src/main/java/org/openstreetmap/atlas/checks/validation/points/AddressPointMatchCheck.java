package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.directory.api.util.Strings;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.*;
import org.openstreetmap.atlas.tags.AddressHousenumberTag;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * This check identifies Point objects in OSM that have a specified street number (addr:housenumber)
 but no specified street name (addr:street). No specified street name refers to either having a null
 value for the street name key, or no street name key present at all.
 *
 * @author savannahostrowski
 */

public class AddressPointMatchCheck extends BaseCheck
{
    private static final long serialVersionUID = 1L;
    public static final String NO_STREET_NAME_POINT_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. The street name should likely "
            + "be one of {1}. These names were derived from nearby nodes.";
    public static final String NO_STREET_NAME_EDGE_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. The street name should likely "
            + "be one of {1}. These names were derived from nearby ways.";
    public static final String NO_SUGGESTED_NAMES_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. No suggestions names were found as there were no"
            + "nearby nodes or ways with street name key tags.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            NO_STREET_NAME_POINT_INSTRUCTIONS, NO_STREET_NAME_EDGE_INSTRUCTIONS,
            NO_SUGGESTED_NAMES_INSTRUCTIONS);
    private static final String ADDRESS_STREET_NUMBER_KEY = AddressHousenumberTag.KEY;
    private static final String POINT_STREET_NAME_KEY = AddressStreetTag.KEY;
    private static final String EDGE_STREET_NAME_KEY = NameTag.KEY;
    private static final String STREET_RELATION_ROLE = "street";
    private static final double BOUNDS_SIZE_DEFAULT = 150.0;

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
                && !hasAssociatedStreetRelation(object)
                // And has a street number specified
                && object.getTag(ADDRESS_STREET_NUMBER_KEY).isPresent()
                // And if the street name key has a value of null or if the street name key is not present
                && Strings.isEmpty(object.tag(POINT_STREET_NAME_KEY));
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
        // street name or do not have the street name key tag, and get a set of candidate street names
        final Set<String> points = Iterables.stream(point.getAtlas().pointsWithin(box))
                .filter(nearbyPoint -> nearbyPoint.getTag(POINT_STREET_NAME_KEY).isPresent())
                .map(nearbyPoint -> nearbyPoint.tag(POINT_STREET_NAME_KEY)).collectToSet();

        // Get all Edges intersecting the bounding box, remove Edges that have null as their
        // street name or do not have the street name key tag, and get a set of candidate street names
        final Set<String> edges = Iterables.stream(point.getAtlas().edgesIntersecting(box))
                .filter(nearbyEdge -> nearbyEdge.getTag(EDGE_STREET_NAME_KEY).isPresent())
                .map(nearbyEdge -> nearbyEdge.tag(EDGE_STREET_NAME_KEY)).collectToSet();

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

    protected boolean hasAssociatedStreetRelation(AtlasObject object) {
        // Initialize the rolePresent flag to false as we have not yet determined whether it is
        // Part of the AssociatedStreet relation
        boolean rolePresent = false;
        Point point = (Point) object;

        // Get all relations that the Point is associated with, filter to keep only relations
        // Where type=associatedStreet
        Set<Relation> relations = point.relations().stream()
                .filter(relation -> relation.getTag(RelationTypeTag.KEY).equals(RelationTypeTag.ASSOCIATEDSTREET))
                .collect(Collectors.toSet());
        // For each relation found in the Set of Relations
        for (Relation relation: relations) {
            // Get all members of that Relation, filter to keep only members where role:street
            // And where the member is an Edge. Apply ! as having a non-empty Set here would
            // indicate that the Relation with its correct member is present
            rolePresent = !relation.members().stream()
                    .filter(member -> member.getRole().equals(STREET_RELATION_ROLE)
                            && member.getEntity().getType().equals(ItemType.EDGE))
                    .collect(Collectors.toSet()).isEmpty();

        }
        return rolePresent;

    }
}
