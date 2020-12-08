package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.MultiplePolyLineToPolygonsConverter;
import org.openstreetmap.atlas.tags.AdministrativeLevelTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * OpenBoundaryCheck This check flags boundaries that should be closed polygons but are not.
 *
 * @author v-garei
 */
public class OpenBoundaryCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 6655863145391887741L;
    private static final String OPEN_BOUNDARY_INSTRUCTIONS = "The Multipolygon relation {0,number,#} with members : {1} is not closed at some locations : {2}";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(OPEN_BOUNDARY_INSTRUCTIONS);
    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();

    /**
     * @param configuration
     *            the JSON configuration for this check
     */
    public OpenBoundaryCheck(final Configuration configuration)
    {
        super(configuration);
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
        final Map<String, String> osmKeys = object.getOsmTags();
        return object instanceof Relation && !isFlagged(object.getOsmIdentifier())
                && Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY)
                && !SyntheticRelationMemberAdded.hasAddedRelationMember(object)
                && this.hasAdminLevelTag(osmKeys);
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
        markAsFlagged(object.getOsmIdentifier());
        final Relation relation = (Relation) object;

        if (this.hasAllMembersLoaded(relation))
        {
            try
            {
                RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(relation);
                return Optional.empty();
            }
            catch (final MultiplePolyLineToPolygonsConverter.OpenPolygonException exception)
            {
                final List<Location> openLocations = exception.getOpenLocations();
                final Set<String> latlonSet = new HashSet<>();

                for (final Location openLocation : openLocations)
                {
                    // create function to get lat lon - pretty it up
                    final String lat = openLocation.getLatitude().toString();
                    final String lon = openLocation.getLongitude().toString();
                    final String latlon = lat + "," + lon;
                    latlonSet.add(latlon);

                }

                final Set<Long> memberIds = relation.members().stream()
                        .map(member -> member.getEntity().getOsmIdentifier())
                        .collect(Collectors.toSet());

                if (!openLocations.isEmpty())
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                            relation.getOsmIdentifier(), memberIds, latlonSet)));

                }
            }
            catch (final Exception exception)
            {
                return Optional.empty();
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
     * Check if the tags contain admin_level OSM tag
     * 
     * @param tags
     *            osm tags
     * @return boolean if tags contain admin_level tag
     */
    private boolean hasAdminLevelTag(final Map<String, String> tags)
    {
        return tags.containsKey(AdministrativeLevelTag.KEY);
    }

    /**
     * Checks to make sure relation is fully loaded by checking if the all known members count is
     * the same as the amount of members the relation in question has
     * 
     * @param relation
     *            relational boundary in question
     * @return true if relation is fully loaded from shards.
     */
    private boolean hasAllMembersLoaded(final Relation relation)
    {
        return relation.members().size() == relation.allKnownOsmMembers().size();
    }
}
