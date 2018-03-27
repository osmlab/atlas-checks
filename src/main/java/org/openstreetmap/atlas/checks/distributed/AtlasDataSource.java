package org.openstreetmap.atlas.checks.distributed;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.hadoop.fs.PathFilter;
import org.openstreetmap.atlas.checks.atlas.CountrySpecificAtlasFilePathFilter;
import org.openstreetmap.atlas.checks.atlas.OsmPbfFilePathFilter;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves {@link Atlas} data source(s) from an {@code input} location. Multiple data files found
 * are stitched together to build a single {@link Atlas} data source. Supported data source types
 * are {@link Atlas} and Osm Protobuf files.
 *
 * @author brian_l_davis
 */
public class AtlasDataSource implements Serializable
{
    private static final long serialVersionUID = -6407331424906155431L;
    private final Logger logger = LoggerFactory.getLogger(AtlasDataSource.class);
    private final SparkFileHelper loadHelper;
    private final AtlasFilePathResolver pathResolver;
    private final MultiPolygon polygon;

    /**
     * Constructs a basic {@link AtlasDataSource}
     *
     * @param sparkContext
     *            Spark context as key-value pairs to use as context
     * @param configuration
     *            The {@link Configuration}
     */
    public AtlasDataSource(final Map<String, String> sparkContext,
            final Configuration configuration)
    {
        this(sparkContext, configuration, MultiPolygon.MAXIMUM);
    }

    /**
     * Constructs an {@link AtlasDataSource} with a {@link MultiPolygon} boundary. Only
     * {@link Atlas} data within the boundary will be loaded
     *
     * @param sparkContext
     *            spark context as key-value pairs to use as context
     * @param configuration
     *            the {@link Configuration}
     * @param polygon
     *            a {@link MultiPolygon} boundary
     */
    public AtlasDataSource(final Map<String, String> sparkContext,
            final Configuration configuration, final MultiPolygon polygon)
    {
        this.loadHelper = new SparkFileHelper(sparkContext);
        this.pathResolver = new AtlasFilePathResolver(configuration);
        this.polygon = polygon;
    }

    /**
     * Constructs an {@link AtlasDataSource} with bounding box. Only {@link Atlas} data within the
     * bounding box will be loaded
     *
     * @param sparkContext
     *            spark context as key-value pairs to use as context
     * @param configuration
     *            the {@link Configuration}
     * @param boundingBox
     *            a {@link Rectangle} boundary
     */
    public AtlasDataSource(final Map<String, String> sparkContext,
            final Configuration configuration, final Rectangle boundingBox)
    {
        this(sparkContext, configuration, MultiPolygon.forPolygon(boundingBox));
    }

    /**
     * Loads an {@link Atlas} from the input location
     *
     * @param input
     *            location of the {@link Atlas} source
     * @param country
     *            country of the {@link Atlas}
     * @return {@link Atlas} representation of the data source
     */
    public Atlas load(final String input, final String country)
    {
        return load(input, country, intermediateAtlas ->
        {
        });
    }

    /**
     * Loads an {@link Atlas} from the input location. Intermediate {@link Atlas}es created are
     * submitted to the provided {@link Consumer} to allow for any additional handling.
     *
     * @param input
     *            location of the {@link Atlas} source
     * @param country
     *            country of the {@link Atlas}
     * @param intermediateAtlasHandler
     *            handler given intermediate {@link Atlas} files when created
     * @return {@link Atlas} representation of the data source
     */
    public Atlas load(final String input, final String country,
            final Consumer<Atlas> intermediateAtlasHandler)
    {
        // Path filters for supported file types
        final PathFilter pbfFilter = new OsmPbfFilePathFilter();
        final PathFilter atlasFilter = new CountrySpecificAtlasFilePathFilter(country);

        final Optional<Resource> resource = this.loadHelper.collectSourceFile(input, pbfFilter,
                atlasFilter);
        if (resource.isPresent())
        {
            final Resource dataSource = resource.get();
            if (Atlas.isAtlas(dataSource))
            {
                return new AtlasResourceLoader().load(dataSource);
            }
            else if (FileSuffix.resourceFilter(FileSuffix.PBF).test(dataSource))
            {
                this.logger.info("Loading Atlas from OSM protobuf {}", input);
                final Atlas atlas = this.loadPbf(dataSource, country);
                intermediateAtlasHandler.accept(atlas);
                return atlas;
            }
        }
        else
        {
            final String directory = this.pathResolver.resolvePath(input, country);
            final List<Resource> atlasResources = this.loadHelper.collectSourceFiles(directory,
                    true, atlasFilter);
            if (atlasResources.size() > 0)
            {
                return new AtlasResourceLoader().load(atlasResources);
            }
            else
            {
                final List<Resource> pbfResources = this.loadHelper.collectSourceFiles(directory,
                        true, pbfFilter);
                final int pbfCount = pbfResources.size();
                if (pbfCount > 0)
                {
                    this.logger.info("Loading Atlas from {} OSM protobuf(s) found in {}", pbfCount,
                            input);
                    final List<Atlas> atlases = pbfResources.parallelStream()
                            .map(dataSource -> this.loadPbf(dataSource, country))
                            .peek(intermediateAtlasHandler).collect(Collectors.toList());
                    return new MultiAtlas(atlases);
                }
            }
        }
        return null;
    }

    private Atlas loadPbf(final Resource input, final String country)
    {
        // Setting the CountryBoundaryMap to the polygon boundary
        final CountryBoundaryMap map = new CountryBoundaryMap(
                Collections.singletonMap(country, this.polygon));
        final AtlasLoadingOption option = AtlasLoadingOption.createOptionWithAllEnabled(map);
        return new OsmPbfLoader(input, this.polygon, option).read();
    }
}
