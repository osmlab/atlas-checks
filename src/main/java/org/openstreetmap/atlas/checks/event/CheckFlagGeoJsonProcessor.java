package org.openstreetmap.atlas.checks.event;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.distributed.GeoJsonPathFilter;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * A {@link Processor} for {@link CheckFlagEvent}s to write them into GeoJson files
 *
 * @author brian_l_davis
 */
public final class CheckFlagGeoJsonProcessor
        implements org.openstreetmap.atlas.event.Processor<CheckFlagEvent>
{

    private static final int MAX_BATCH_SUM = 25000;
    private static final int MIN_BATCH_SIZE = 100;
    private static final int BUCKET_CAPACITY = 100;
    private static final int BUCKET_INCREMENT = 25;

    private static final Logger logger = LoggerFactory.getLogger(CheckFlagGeoJsonProcessor.class);

    // File helper to write files
    private final SparkFileHelper fileHelper;

    // Directory to write files in
    private final String directory;

    // Event Features bucketed by Challenge
    private final ConcurrentHashMap<String, Vector<JsonObject>> featureBuckets = new ConcurrentHashMap<>();

    // Bucket locks used to synchronize filling and emptying buckets
    private final ConcurrentHashMap<String, ReadWriteLock> bucketLocks = new ConcurrentHashMap<>();

    // Whether or not to compress output file
    private boolean compressOutput = true;

    // Batch size override
    private int batchSizeOverride;

    // Detect has written
    private boolean hasWritten;

    /**
     * Default constructor
     *
     * @param fileHelper
     *            {@link SparkFileHelper} for I/O operations
     * @param outputFolder
     *            output folder path to write files to
     */
    public CheckFlagGeoJsonProcessor(final SparkFileHelper fileHelper, final String outputFolder)
    {
        this.fileHelper = fileHelper;
        this.directory = outputFolder;
        this.hasWritten = false;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void process(final CheckFlagEvent event)
    {
        final String challenge = event.getCheckFlag().getChallengeName()
                .orElse(event.getCheckName());

        final Vector<JsonObject> featureBucket = this.featureBuckets.computeIfAbsent(challenge,
                key -> new Vector<>(BUCKET_CAPACITY, BUCKET_INCREMENT));
        final ReadWriteLock bucketLock = this.bucketLocks.computeIfAbsent(challenge,
                key -> new ReentrantReadWriteLock());

        bucketLock.readLock().lock();
        try
        {
            featureBucket.add(event.toGeoJsonFeature());
        }
        finally
        {
            bucketLock.readLock().unlock();
        }

        final int batchSize = computeBatchSize();
        if (featureBucket.size() >= batchSize)
        {
            bucketLock.writeLock().lock();
            try
            {
                if (featureBucket.size() >= batchSize)
                {
                    this.write(challenge, featureBucket);
                }
            }
            finally
            {
                bucketLock.writeLock().unlock();
            }
        }
    }

    @Override
    @Subscribe
    public void process(final org.openstreetmap.atlas.event.ShutdownEvent event)
    {
        try
        {
            if (!this.featureBuckets.isEmpty())
            {
                this.featureBuckets.forEach(this::write);
            }
            else
            {
                this.write(CommonConstants.EMPTY_STRING, new Vector<>());
            }
        }
        catch (final Exception e)
        {
            logger.warn("CheckFlag geojson file write is failed.", e);
        }
    }

    /**
     * Overrides the computed batch size
     *
     * @param batchSizeOverride
     *            override batch size value
     * @return the {@link CheckFlagFileProcessor}
     */
    public CheckFlagGeoJsonProcessor withBatchSizeOverride(final int batchSizeOverride)
    {
        this.batchSizeOverride = batchSizeOverride;
        return this;
    }

    /**
     * Sets whether or not output files are compressed
     *
     * @param compress
     *            value to set
     * @return the {@link CheckFlagFileProcessor}
     */
    public CheckFlagGeoJsonProcessor withCompression(final boolean compress)
    {
        this.compressOutput = compress;
        return this;
    }

    /**
     * Returns bucket size based on the number of Checks we have bucketed so far
     *
     * @return batch size
     */
    protected int computeBatchSize()
    {
        if (this.batchSizeOverride > 0)
        {
            return this.batchSizeOverride;
        }
        if (this.featureBuckets.isEmpty())
        {
            return MAX_BATCH_SUM;
        }
        return Math.max(MAX_BATCH_SUM / this.featureBuckets.size(), MIN_BATCH_SIZE);
    }

    protected String getFilename(final String challenge, final int size)
    {
        return String.format("%s-%s-%s%s", challenge, new Date().getTime(), size,
                new GeoJsonPathFilter(this.compressOutput).getExtension());
    }

    /**
     * Writes a new file with the cached in String buffer
     */
    private void write(final String challenge, final Vector<JsonObject> featureBucket)
    {
        if (featureBucket.size() > 0)
        {
            final JsonObject featureCollection = new JsonObject();
            featureCollection.addProperty("type", "FeatureCollection");
            final JsonArray featureJsonArray = new JsonArray();
            featureBucket.forEach(featureJsonArray::add);
            featureCollection.add("features", featureJsonArray);
            this.fileHelper.write(this.directory,
                    this.getFilename(challenge, featureJsonArray.size()),
                    featureCollection.toString());
            this.hasWritten = true;
            featureBucket.clear();
        }
        else if (!this.hasWritten)
        {
            logger.warn("Writing empty file with no content in {}.", this.directory);
            this.fileHelper.write(this.directory,
                    String.format("%s%s", "empty",
                            new GeoJsonPathFilter(this.compressOutput).getExtension()),
                    CommonConstants.EMPTY_STRING);
        }
    }
}
