# Atlas Checks

[![Build Status](https://travis-ci.org/osmlab/atlas-checks.svg?branch=main)](https://travis-ci.org/osmlab/atlas-checks)
[![quality gate](https://sonarcloud.io/api/project_badges/measure?project=org.openstreetmap.atlas%3Aatlas-checks&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.openstreetmap.atlas%3Aatlas-checks)
[![Maven Central](https://img.shields.io/maven-central/v/org.openstreetmap.atlas/atlas-checks.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.openstreetmap.atlas%22%20AND%20a:%22atlas-checks%22)
[![CircleCI](https://circleci.com/gh/osmlab/atlas-checks/tree/main.svg?style=svg)](https://circleci.com/gh/osmlab/atlas-checks/tree/main)

---

The Atlas Checks framework and standalone application are tools to enable quality assurance of Atlas data files. For more information on the Atlas mapping file format please see the [Atlas project](http://github.com/osmlab/atlas) in Github.

## Starting with Atlas Checks

Please see the [contributing guidelines](https://github.com/osmlab/atlas/blob/dev/CONTRIBUTING.md)!

### Requirements
To run Atlas Checks the following is required:
1. [OpenJDK 11](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.5%2B10/OpenJDK11U-jdk_x64_mac_hotspot_11.0.5_10.pkg)
2. [Gradle](https://gradle.org/install)
3. [Git Command Line Tools](https://git-scm.com/downloads)

### Run Atlas Checks
To start working with Atlas Checks follow the steps below:
1. Clone Atlas Checks project using the following command `git clone https://github.com/osmlab/atlas-checks.git`
2. Switch to newly created directory: `cd atlas-checks`
3. Execute `./gradlew run`

This command will build and run Atlas Checks with all the default options against a sample Atlases of Belize downloaded from [here](https://www.dropbox.com/sh/54aqfbs12suqd9t/AAC8bpVWCgGLY-SZ30XKIY92a/atlas/BLZ). GeoJSON output will be produced that contains all the results found from the run. Those outputs will be found in `atlas-checks/build/examples/data/output`. For more information on running Atlas Checks as a standalone application click [here](docs/standalone.md).

## Working with Configuration
See [configuration docs](docs/configuration.md) for more information about the configuration files that can be used to define specific details around the Atlas Checks application.

## Running Atlas Checks in Spark Cluster
Atlas Checks have been developed to take advantage of distributed computing by running the checks in Spark. For more information on Spark see [spark.apache.org](http://spark.apache.org/). Running Atlas Checks locally is already executed within a local Spark environment on your machine, so running Spark in a cluster is simply a matter of updating the configuration. For more information see [Running Atlas Checks in a Spark Cluster](docs/cluster.md)

## Developing your own Atlas Checks
See [Development docs](docs/dev.md) for more information about developing and best practices for new Atlas Checks.

## Currently Available Checks
A document with a list of tables for [Available checks](docs/available_checks.md) is available and includes descriptions and links to documentation for each check.  Each table is organized by check type.
