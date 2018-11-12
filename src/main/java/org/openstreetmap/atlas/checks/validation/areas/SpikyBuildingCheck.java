package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First pass at a spikybuilding check.
 *
 * @author nachtm
 */
public class SpikyBuildingCheck extends BaseCheck<Long>
{
    private final Logger logger = LoggerFactory.getLogger(SpikyBuildingCheck.class);

    private static final Angle HEADING_THRESHOLD_LOWER = Angle.degrees(15);
    private static final int MIN_NUMBER_OF_SIDES = 3;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "An angle of {0,number,#.###}, which is under the minimum allowed angle of {1,number}");

    private static final String FIRST_INSTRUCTION = "This building has the following suspicious angles: ";

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public SpikyBuildingCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return ((object instanceof Area && ((Area) object).asPolygon().size() > MIN_NUMBER_OF_SIDES)
                || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && (this.isBuildingOrPart(object));
    }

    private boolean isBuildingOrPart(final AtlasObject object)
    {
        return BuildingTag.isBuilding(object)
                || Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO);
    }

    private Optional<Polygon> toPolygon(final RelationMember member)
    {
        if (member.getEntity().getType().equals(ItemType.AREA))
        {
            return Optional.of(((Area) member.getEntity()).asPolygon());
        }
        return Optional.empty();
    }

    private Stream<Polygon> getPolylines(final AtlasObject object)
    {
        if (object instanceof Area)
        {
            return Stream.of(((Area) object).asPolygon());
        }
        else if (((Relation) object).isMultiPolygon())
        {
            return ((Relation) object).members().stream().map(this::toPolygon).flatMap(
                    optPoly -> optPoly.isPresent() ? Stream.of(optPoly.get()) : Stream.empty());
        }
        logger.warn("Returning empty stream");
        return Stream.empty();
    }

    // this is almost exactly the same as polygon.anglesLessThanOrEqualTo, except we also need to
    // compare the angle between the last segment and the first segment.
    private List<Tuple<Angle, Location>> getSkinnyAngleLocations(final Polygon polygon)
    {
        final List<Tuple<Angle, Location>> results = new ArrayList<>();
        final List<Segment> segments = polygon.segments();
        // comparing segment to previous segment
        for (int i = 1; i < segments.size(); i++)
        {
            final Angle difference = getDifferenceBetween(segments.get(i - 1), segments.get(i));
            if (difference.isLessThan(HEADING_THRESHOLD_LOWER))
            {
                results.add(Tuple.createTuple(difference, segments.get(i).start()));
            }
        }
        // compare last segment to first
        final Angle difference = getDifferenceBetween(segments.get(segments.size() - 1),
                segments.get(0));
        if (difference.isLessThan(HEADING_THRESHOLD_LOWER))
        {
            results.add(Tuple.createTuple(difference, segments.get(0).start()));
        }
        return results;

    }

    private Angle getDifferenceBetween(final Segment firstSegment, final Segment secondSegment)
    {
        // TODO resolve get() issues
        final Heading first = firstSegment.heading().get();
        final Heading second = secondSegment.reversed().heading().get();
        return first.difference(second);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final List<Tuple<Angle, Location>> allSkinnyAngles = getPolylines(object)
                .map(this::getSkinnyAngleLocations)
                .filter(angleLocations -> !angleLocations.isEmpty()).flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (!allSkinnyAngles.isEmpty())
        {
            final CheckFlag flag;
            if (object instanceof Area)
            {
                flag = this.createFlag(object, FIRST_INSTRUCTION);
            }
            else
            {
                flag = this.createFlag(((Relation) object).flatten(), FIRST_INSTRUCTION);
            }
            flag.addInstructions(allSkinnyAngles
                    .stream().map(tuple -> this.getLocalizedInstruction(0,
                            tuple.getFirst().asDegrees(), HEADING_THRESHOLD_LOWER.asDegrees()))
                    .collect(Collectors.toList()));
            flag.addPoints(
                    allSkinnyAngles.stream().map(Tuple::getSecond).collect(Collectors.toList()));
            return Optional.of(flag);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
