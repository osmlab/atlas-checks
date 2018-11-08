package org.openstreetmap.atlas.checks.validation.areas;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
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

    private static final Heading HEADING_THRESHOLD_LOWER = Heading.degrees(15);
    private static final Heading HEADING_THRESHOLD_UPPER = Heading.degrees(165);
    private static final double COS_15 = 0.9659258262890682867497431997288973676339;

    /**
     * Default constructor
     *
     * @param configuration {@link Configuration} required to construct any Check
     */
    public SpikyBuildingCheck(final Configuration configuration)
    {
        super(configuration);
    }

    // TODO remove code duplication
    @Override public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Area
                || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && (this.isBuildingOrPart(object) || this.isBuildingRelationMember(object));
    }

    private boolean isBuildingOrPart(final AtlasObject object)
    {
        return (BuildingTag.isBuilding(object)
                && Validators.isNotOfType(object, BuildingTag.class, BuildingTag.ROOF))
                || Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO);
    }

    private boolean isBuildingRelationMember(final AtlasObject object)
    {
        return object instanceof AtlasEntity && ((AtlasEntity) object).relations().stream()
                .anyMatch(relation -> Validators.isOfType(relation, RelationTypeTag.class,
                        RelationTypeTag.BUILDING)
                        && relation.members().stream()
                        .anyMatch(member -> member.getEntity().equals(object)
                                && (member.getRole().equals("outline"))
                                || member.getRole().equals("part")));
    }
    // end TODO code duplication

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
        } else if (((Relation) object).isMultiPolygon())
        {
            return StreamSupport.stream(((Relation) object).members().spliterator(), false)
                    .map(this::toPolygon)
                    .flatMap(optPoly -> optPoly.isPresent() ? Stream.of(optPoly.get()) : Stream.empty());
        }
        logger.warn("Returning empty stream");
        return Stream.empty();
    }

    private boolean hasSkinnyAngle(final Polygon polygon)
    {
        final List<Segment> segments = polygon.segments();
        // comparing second segment to first
        for(int i = 1; i < segments.size(); i++) {
            if (isSkinnyAngle(segments.get(i-1), segments.get(i))) {
                return true;
            }
        }
        // compare last segment to first
        return isSkinnyAngle(segments.get(segments.size() - 1), segments.get(0));
    }

    private boolean isSkinnyAngle(final Segment firstSegment, final Segment secondSegment)
    {
//        final Heading first = firstSegment.pointingNorth().heading().get();
//        final Heading second = secondSegment.pointingNorth().heading().get();
//        return first.difference(second).isLessThan(HEADING_THRESHOLD_LOWER)
//                || first.difference(second).isGreaterThan(HEADING_THRESHOLD_UPPER);
        return cosine(firstSegment.start(), firstSegment.end(), secondSegment.end()) > COS_15;
    }

    public static double dotProd(final Location start, final Location middle, final Location end)
    {
        return dm7Mult(start.getLatitude().asDm7(), middle.getLatitude().asDm7(), end.getLatitude().asDm7())
                + dm7Mult(start.getLongitude().asDm7(), middle.getLongitude().asDm7(), end.getLongitude().asDm7());
    }

    private static long dm7Mult(final long start, final long middle, final long end)
    {
        return (start - middle) * (end - middle);
    }

    public static double distance(final Location start, final Location end)
    {
        return Math.sqrt(Math.pow(start.getLatitude().asDm7() - end.getLatitude().asDm7(), 2)
                + Math.pow(start.getLongitude().asDm7() - end.getLongitude().asDm7(), 2));
    }

    public static double cosine(final Location start, final Location middle, final Location end)
    {
        return dotProd(start, middle, end) / (distance(start, middle) * distance(middle, end));
    }

    @Override protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Stream<Polygon> buildingPolylines = getPolylines(object);

        if(buildingPolylines.anyMatch(this::hasSkinnyAngle)) {
            return Optional.of(this.createFlag(object, "Spiky building here."));
        }
        return Optional.empty();
    }
}
