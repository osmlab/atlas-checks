package org.openstreetmap.atlas.checks.flag;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

import com.google.gson.JsonArray;

/**
 * A {@link CheckFlag} is used to flag one or more {@link AtlasObject}s found to violate some set of
 * rules.
 *
 * @author matthieun
 * @author cuthbertm
 * @author mgostintsev
 * @author brian_l_davis
 */
public class CheckFlag implements Iterable<Location>, Located, Serializable
{
    private static final long serialVersionUID = -1287808902452203852L;
    private final String identifier;
    private String challengeName = null;
    private final List<String> instructions = new ArrayList<>();
    private final Set<FlaggedObject> flaggedObjects = new HashSet<>();

    /**
     * A basic constructor that simply flags some identifying value
     *
     * @param identifier
     *            the identifying value to flag
     */
    public CheckFlag(final String identifier)
    {
        this.identifier = identifier;
    }

    /**
     * Creates a {@link CheckFlag} with a set of {@link AtlasObject}s to flag, most commonly used
     * when all of the {@link AtlasObject}s contribute to the rule violation
     *
     * @param identifier
     *            the identifying value to flag
     * @param objects
     *            {@link AtlasObject}s to flag
     * @param instructions
     *            a list of free form instructions
     */
    public CheckFlag(final String identifier, final Set<AtlasObject> objects,
            final List<String> instructions)
    {
        this(identifier, objects, instructions, new ArrayList<>());
    }

    /**
     * Creates a {@link CheckFlag} with the addition of a list of {@code point} {@link Location}s
     * that highlight specific points in the geometry that caused the rule violation
     *
     * @param identifier
     *            the identifying value to flag
     * @param objects
     *            {@link AtlasObject}s to flag
     * @param instructions
     *            a list of free form instructions
     * @param points
     *            {@code point} {@link Location}s to highlight
     */
    public CheckFlag(final String identifier, final Set<AtlasObject> objects,
            final List<String> instructions, final List<Location> points)
    {
        addObjects(objects);
        addPoints(points);
        addInstructions(instructions);
        this.identifier = identifier;
    }

    /**
     * Adds any instructions that may help communicate why the {@link AtlasObject}(s) were flagged
     *
     * @param instruction
     *            a free form instruction
     */
    public void addInstruction(final String instruction)
    {
        if (StringUtils.isNotEmpty(instruction))
        {
            this.instructions.add(instruction);
        }
    }

    /**
     * Adds a list of instructions that may help communicate why the {@link AtlasObject}(s) were
     * flagged. This can be useful if multiple rules were violated
     *
     * @param instructions
     *            a list of free form instruction
     */
    public void addInstructions(final Iterable<String> instructions)
    {
        instructions.forEach(this::addInstruction);
    }

    /**
     * Adds an {@link AtlasObject} to flag
     *
     * @param object
     *            an {@link AtlasObject}
     */
    public void addObject(final AtlasObject object)
    {
        if (object instanceof AtlasItem)
        {
            if (object instanceof LocationItem)
            {
                this.flaggedObjects.add(new FlaggedPoint((LocationItem) object));
            }
            else
            {
                this.flaggedObjects.add(new FlaggedPolyline(object));
            }
        }
    }

    /**
     * Flags an {@link AtlasObject}, highlighting a specific {@code point} {@link Location} and
     * instructions with more detail. This helps build flags iteratively when more complex
     * {@link Check}s span a large number of {@link AtlasObject}s.
     *
     * @param object
     *            the {@link AtlasObject} to flag
     * @param point
     *            the {@code point} {@link Location} to highlight
     * @param instruction
     *            a free form instruction
     */
    public void addObject(final AtlasObject object, final Location point, final String instruction)
    {
        this.addObject(object);
        this.addPoint(point);
        this.addInstruction(instruction);
    }

    /**
     * Flags an {@link AtlasObject} with instructions. This helps build flags iteratively when more
     * complex {@link Check}s span a large number of {@link AtlasObject}s
     *
     * @param object
     *            the {@link AtlasObject} to flag
     * @param instruction
     *            a free form instruction
     */
    public void addObject(final AtlasObject object, final String instruction)
    {
        this.addObject(object);
        this.addInstruction(instruction);
    }

    /**
     * Adds a list of {@link AtlasObject}s to flag
     *
     * @param objects
     *            a list of {@link AtlasObject}s
     */
    public void addObjects(final Iterable<AtlasObject> objects)
    {
        Iterables.stream(objects).forEach(this::addObject);
    }

    /**
     * Flags a specific {@link Location}
     *
     * @param point
     *            the {@code point} {@link Location} to flag
     */
    public void addPoint(final Location point)
    {
        this.flaggedObjects.add(new FlaggedPoint(point));
    }

