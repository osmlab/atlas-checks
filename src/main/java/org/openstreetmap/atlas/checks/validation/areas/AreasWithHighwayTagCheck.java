package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flag the {@link Area}s that have a highway tag
 *
 * @author matthieun
 * @author cuthbertm
 * @author bbreithaupt
 */
public class AreasWithHighwayTagCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The way with OSM ID {0,number,#} has a highway value of {1}, which should not have an area=yes tag. Consider changing this to highway={2}.",
            "The way with OSM ID {0,number,#} has a highway value of {1}, which should not have an area=yes tag.");
    private static final long serialVersionUID = 3638306611072651348L;
    private static final EnumSet<HighwayTag> VALID_HIGHWAY_TAGS = EnumSet.of(HighwayTag.SERVICES,
            HighwayTag.SERVICE, HighwayTag.REST_AREA, HighwayTag.PEDESTRIAN, HighwayTag.PLATFORM);

    /**
     * An object is not allowed to have a highway tag that is not in VALID_HIGHWAY_TAGS if it also
     * has an area=yes tag
     *
     * @param object
     *            the object in question
     * @param tag
     *            the object's highway tag
     * @return true if the object has an invalid highway tag and an area=yes tag
     */
    static boolean isUnacceptableAreaHighwayTagCombination(final AtlasObject object,
            final HighwayTag tag)
    {
        return !VALID_HIGHWAY_TAGS.contains(tag)
                && Validators.isOfType(object, AreaTag.class, AreaTag.YES);
    }

    public AreasWithHighwayTagCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Area || (object instanceof Edge && ((Edge) object).isMainEdge()))
                && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        return HighwayTag.highwayTag(object)
                // If the tag isn't one of the VALID_HIGHWAY_TAGS, we want to flag it.
                .filter(tag -> isUnacceptableAreaHighwayTagCombination(object, tag)).map(tag ->
                {
                    this.markAsFlagged(object.getOsmIdentifier());

                    if (tag.equals(HighwayTag.FOOTWAY))
                    {
                        final Set<AtlasObject> objectsToFlag = this.getObjectsToFlag(object);
                        return this
                                .createFlag(objectsToFlag,
                                        this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                                                tag, HighwayTag.PEDESTRIAN))
                                .addFixSuggestions(objectsToFlag.stream()
                                        .map(toFlag -> FeatureChange.add(
                                                (AtlasEntity) ((CompleteEntity) CompleteEntity
                                                        .shallowFrom((AtlasEntity) toFlag))
                                                                .withTags(toFlag.getTags())
                                                                .withReplacedTag(HighwayTag.KEY,
                                                                        HighwayTag.KEY,
                                                                        HighwayTag.PEDESTRIAN
                                                                                .getTagValue()),
                                                object.getAtlas()))
                                        .collect(Collectors.toSet()));
                    }

                    final Set<AtlasObject> objectsToFlag = this.getObjectsToFlag(object);
                    return this
                            .createFlag(objectsToFlag,
                                    this.getLocalizedInstruction(1, object.getOsmIdentifier(), tag,
                                            tag.getTagValue()))
                            .addFixSuggestions(objectsToFlag.stream()
                                    .map(toFlag -> FeatureChange.add(
                                            (AtlasEntity) ((CompleteEntity) CompleteEntity
                                                    .shallowFrom((AtlasEntity) toFlag))
                                                            .withTags(toFlag.getTags())
                                                            .withRemovedTag(AreaTag.KEY),
                                            object.getAtlas()))
                                    .collect(Collectors.toSet()));
                });
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Set<AtlasObject> getObjectsToFlag(final AtlasObject object)
    {
        if (object instanceof Edge)
        {
            return new AreasWithHighwayTagCheckWalker((Edge) object).collectEdges().stream()
                    .filter(Edge::isMainEdge).collect(Collectors.toSet());
        }

        return Collections.singleton(object);

    }
}
