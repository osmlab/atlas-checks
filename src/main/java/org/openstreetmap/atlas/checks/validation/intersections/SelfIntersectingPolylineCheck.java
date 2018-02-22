package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.Taggable;
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
 */
public class SelfIntersectingPolylineCheck extends BaseCheck<Long>
{
    private static final String INSTRUCTION_SHORT = "Self-intersecting polyline for feature {0,number,#}";
    private static final String INSTRUCTION_LONG = INSTRUCTION_SHORT + " at {1}";
    private static final String AREA_INSTRUCTION = "Feature {0,number,#} has invalid geometry at {1}";
    private static final String AREA_BUILDING_INSTRUCTION = "Feature {0,number,#} is an incomplete "
            + "building at {1}";
    public static final String DUPLICATE_EDGE_INSTRUCTION = "Feature {0,number,#} has duplicate Edges";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_SHORT,
            INSTRUCTION_LONG, AREA_INSTRUCTION, AREA_BUILDING_INSTRUCTION,
            DUPLICATE_EDGE_INSTRUCTION);
    private static final Logger logger = LoggerFactory
            .getLogger(SelfIntersectingPolylineCheck.class);
    // Excluded Highway tags
    private static final Predicate<Taggable> INELIGIBLE_HIGHWAY_TAGS = object -> Validators
            .isOfType(object, HighwayTag.class, HighwayTag.PROPOSED)
            || Validators.isOfType(object, HighwayTag.class, HighwayTag.CONSTRUCTION)
            || Validators.isOfType(object, HighwayTag.class, HighwayTag.FOOTWAY)
            || Validators.isOfType(object, HighwayTag.class, HighwayTag.PATH);
    // Excluded Waterway tags
    private static final Predicate<Taggable> WATERWAY_TAGS = object -> Validators
            .hasValuesFor(object, WaterwayTag.class);
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
        // Master edges with eligible highway tags
        return ((object instanceof Edge && ((Edge) object).isMasterEdge()
                && !INELIGIBLE_HIGHWAY_TAGS.test(object))
                // Areas
                || (object instanceof Area)
                // Lines with eligible highway tags
                || (object instanceof Line && !INELIGIBLE_HIGHWAY_TAGS.test(object)))
                // No waterway tags
                && !WATERWAY_TAGS.test(object);
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
            localizedInstructionIndex = (TagPredicates.IS_BUILDING.test(object)) ? THREE : 0;
        }
        else if (object instanceof Line)
        {
            polyline = ((Line) object).asPolyLine();
            // If Line has a Building tag, send special instructions
            localizedInstructionIndex = (TagPredicates.IS_BUILDING.test(object)) ? THREE : 0;
        }
        else if (object instanceof Area)
        {
            polyline = ((Area) object).asPolygon();
            // If polyline contains duplicate edges, return specific instructions
            localizedInstructionIndex = hasDuplicateEdges(polyline) ? 4 : 2;

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
     * Returns true if Polyline has Duplicate edges
     * 
     * @param polyline
     *            Atlas object
     * @return boolean
     */
    private boolean hasDuplicateEdges(PolyLine polyline)
    {
        final List<Segment> segments = polyline.segments();
        final List<Segment> duplicates = new ArrayList<>();

        // Loop through Polyline Segments
        for (int i = 0; i < segments.size(); i++)
        {
            final Segment segment = segments.get(i);
            final int adjacentEdges = i + 2;

            // Check if segment exists in List
            if (segments.indexOf(segment) != segments.lastIndexOf(segment))
            {
                // If adjacent segment is the same value as our current, add to
                if (adjacentEdges < segments.size() && segment.equals(segments.get(adjacentEdges)))
                {
                    duplicates.add(segment);
                }

            }

        }

        return duplicates.size() > 0;
    }
}