    /**
     * Flags a list of {@code point} {@link Location}s
     *
     * @param points
     *            the {@code point} {@link Location}s to flag
     */
    public void addPoints(final Iterable<Location> points)
    {
        Iterables.stream(points).map(FlaggedPoint::new).forEach(this.flaggedObjects::add);
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.forLocated(new MultiIterable<>(this.getShapes()));
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof CheckFlag))
        {
            return false;
        }

        final CheckFlag otherFlag = (CheckFlag) other;
        return Objects.equals(this.identifier, otherFlag.identifier)
                && Objects.equals(this.challengeName, otherFlag.challengeName)
                && Objects.equals(this.instructions, otherFlag.instructions)
                && Objects.equals(this.flaggedObjects, otherFlag.flaggedObjects);
    }

    /**
     * @return a Challenge name
     */
    public Optional<String> getChallengeName()
    {
        return Optional.ofNullable(this.challengeName);
    }

    /**
     * Will return the first country ISO that it finds in the objects provided in the flag
     *
     * @return a country ISO3 code
     */
    public String getCountryISO()
    {
        for (final FlaggedObject object : this.flaggedObjects)
        {
            if (object.hasCountry())
            {
                return object.getCountry();
            }
        }
        return FlaggedObject.COUNTRY_MISSING;
    }

    /**
     * @return a set of flagged {@link AtlasObject}s
     */
    public Set<FlaggedObject> getFlaggedObjects()
    {
        return this.flaggedObjects;
    }

    /**
     * @return the flag identifier
     */
    public String getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return all of the instructions in a {@code String}
     */
    public String getInstructions()
    {
        final StringBuilder builder = new StringBuilder();
        int instructionNumber = 1;
        for (final String instruction : this.instructions)
        {
            if (StringUtils.isNotEmpty(instruction))
            {
                if (builder.length() > 0)
                {
                    builder.append("\n");
                }
                // This is a visually displayed list of instructions for use in Map Roulette
                builder.append(instructionNumber++ + ". " + instruction);
            }
        }
        return builder.toString();
    }

    /**
     * @return a list of {@link GeoJsonBuilder.LocationIterableProperties} representing all flagged
     *         geometries
     */
    public List<GeoJsonBuilder.LocationIterableProperties> getLocationIterableProperties()
    {
        return this.flaggedObjects.stream()
                .map(flaggedObject -> new GeoJsonBuilder.LocationIterableProperties(
                        flaggedObject.getGeometry(), flaggedObject.getProperties()))
                .collect(Collectors.toList());
    }

    /**
     * Builds a MapRouletted {@link Task} from this {@link CheckFlag}
     *
     * @return a {@link Task}
     */
    public Task getMapRouletteTask()
    {
        final Task task = new Task();
        task.setInstruction(this.getInstructions());
        task.setProjectName(this.getCountryISO());
        task.setChallengeName(this.getChallengeName().orElse(this.getClass().getSimpleName()));
        task.setTaskIdentifier(this.identifier);

        // Add custom pin point(s), if supplied.
        final Set<Location> points = getPoints();
        if (!points.isEmpty())
        {
            task.setPoints(points);
        }
        else
        {
            final Set<PolyLine> polyLines = getPolyLines();
            if (!polyLines.isEmpty())
            {
                // Retrieve the first item in the list and retrieve the first point in the
                // geometry for the object
                task.setPoint(polyLines.iterator().next().iterator().next());
            }
        }

        final JsonArray features = new JsonArray();
        this.getLocationIterableProperties()
                .forEach(shape -> features.add(new GeoJsonBuilder().create(shape)));
        task.setGeoJson(Optional.of(features));

        return task;
    }

    /**
     * @return a set of all {@code point} {@link Location} geometries flagged
     */
    public Set<Location> getPoints()
    {
        return this.flaggedObjects.stream().map(FlaggedObject::getGeometry)
                .filter(geometry -> geometry instanceof Location)
                .map(geometry -> (Location) geometry).collect(Collectors.toSet());
    }

    /**
     * @return a set of all {@code polyline} geometries flagged
     */
    public Set<PolyLine> getPolyLines()
    {
        return this.flaggedObjects.stream().map(FlaggedObject::getGeometry)
                .filter(geometry -> geometry instanceof PolyLine)
                .map(geometry -> (PolyLine) geometry).collect(Collectors.toSet());
    }

    /**
     * @return a {@code shape} representation of all {@code polyline} geometries flagged
     */
    public Iterable<Iterable<Location>> getShapes()
    {
        return Iterables.asIterable(getPolyLines().stream()
                .map(polyLine -> (Iterable<Location>) polyLine).collect(Collectors.toList()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.identifier, this.challengeName, this.instructions,
                this.flaggedObjects);
    }

    @Override
    public Iterator<Location> iterator()
    {
        return new MultiIterable<>(getShapes()).iterator();
    }

    /**
     * Writes the string value of this {@link CheckFlag} to the {@link WritableResource}
     *
     * @param writableResource
     *            a {@link WritableResource} to write to
     */
    public void save(final WritableResource writableResource)
    {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(writableResource.write(), StandardCharsets.UTF_8)))
        {
            out.write(toString());
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not save Check Flag to {}", e, writableResource);
        }
    }

    /**
     * Sets a Challenge name for this Flag
     *
     * @param challengeName
     *            a Challenge name
     */
    public void setChallengeName(final String challengeName)
    {
        this.challengeName = challengeName;
    }

    @Override
    public String toString()
    {
        return String.format("[CheckFlag: %s, %s]", this.identifier, this.getInstructions());
    }
}
