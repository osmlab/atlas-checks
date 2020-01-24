package org.openstreetmap.atlas.checks.validation.points;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link NodeValenceCheckTest} data generator.
 *
 * @author mkalender
 */
public class NodeValenceCheckTestRule extends CoreTestRule
{
    private static final String FOUR = "37.780744, -122.471797";
    private static final String ONE = "37.780574, -122.472852";
    private static final String THREE = "37.780572, -122.472846";
    private static final String TWO = "37.780724, -122.472249";
    private static final String FIVE = "37.780724, -112.472249";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = ONE)),
                    @Node(coordinates = @Loc(value = TWO)),
                    @Node(coordinates = @Loc(value = THREE)),
                    @Node(coordinates = @Loc(value = FOUR)),
                    @Node(coordinates = @Loc(value = FIVE)) })
    private Atlas noConnectionAtlas;

    private static List<Location> generateRandomLocations(final int count)
    {
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            locations.add(Location.random(Rectangle.TEST_RECTANGLE));
        }

        return locations;
    }

    public Atlas generateAtlas(final int connectionCount, final boolean onlyOneWay,
            final HighwayTag highwayTag)
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        long randomIdentifier = Math.abs(new Random().nextLong());

        // Pick a random location for connections
        final Location connectionLocation = Location.random(Rectangle.TEST_RECTANGLE);
        builder.addNode(randomIdentifier++, connectionLocation, RandomTagsSupplier.randomTags(5));

        // Collection to add edges
        final List<PolyLine> polyLinesToAdd = new ArrayList<>();

        // Generate nodes/edges
        for (int i = 0; i < connectionCount; i++)
        {
            // Generate locations
            final int edgeLocationCount = 1 + new Random().nextInt(4);
            final List<Location> edgeLocations = generateRandomLocations(edgeLocationCount);

            // Add connection
            final boolean isIncoming = new Random().nextBoolean();
            if (isIncoming)
            {
                builder.addNode(randomIdentifier++, edgeLocations.get(edgeLocations.size() - 1),
                        RandomTagsSupplier.randomTags(5));
                edgeLocations.add(0, connectionLocation);
            }
            else
            {
                builder.addNode(randomIdentifier++, edgeLocations.get(0),
                        RandomTagsSupplier.randomTags(5));
                edgeLocations.add(connectionLocation);
            }

            // Add polyline to the list
            final PolyLine geometry = new PolyLine(edgeLocations);
            polyLinesToAdd.add(geometry);
        }

        // Add edges
        for (final PolyLine polyLine : polyLinesToAdd)
        {
            // Generate tags
            final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
            tags.put(HighwayTag.KEY, highwayTag.getTagValue());

            // Add the edge
            final long identifier = randomIdentifier++;
            builder.addEdge(identifier, polyLine, tags);

            // Add reverse edge randomly
            final boolean addReverseEdge = new Random().nextBoolean();
            if (!onlyOneWay && addReverseEdge)
            {
                builder.addEdge(-identifier, polyLine.reversed(), tags);
            }
        }

        return builder.get();
    }

    public Atlas noConnectionAtlas()
    {
        return this.noConnectionAtlas;
    }
}
