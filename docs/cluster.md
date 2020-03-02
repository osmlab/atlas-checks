# Running Atlas Checks in a Spark Cluster

If you work requires you to run Atlas checks for a large area, or even the whole planet, to finish it in a reasonable amount of time, 
you should consider using a decent sized Spark cluster. 

### Prerequisites

- Generated Atlas files: please refer to [Atlas-generator](https://github.com/osmlab/atlas-generator) project on how to create osm atlas files from OSM BPF shards.
- A distributed file system to host atlas files, e.g., HDFS, S3. 

the folder structure for sharded atlas files should be organized by country/region, with the sub-folder named with country code ISO3 standard, something looks like this:
```text
- root
 | - USA  
 | - SGP
   | - SGP_10-806-508.atlas
   | - SGP_11-1614-1016.atlas
   | - ...
```

### Building deployment jar

Atlas-checks uses `gradle` for all the build tasks. 

```bash
cd $PROJECT_HOME # replace with the path where you cloned the project.
./gradlew clean build
```

After the command runs into completion, a set of jar files will be generated under `$PROJECT_HOME/build/libs` folder.
To submit jobs to a cluster, we will be using the shaded jar built by `shaded` task, with the name `atlas-checks-$version-SNAPSHOT-shaded.jar`. 
The shaded jar excluded dependencies from Spark & Hadoop, and is to be used to copy to executor nodes. 
More details for building process could be found out by checking the `build.gradle` file.


### Configuration

atlas-checks takes a JSON formatted configuration file to control the settings for all the checks to run.  You could get an example from here: 
[sample configuration file](../config/configuration.json).

When running atlas-checks in a spark cluster, you would want to have the config file stored somewhere accessible to all your executor nodes.
`http`,`s3` and `HDFS` are the formats currently supported. 


### Submit your spark job

The following is a sample command to submit an atlas check spark job.
```bash
spark-submit \
  --class org.openstreetmap.atlas.checks.distributed.IntegrityCheckSparkJob \
  --master spark://localhost:7077  # here replace with your spark master url
  --deploy-mode cluster \
  --conf spark.driver.userClassPathFirst=true \
  --conf spark.executor.userClassPathFirst=true \
  --conf spark.io.compression.codec=lz4 \
  --executor-memory 8G \
  --total-executor-cores 4 \
  build/libs/atlas-checks-$version-SNAPSHOT-shaded.jar \   # here replace with the path to the shaded jar you just built
  -input="file:///Users/xyz/data_dir/atlas" \  # here replace with the path to atlas files, to one level up to country folders
  -output=spark_output \  # here replace with a shared folder path 
  -countries=SGP \  # replace with the list of countries you'd like to run, separated by comma.
  -startedFolder=spark_start \  # here replace with a shared folder path 
  -configFiles="http:///config.atlas-check.org/4.0.3/atlas-checks-conf.json"  # this is a fake url, replace with the url to your configuration file.

```

**remember to replace all example values and comments before you run the command in your environment.**

Application parameters:

| Parameter     | Description       | Example      |
| ------------- | ----------------- | -------------|
| input         | input folder path, expecting sub directories organized by country code iso3 | blob://atlas_data/test_atlas |
| output        | output folder path, check results will be generated here      |   blob://atlas_data/atlas_check_result  |
| startedFolder | a place to write job status files    |   blob://atlas_data/started |
| countries     | comma separated, ISO3 country code to specify the countries to run check against.     |  SGP,CYM  |
| configFiles   | url to configuration file(s), if multiple, separated by comma.   |    http:///config.atlas-check.org/4.0.3/atlas-checks-conf.json |




### Caveats

#### Dependency hell is real!

To get the job running against a different version of Spark could be very challenging. Right now it's only been tested against Spark version 1.6.0-cdh5.7.0

If you see an exception while running in your environment, like `ClassNotFoundException`,  `ClassCastException`, thrown from a standard library, such as Guava, Slf4j, 
then you are hitting the dependency conflict problem.   To solve it, try to figure out the name of the conflicted library, then update dependency configuration in `build.gradle` file to force a working version. 

#### Large country support

By default atlas-check run jobs by country. This means one worker node will have to be able to load all the data for one country in memory. 
This is not a good way to distribute workload and could make it very challenging to run large countries like USA, RUS, or CHN. 
For running large countries see [Sharded Checks](shardedchecks.md).
