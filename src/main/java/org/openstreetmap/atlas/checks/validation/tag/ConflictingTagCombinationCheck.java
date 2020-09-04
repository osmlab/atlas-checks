package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check verifies whether an {@link AtlasObject} has a conflicting tag combination or not. Even
 * though people are free to add/remove/update tags on OSM. They may end up creating invalid
 * combinations. Some of the combinations below have official documentation, some don't.
 * Theoretically we can add as many checks as we want, but that has a potential to degrade
 * performance, and we might not get combinations after all.
 *
 * @author mkalender
 * @author danielbaah
 * @author bbreithaupt
 */
public class ConflictingTagCombinationCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = -4239136867519896104L;

    private static final String INVALID_COMBINATION_INSTRUCTION = "OSM feature {0,number,#} has invalid tag combinations in 2 or more of the following tags: {1}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(INVALID_COMBINATION_INSTRUCTION);

    private static final List<String> CONFLICTING_TAG_FILTERS_DEFAULT = Arrays.asList("highway->*"
            // highway tag should not appear together with building tag
            // Highways identify roads, whereas buildings identify buildings. They should not appear
            // together. highway=elevator is commonly combined with building, but
            // documentation (http://wiki.openstreetmap.org/wiki/Tag:highway%3Delevator) does not
            // include
            // building as a possible combination.
            + "&building->*"
            // highway tag should not appear together with route=ferry tag
            // ferry routes cannot be a road
            + "||route->ferry"
            // Let's make sure highway tag does appear together with natural tag
            // Highways generally man-made whereas natural describes geological features.
            // Track with a grass could be an exception, but that one is explained
            // http://wiki.openstreetmap.org/wiki/How_to_map_landuse#How_should_a_track_with_a_grass_surface_be_mapped.3F
            + "||natural->*"
            // Land use tags are used to describe human use of land. In OSM, the land use tag should
            // only
            // be applied to Area's and Points, therefore should not appear together with Highway
            // tags.
            + "||landuse->*"
            // Place tags are used to identify locations by name, particularly villages and
            // communities.
            // They should not exist with Highway tags.
            + "||place->*",
            // Service tag check and instruction
            // service tag requires highway=service or railway=* or waterway=canal
            // ref: http://wiki.openstreetmap.org/wiki/Key:service
            "service->*&highway->!service&highway->!construction&railway->!&waterway->!canal");

    private final List<TaggableFilter> conflictingTagFilters;
    private final List<String> filterKeys;

    /**
     * Gathers the keys from a {@link TaggableFilter} using regex.
     *
     * @param filter
     *            a {@link TaggableFilter}
     * @return the tag keys as a {@link Set} of {@link String}s
     */
    private static Set<String> getFilterKeys(final TaggableFilter filter)
    {
        return Arrays.stream(filter.toString().split("[|&]+")).map(tag -> tag.split("->")[0])
                .collect(Collectors.toSet());
    }

    public ConflictingTagCombinationCheck(final Configuration configuration)
    {
        super(configuration);
        final List<String> filterStrings = configurationValue(configuration,
                "tags.conflicting.filters", CONFLICTING_TAG_FILTERS_DEFAULT);
        this.conflictingTagFilters = filterStrings.stream().map(TaggableFilter::forDefinition)
                .collect(Collectors.toList());
        this.filterKeys = this.conflictingTagFilters.stream()
                .map(filter -> String.join(", ", getFilterKeys(filter)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return ((object instanceof Edge && HighwayTag.isCarNavigableHighway(object)
                && ((Edge) object).isMainEdge()) || (object instanceof Line))
                && !this.isFlagged(this.getUniqueOSMIdentifier(object));
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Set<String> instructions = new HashSet<>();
        for (int filterIndex = 0; filterIndex < this.conflictingTagFilters.size(); filterIndex++)
        {
            if (this.conflictingTagFilters.get(filterIndex).test(object))
            {
                this.markAsFlagged(this.getUniqueOSMIdentifier(object));
                instructions.add(this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                        this.filterKeys.get(filterIndex)));
            }
        }
        if (object instanceof Edge)
        {
            return instructions.isEmpty() ? Optional.empty()
                    : Optional.of(this.createFlag(new OsmWayWalker((Edge) object).collectEdges(),
                            String.join(" ", instructions)));
        }
        return instructions.isEmpty() ? Optional.empty()
                : Optional.of(this.createFlag(object, String.join(" ", instructions)));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
