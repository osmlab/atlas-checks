# Atlas Checks

[![Build Status](https://travis-ci.org/osmlab/atlas-checks.svg?branch=master)](https://travis-ci.org/osmlab/atlas-checks)

The Atlas Checks framework and standalone application are tools to enable quality assurance of Atlas data files. For more information on the Atlas mapping file format please see the [Atlas project](http://github.com/osmlab/atlas) in Github. 

## Starting with Atlas Checks

Please see the [contributing guidelines](https://github.com/osmlab/atlas/blob/dev/CONTRIBUTING.md)!

### Requirements
To run Atlas Checks the following is required:
1. [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. [Gradle](https://gradle.org/install)
3. [Git Command Line Tools](https://git-scm.com/downloads)

### Run Atlas Checks
To start working with Checks follow the steps below:
1. Clone Atlas Checks project using following command `git clone https://github.com/osmlab/atlas-checks.git`
2. Switch to newly created directory: `cd atlas-checks`
3. Execute `gradle run`

This command will build and run Atlas Checks with all the default options against the country Anguilla. GeoJSON output will be be produced that contains all the results found from the run. For more information on running Atlas Checks as a standalone application click [here](docs/standalone.md).

## Working with Configuration
See [configuration docs](docs/configuration.md) for more information about the configuration files that can be used to define specific details around the Atlas Checks application.

## Running Atlas Checks in Spark Cluster
Atlas Checks have been developed to take advantage of distributed computing by running the checks in Spark. For more information on Spark see [spark.apache.org](http://spark.apache.org/). Running Atlas Checks locally is already executed within a local Spark environment on your machine, so running Spark in a cluster is simply a matter of updating the configuration. For more information see [Running Atlas Checks in a Spark Cluster](docs/cluster.md)

## Developing your own Atlas Checks
See [Development docs](docs/dev.md) for more information about developing and best practices for new Atlas Checks.

## Currently Available Checks

- [PoolSizeCheck](docs/tutorials/tutorial1-PoolSizeCheck.md)
- [BuildingRoadIntersectionCheck](docs/checks/buildingRoadIntersectionCheck.md)
- [SelfIntersectingPolylineCheck](docs/checks/selfIntersectingPolylineCheck.md)
- [FloatingEdgeCheck](docs/checks/floatingEdgeCheck.md)
- [RoundAboutClosedLoopCheck](docs/checks/roundaboutClosedLoopCheck.md)
- [SharpAngleCheck](docs/checks/sharpAngleCheck.md)
- [SinkIslandCheck](docs/tutorials/tutorial3-SinkIslandCheck.md)
- [SnakeRoadCheck](docs/checks/snakeRoadCheck.md)
- [DuplicateNodeCheck](docs/checks/duplicateNodeCheck.md)
- [OrphanNodeCheck](docs/tutorials/tutorial2-OrphanNodeCheck.md)
