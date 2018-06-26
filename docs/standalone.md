# Running Atlas Checks as a Standalone Application

For prerequisites for running checks see [dev.md](dev.md). To run the checks only the first two prerequisites are required, Java8 and Gradle. 

Atlas Checks can easily be executed using all the default settings with no additional requirements by simply executing 
the following statement in the root directory of the cloned Atlas Checks repository:

`gradle run`

This will run Atlas Checks against AIA (Anguilla) found in data/atlas/AIA using the [configuration.json](../config/configuration.json) configuration.

#### Running against new Atlas

Gradle will look inside this folder [data/](../data/) for any atlas or PBF files.  

#### Running against PBF

Atlas Checks allows you to run the checks against PBF files. If you have downloaded a PBF file you can simply
place the PBF in the [data/](../data/) directory and execute

`./gradlew run`

If you want to supply a location to a remote PBF you can use a project property in Gradle to set the PBF location
and execute

`gradle run -Pchecks.local.input=https://download.geofabrik.de/africa/south-africa-latest.osm.pbf`

#### Running against PBF with Bounding Box

Atlas Checks allows you to run checks against a PBF file with a bounding box restriction. This allows you 
to restrict the checks to a specific area of the map. Either for performance reasons, ie. less geographic
area allows Atlas Checks to complete faster, or simply for focus. Like running against a PBF, you can include
the PBF file using either a URL location or a local file, but in this case you would include the bounding
box as well.

`./gradlew run -Pchecks.local.input=https://download.geofabrik.de/africa/south-africa-latest.osm.pbf -Pchecks.local.pbfBoundingBox=lat,lon:lat,lon`

In the above case you would replace lat,lon:lat,lon with the actual bounds of your box.

#### Saving Intermediate Atlas File

When executing the Atlas Checks against PBF files, the framework will convert the PBF to an Atlas file and then
execute the checks over the Atlas file. Ordinarily the Atlas file will simply be used as an in-memory object and 
then garbage collected once we are finished with it. However it may be useful to save the Atlas for later usage, you
can do this by adding the `savePbfAtlas` flag to your gradle command, like so:

`./gradlew run -Pchecks.local.savePbfAtlas=true`

#### Output File Formats

Several types of output may be produced by the Atlas Check:
- Flag Logs (`flags`) - Line delimited GeoJson log files, each line the file representing one Check Flag consisting of a 
`FeatureCollection` of features flagged with additional information held within it's properties.
- Check GeoJson (`geojson`) - Each file contains a `FeatureCollection` containing a `Feature` per Atlas Check. This
format provides high level view of all geometries flagged by each check, useful for editing and visualization tools like
[JOSM](http://josm.openstreetmap.de/) and [geojson.io](http://geojson.io). Flagged features are represented as a
GeometryCollection within a GeoJson Feature that has aggregate information within it's properties.
- Check Metrics (`metrics`) - A comma-separated, line delimited file containing various metrics collected for each Atlas Check.

By default, all output formats are enabled. This can be changed by setting the `outputFormat` flag to a comma-separated list'
of desired formats, like so:

`./gradlew run -Pchecks.local.outputFormats=geojson,metrics`

#### Publish directly to MapRoulette

The standalone application for Atlas Checks can be published directly to MapRoulette using your personal MapRoulette
API Key. This can be easily done by including the following parameter during your gradle run.

`gradle run -Pchecks.local.maproulette=maproulette.org:80:HOME_12345:APIKey`

The MapRoulette configuration supplied is separated by colons and split into 4 different values which are:
1. maproulette.org - The hostname for the MapRoulette server that you are connecting too
2. 80 - The port for the MapRoulette server that you are connecting too
3. HOME_12345 - This would be the name of the project (any project) that you have access too and wish to store the check results in.
4. APIKey - Your personal APIKey from MapRoulette to allow access to upload checks.

#### Creating Compressed Output Files

Flag log and GeoJson files created while running the Atlas Checks are uncompressed by default. Changing this may be 
important when working with very large data sets. You can change this by adding the `compressOutput` flag to your gradle 
command, like so:

`gradle run -Pchecks.local.compressOutput=true`

#### Gradle Default Profiles

Gradle profiles can be found in this [gradle.properties](../gradle.properties) file. This contains the default profile used for setting project properties
for running Atlas Checks.

The default profile is used to set the application configuration (separate from the check configuration) for running
Atlas Checks locally. The following variables are set in the [gradle.properties](../gradle.properties) file.

- checks.local.input - This is the root directory for the atlas or PBF files
- checks.local.configFiles - The location of the config files.
- checks.local.output - The output location for any output produced by Atlas Checks.
- checks.local.outputFormat - Which output formats to produced.
- checks.local.startedFolder - A folder used for Spark to work in, this should generally remain unchanged.
- checks.local.countries - The country that is being used to run the checks over. Currently for local standalone this is limited to a single country.
- checks.local.master - This defines the master URL for the cluster, for running locally this should never be changed and remain "local".
- checks.local.sparkOptions - Any custom options that are subsequently sent to Spark. See [Spark Configuration](https://spark.apache.org/docs/1.6.0/configuration.html) for more information.
- checks.local.compressOutput - Whether to create compressed output files or not.

You will notice that some of these variables are similar to the variables that we used to pass in properties to our Gradle run.
They are the same property and can be changed either by modifying this file or by passing it into Gradle as a project property.
