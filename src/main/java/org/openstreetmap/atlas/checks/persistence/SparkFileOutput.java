package org.openstreetmap.atlas.checks.persistence;

import java.io.Serializable;
import java.util.function.Consumer;

import org.openstreetmap.atlas.generator.tools.spark.SparkJob;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

/**
 * Helper class to hide details of output operations for {@link SparkJob}s
 *
 * @author mkalender
 */
public final class SparkFileOutput implements Serializable
{
    private static final long serialVersionUID = 4719291632105671724L;

    private final Consumer<WritableResource> saveFunction;
    private final SparkFilePath path;
    private final String operationName;

    /**
     * Constructs a {@link SparkFileOutput} from given parameters
     *
     * @param saveFunction
     *            save function that is going to be executed
     * @param parentFolder
     *            parent folder
     * @param targetFolder
     *            target folder
     * @param folder
     *            sub-folder
     * @param fileName
     *            file name
     * @param extension
     *            file extension
     * @param operationName
     *            name of the output operation
     * @return a new {@link SparkFileOutput}
     */
    public static SparkFileOutput from(final Consumer<WritableResource> saveFunction,
            final String parentFolder, final String targetFolder, final String folder,
            final String fileName, final String extension, final String operationName)
    {
        return new SparkFileOutput(saveFunction,
                getFilePath(targetFolder, parentFolder, folder, fileName, extension),
                operationName);
    }

    /**
     * Constructs a {@link SparkFileOutput} from given parameters
     *
     * @param saveFunction
     *            save function that is going to be executed
     * @param folderPath
     *            {@link SparkFilePath} with folder paths
     * @param fileName
     *            file name
     * @param extension
     *            file extension
     * @param operationName
     *            name of the output operation
     * @return a new {@link SparkFileOutput}
     */
    public static SparkFileOutput from(final Consumer<WritableResource> saveFunction,
            final SparkFilePath folderPath, final String fileName, final String extension,
            final String operationName)
    {
        final String fileNameWithExtension = fileName
                + SparkFileHelper.extensionStartingWithSeparator(extension);
        final SparkFilePath filePath = new SparkFilePath(
                SparkFileHelper.combine(folderPath.getTemporaryPath(), fileNameWithExtension),
                SparkFileHelper.combine(folderPath.getTargetPath(), fileNameWithExtension));

        return new SparkFileOutput(saveFunction, filePath, operationName);
    }

    /**
     * Calculates file path with given path parameters. System library is not being utilized, since
     * path is sometimes a local file path, sometimes a URI.
     *
     * @param targetFolder
     *            target folder
     * @param parentFolder
     *            parent folder
     * @param folder
     *            folder
     * @param fileName
     *            file name
     * @param extension
     *            file extension
     * @return a new {@SparkFilePath}
     */
    private static SparkFilePath getFilePath(final String targetFolder, final String parentFolder,
            final String folder, final String fileName, final String extension)
    {
        final String fileNameWithFolderAndExtension = SparkFileHelper.combine(folder,
                fileName + SparkFileHelper.extensionStartingWithSeparator(extension));

        return new SparkFilePath(
                SparkFileHelper.combine(parentFolder, fileNameWithFolderAndExtension),
                SparkFileHelper.combine(targetFolder, fileNameWithFolderAndExtension));
    }

    private SparkFileOutput(final Consumer<WritableResource> saveFunc, final SparkFilePath path,
            final String operationName)
    {
        this.saveFunction = saveFunc;
        this.path = path;
        this.operationName = operationName;
    }

    /**
     * @return the operation name
     */
    public String getOperationName()
    {
        return this.operationName;
    }

    /**
     * @return the path
     */
    public SparkFilePath getPath()
    {
        return this.path;
    }

    /**
     * @return the save function
     */
    public Consumer<WritableResource> getSaveFunction()
    {
        return this.saveFunction;
    }

}
