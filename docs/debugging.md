# Debugging Atlas Checks

### Overview

When developing an Atlas Checks, it is very important to be able to debug your checks to understand what they are doing or potentially where your check is not doing what you would expect it to do. The document describes how you would debug Atlas Checks using the Intellij IDE by JetBrains. There are various IDE's out there that perform similar debugging functions, namely an IDE like Eclipse, and transferring the instructions from Intellij to your IDE of choice should be fairly straight forward. This document should give you the basics for whatever IDE you wish to use.

### Setup

The first thing required is to import the project into Intellij. Intellij supports gradle projects which is what Atlas-Checks is, so importing the project is fairly straight forward. These instructions assumed 

1. Clone the Atlas-Checks project into a directory of your choice 
2. Click on "File" -> "New" -> "Project from Existing Sources..."
3. Select the folder that you original cloned your Atlas-Checks project into in Step 1.
4. Select the "Import project from external model" and then select the "Gradle" option
5. In the final screen check the box "Use auto-import" and make sure that "Use default gradle wrapper (recommended)" is selected.
6. Click Finish and wait for gradle tasks to complete.

This will create a new Atlas-Checks project that you can start developing in Intellij. Most other full fledged IDEs should following a similar path. Smaller lightweight IDEs will simply have you point to a directory.

### Creating Configuration

In Intellij, a configuration is used to run a project. With the setup above you will be able to build the project directly from within IDE but we need to setup a configuration to actually run and debug it. We won't be running it as mentioned in other documents using the gradle task `gradle run`, we will be running it directly against the main class. Essentially diving into what `gradle run` hides from the user. 

1. Click on the drop down bar in the toolbar, it should currently be empty. If you are not sure where this is you can also go to File Menu and click on "run" and then select the "run" option in the drop down.

    - If you click on the drop down bar, it will drop down an "edit configurations" option that you must click.
    - If you went through the file menu, then a small dialog will pop up, click on the "edit configurations" option.
    
2. In the "Run/Debug Configurations" dialog box click on the + sign in the top left hand corner of the dialog.
3. Select the "Application" option
4. In the pane on the right that now contains the new Application update the following.

       - Change the name to something like 'Atlas-Checks'       
       - Change the main class to `org.openstreetmap.atlas.checks.distributed.IntegrityCheckSparkJob`       
       - Optionally include VM Options: -Xms2048m -Xmx10240m -XX:MaxPermSize=4096m, this will help with larger atlas files.
       - Update the working directory to be the current directory of your Atlas-Checks project.
       - Set "Use of classpath module" to `atlas-checks_main`
       - Include the following parameter template in "Program Arguments". Replace explanation with actual value.
   
            - inputFolder=[Folder pointing to location of country folders with atlas files]
            - startedFolder=[Output directory for Spark, can be any directory you create]
            - output=[Output directory for results of job]
            - countries=[ISO3 country code comma separated list]
            - maproulette=[Maproulette configuration in format "server:host:project:api_key"]
            - saveCheckOutput=false
            - master=local
            - configFiles=[Points to the configuration file, should be something like file:/atlas_checks_root_dir/config/configuration.json]
            - sparkOptions=spark.executor.memory->4g,spark.driver.memory->4g,spark.rdd.compress->true
            
A couple of notes about the program arguments.

1. SparkOptions should generally not be changed, but if you require more memory for either driver or workers then you can update it. For more information about Spark options see [here](http://spark.apache.org/docs/1.6.0/configuration.html).
2. This can be used to deploy your jobs to a Spark cluster, that information you can find [here](cluster.md)
3. The `gradle run` task hides the structure of the atlas files, however in the background the files are separated by country into ISO3 country folders. So unlike using the gradle task you need to have the actual atlas files and place them in the correct folder structure for this to work, an example folder structure would be like below:

```
    - Root Folder
        - ABC
            - ABC_7-41-57.atlas
        - XYZ
            - XYZ_10-806-508.atlas
            - XYZ_11-1614-1016.atlas 
            - XYZ_11-1614-1017.atlas 
            - XYZ_11-1615-1016.atlas 
            - XYZ_7-101-63.atlas
            - XYZ_8-201-126.atlas
```
Where ABC and XYZ are two different countries.

### Running/Debugging Atlas Checks

At this point running Atlas-Checks is as simple as either clicking on the play button on the taskbar or clicking on "Run" -> "Run..." in the file menu. Similarly with debugging you can either click on the little bug icon (usually next to the play button) on the taskbar or clicking  "Run" -> "Debug..." in the file menu. The code will run and the code will break at any breakpoints that you place in the code.
