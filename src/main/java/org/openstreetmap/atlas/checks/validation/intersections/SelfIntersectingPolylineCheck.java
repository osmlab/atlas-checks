package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.GeometryValidator;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BaseCheck} that identifies self-intersecting {@link PolyLine}s within {@link Area}s,
 * {@link Edge}s and {@link Line}s. Both shape point and non-shape point intersections are flagged.
 *
 * @author mgostintsev
 * @author dbaah
 */
public class SelfIntersectingPolylineCheck extends BaseCheck<Long>
{
    private static final String AREA_INSTRUCTION = "Feature {0,number,#} has invalid geometry at {1}";
    private static final String POLYLINE_BUILDING_INSTRUCTION = "Feature {0,number,#} is a incomplete "
            + "building at {1}";

    private static final String DUPLICATE_EDGE_INSTRUCTION = "Feature {0,number,#} has a duplicate "
            + "Edge at {1}";
    private static final String POLYLINE_INSTRUCTION = "Self-intersecting polyline for feature "
            + "{0,number,#} at {1}";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(POLYLINE_INSTRUCTION,
            AREA_INSTRUCTION, POLYLINE_BUILDING_INSTRUCTION, DUPLICATE_EDGE_INSTRUCTION);
    private static final Logger logger = LoggerFactory
            .getLogger(SelfIntersectingPolylineCheck.class);
    public static final Integer THREE = 3;
    private static final long serialVersionUID = 2722288442633787006L;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SelfIntersectingPolylineCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * Checks to see whether the atlas object has the prerequisites to be evaluated. It uses a
     * function {@link BaseCheck#isFlagged(Object)} that looks through a list of elements that have
     * been flagged by the check algorithm, and if the check has already looked at a specific
     * feature it can skip it here. This is useful if you are walking the graph in your check
     * algorithm and then can flag each feature that you visit while walking the graph.
     *
     * @param object
     *            the {@link AtlasObject} you are checking
     * @return {@code true} if object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Master edges excluding ineligible highway tags
        return object instanceof Edge && ((Edge) object).isMasterEdge()
                // Areas
                || object instanceof Area
                // Lines excluding ineligible highway tags
                || object instanceof Line
                        // Exclude waterway tags
                        && !Validators.hasValuesFor(object, WaterwayTag.class);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Optional<CheckFlag> response;
        final int localizedInstructionIndex;

        final PolyLine polyline;
        if (object instanceof Edge)
        {
            polyline = ((Edge) object).asPolyLine();
            // Send building instructions if building tag exists
            localizedInstructionIndex = (TagPredicates.IS_BUILDING.test(object)) ? 2 : 0;
        }
        else if (object instanceof Line)
        {
            polyline = ((Line) object).asPolyLine();
            // Send building instructions if building tag exists
            localizedInstructionIndex = (TagPredicates.IS_BUILDING.test(object)) ? 2 : 0;
        }
        else if (object instanceof Area)
        {
            polyline = ((Area) object).asPolygon();
            // Send duplicate Edge instructions if duplicate Edges exist
            localizedInstructionIndex = hasDuplicateSegments(polyline) ? THREE : 1;
        }
        else
        {
            throw new CoreException("Invalid item type {}", object.getClass().toString());
        }

        // First, find shape point intersections
        final Set<Location> selfIntersections = polyline.selfIntersections();

        if (selfIntersections.size() > 0)
        {
            final CheckFlag flag = new CheckFlag(Long.toString(object.getIdentifier()));
            flag.addObject(object);
            flag.addInstruction(this.getLocalizedInstruction(localizedInstructionIndex,
                    object.getOsmIdentifier(), selfIntersections.toString()));
            selfIntersections.forEach(flag::addPoint);
            response = Optional.of(flag);
        }
        else
        {
            // Next, find intersections occurring at non-shape points using JTS verification
            boolean isJtsValid = true;
            try
            {
                if (object instanceof Area)
                {
                    isJtsValid = GeometryValidator.isValidPolygon((Polygon) polyline);
                }
                else
                {
                    isJtsValid = GeometryValidator.isValidPolyLine(polyline);
                }
            }
            catch (final IllegalArgumentException e)
            {
                // Invalid geometry found when converting the PolyLine/Polygon.
                // This can be a number of cases. For example, a LineString expects exactly 0 or >=2
                // points or a Polygon expects 0 or >= 4 points. This isn't self-intersecting
                // geometry, but rather inconsistent geometry, according to JTS.
                logger.error("Encountered invalid geometry for feature {}",
                        object.getOsmIdentifier(), e);
            }

            if (!isJtsValid)
            {
                response = Optional.of(createFlag(object,
                        this.getLocalizedInstruction(localizedInstructionIndex)));
            }
            else
            {
                response = Optional.empty();
            }
        }

        return response;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Returns true if adjacent {@link Segment} has identical lat,lng sequences
     *
     * @param polyline
     *            the {@link PolyLine} being examined
     * @return {@code true} if the any set of adjacent segments have identical geometries
     */
    private boolean hasDuplicateSegments(final PolyLine polyline)
    {
        final List<Segment> segments = polyline.segments();

        // Loop through Polyline Segments
        for (final Segment segment : segments)
        {
            // Check if segment exists elsewhere in List
            if (segments.indexOf(segment) != segments.lastIndexOf(segment))
            {
                return true;
            }
        }

        return false;
    }
}
