package org.openstreetmap.atlas.checks.event;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.openstreetmap.atlas.checks.distributed.LogFilePathFilter;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A useful base class to handle common functionality for file processors.
 *
 * @author mkalender
 * @param <T>
 *            type that is going to be processed
 */
public abstract class FileProcessor<T extends Event> implements Processor<T>
{
    // Default number of events to batch
    public static final int BATCH_SIZE = 2500;
    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    // File helper to write files
    private final SparkFileHelper fileHelper;

    // Directory to write files in
    private final String directory;

    // Buffer to hold events generated so far
    private final StringBuffer buffer;

    // Number of events to be batched
    private int batchSize = BATCH_SIZE;

    // Thread safe counter
    private final AtomicInteger counter;

    // A lock useful to handle synchronization when we write files
    private final ReadWriteLock fileLock = new ReentrantReadWriteLock();

    // Whether or not to compress output file
    private boolean compressOutput = true;

    /**
     * Default constructor
     *
     * @param fileHelper
     *            {@link SparkFileHelper} instance for I/O operations
     * @param directory
     *            directory path to write files to
     */
    public FileProcessor(final SparkFileHelper fileHelper, final String directory)
    {
        this.fileHelper = fileHelper;
        this.directory = directory;
        this.buffer = new StringBuffer();
        this.counter = new AtomicInteger(0);
    }

    /**
     * @return the maximum number of events to be batched in a file
     */
    public int getBatchSize()
    {
        return this.batchSize;
    }

    /**
     * @return the number of events processed in the current batch
     */
    public final int getCount()
    {
        return this.counter.get();
    }

    /**
     * Processes given String and writes batched events into a file if needed
     *
     * @param event
     *            a character set to process
     */
    public void process(final String event)
    {
        // Process new event
        this.fileLock.readLock().lock();

        try
        {
            this.buffer.append(String.format("%s%s", event, System.lineSeparator()));
            this.counter.incrementAndGet();
        }
        catch (final Exception e)
        {
            logger.warn("Event processing is failed.", e);
        }
        finally
        {
            this.fileLock.readLock().unlock();
        }

        // Write batched events to a file if needed
        if (this.counter.get() >= this.getBatchSize())
        {
            this.fileLock.writeLock().lock();

            try
            {
                if (this.getCount() >= this.getBatchSize())
                {
                    this.write();
                }
            }
            catch (final Exception e)
            {
                logger.warn("File write is failed.", e);
            }
            finally
            {
                this.fileLock.writeLock().unlock();
            }
        }
    }

    /**
     * Sets batch size to given value
     *
     * @param batchSize
     *            New batch size
     */
    public void setBatchSize(final int batchSize)
    {
        this.batchSize = batchSize;
    }

    /**
     * Sets whether or not output files are compressed
     *
     * @param compress
     *            value to set
     * @return the {@link FileProcessor}
     */
    public FileProcessor<T> withCompression(final boolean compress)
    {
        this.compressOutput = compress;
        return this;
    }

    /**
     * @return the name of the file to be used in {@code #write()} method to write files
     */
    protected String getFilename()
    {
        return String.format("%s-%s%s", new Date().getTime(), this.getCount(),
                new LogFilePathFilter(this.compressOutput).getExtension());
    }

    /**
     * Writes a new file with the cached in String buffer
     */
    protected void write()
    {
        final int count = this.getCount();
        if (count == 0)
        {
            logger.warn("Writing empty file with no content in {}.", this.directory);
        }

        this.fileHelper.write(this.directory, this.getFilename(), this.buffer.toString());
        this.buffer.delete(0, this.buffer.length());
        this.counter.set(0);
    }
}
