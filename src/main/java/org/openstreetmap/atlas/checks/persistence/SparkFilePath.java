package org.openstreetmap.atlas.checks.persistence;

import java.io.Serializable;

/**
 * A helper class to handle temporary and target paths
 *
 * @author mkalender
 */
public final class SparkFilePath implements Serializable
{
    private static final long serialVersionUID = -7976178731336901454L;

    private final String temporaryPath;
    private final String targetPath;

    /**
     * Default constructor
     *
     * @param temporaryPath
     *            a temporary file path
     * @param targetPath
     *            the target file path
     */
    public SparkFilePath(final String temporaryPath, final String targetPath)
    {
        this.temporaryPath = temporaryPath;
        this.targetPath = targetPath;
    }

    /**
     * @return the target file path
     */
    public String getTargetPath()
    {
        return this.targetPath;
    }

    /**
     * @return the temporary file path
     */
    public String getTemporaryPath()
    {
        return this.temporaryPath;
    }

    @Override
    public String toString()
    {
        return String.format("{target: %s, temporary: %s}", this.getTargetPath(),
                this.getTemporaryPath());
    }
}
