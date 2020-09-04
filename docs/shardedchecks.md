# Sharded Atlas Checks

Sharded Atlas Checks is an implementation of the Atlas Checks framework that seeks to provide a memory efficient way to run large data sets. This is achieved using a "sharded" input schema and parallel processing. 

Normally when running Atlas Checks the size of input data is capped on a per country level by the amount of memory available. This means that even in a distributed environment enormous amounts of RAM are required to run countries like USA, RUS, and CHN. This implementation circumvents that cap by processing a country as multiple small sections. Each section is loaded as an individual process. Each process runs checks on the given section and returns a set of flags. Flags from all sections of a country are combined in the output. 

While the normal Atlas Checks job can run on either PBF or Atlas files, this job is restricted to specialized atlas files. These files should be in a countrified and sharded format. These files can be produced by using [Atlas Generator](https://github.com/osmlab/atlas-generator).

## How To Run

Sharded Checks is designed to be run in a Spark [cluster](cluster.md) for best performance, but can also be run as a standalone Java execution. The following details the parameters required for running the job in either environment, and parameters that are unique to Sharded Checks.

#### Main Class 
The main class of this job is org.openstreetmap.atlas.checks.distributed.ShardedIntegrityChecksSparkJob.

#### Input Data Path
Input Atlas files should be organized by country and sub-region (shard). The root of the input folder should contain sub-folders that are named by ISO3 county codes. Each sub-folder should contain atlas files named with the schema `<iso>_<zoom>_<x>_<y>.atlas`. An example of the full structure would be:
```text
- root
 | - USA  
 | - SGP
   | - SGP_10-806-508.atlas
   | - SGP_11-1614-1016.atlas
   | - ...
```

#### Expansion Distance
To maintain geographic completeness and avoid edge effects while running subsections of countries, each process is allowed to expand the area of a country that is loaded up to a set amount. The distance given in this parameter defines that expansion as shards within the given distance (in kilometers).

#### Sharding Schema
In order to load geographically connected shards together the job requires a definition of the sharding schema used for the input Atlas files. This can be supplied in 2 ways. A dynamic sharding definition can be supplied by placing a sharding.txt file in the input path. Alternatively, a schema can be provided using the `sharding` parameter. For more on this see the [sharding package](https://github.com/osmlab/atlas/tree/dev/src/main/java/org/openstreetmap/atlas/geography/sharding) in Atlas.

#### In Memory Atlas Type
By default Sharded Checks uses a [Dynamic Atlas](https://github.com/osmlab/atlas/tree/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/dynamic). It is also possible to use a [Multi Atlas](https://github.com/osmlab/atlas/tree/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/multi) to load Atlas files. This can be done by setting the `multiAtlas` parameter to `true`. It has been found that a Multi Atlas is the more performant in non-distributed environments.

#### Shared Arguments
The following are brief descriptions of the parameters that Sharded Atlas Checks shares with the normal job

| Parameter | Description |
|---|---|
| Output Path | Local or remote folder to save outputs to |
| Output Formats | Comma separated list of output types (flags,geojson,metrics,tippecanoe); MapRoulette output is not available in Sharded Checks |
| Countries | Comma separated list of ISO3 country codes of countries to run |
| Configuration File | Comma separated list of resource URIs for checks configuration json files |
| Cluster | Spark cluster URL (just `local` for local environments) |

## Limitations

#### Limited Input
Currently Sharded Checks only supports sharded Atlas files as an input data source. Eventually this may be expanded to include PBF files.

#### Large Relations
There is a known issue with large relations being flagged incorrectly. This can occur when a relation spans many shards and even at maximum expansion it cannot all be loaded. 


