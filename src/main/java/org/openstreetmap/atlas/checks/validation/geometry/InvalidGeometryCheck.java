package org.openstreetmap.atlas.checks.validation.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.GeometryValidator;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticGeometrySlicedTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Checks Atlas items using {@link org.locationtech.jts.geom.Geometry} isValid and isSimple methods.
 * Generates flag based on JTS provided invalidity and non simplicity causes.
 *
 * @author jklamer
 * @author bbreithaupt
 */
public class InvalidGeometryCheck extends BaseCheck<Long>
{
    private static final String NOT_SIMPLE_TEMPLATE = "Geometry is Not Simple: {0}. ";
    private static final String NOT_VALID_TEMPLATE = "Geometry is Not Valid: {0}. ";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(NOT_SIMPLE_TEMPLATE,
            NOT_VALID_TEMPLATE);
    private static final long serialVersionUID = 4212714363153085279L;

    public InvalidGeometryCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Area || object instanceof LineItem)
                && !SyntheticGeometrySlicedTag.isGeometrySliced(object)
                && !(object instanceof Edge && ((Edge) object).connectedNodes().stream()
                        .anyMatch(SyntheticBoundaryNodeTag::isBoundaryNode));
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final List<String> instructions = new ArrayList<>();

        // check if simple if not a LineItem (too many correct in OSM but not simple linear
        // geometries)
        final Optional<String> simpleTest = object instanceof LineItem ? Optional.empty()
                : GeometryValidator.testSimplicity(((AtlasItem) object).getRawGeometry());
        // check if valid
        final Optional<String> validTest = GeometryValidator
                .testValidity(((AtlasItem) object).getRawGeometry());

        simpleTest.ifPresent(reason -> instructions.add(this.getLocalizedInstruction(0, reason)));
        validTest.ifPresent(reason -> instructions.add(this.getLocalizedInstruction(1, reason)));

        return instructions.isEmpty() ? Optional.empty()
                : Optional.of(new CheckFlag(this.getTaskIdentifier(object),
                        Collections.singleton(object), instructions));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
