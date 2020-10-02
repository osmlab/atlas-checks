# MapRoulette Upload Command

The MapRouletteUploadCommand deserilizes line-delimited Atlas Check flags, and uploads them to the provided MapRoulette server.

The command create a new Challenge for each Atlas Check and country in the input data. For example: If the input data contains flags for EdgeCrossingEdgeCheck and SinkIslandCHeck in USA and CAN, the following Challenges are created:

1. CAN - EdgeCrossingEdgeCheck
2. CAN - SinkIslandCheck
2. USA - EdgeCrossingEdgeCheck
3. USA - SinkIslandCheck

Each Challenge is added to the project provided in the MapRoulette connection url.

## Parameters

* **maproulette** - MapRoulette connection url
* **logfiles** - Path to `/flag` output geojson
* **config** - Path to Atlas Checks configuration
* **countries** (optional) - Comma delimited list of country permit list
* **checks** (optional) - Comma delimted list of checks permit list
* **outputPath** (optional) - Path to store text file of new challenge ids
* **checkinComment** (optional) - Value of Changeset check in comment
* **checkinCommentPrefix** - Prefix added to default check in comment of [prefix ISO - CheckName]

## Example

The following command will upload EdgeCrossingEdge & SinkIsland checks to the `example_project` Project on staging.maproulette.org. Please remember to change the MapRoulette API key. You can find this in your MapRoulette user settings.

```java
java -classpath /jar/path/atlas-checks-6.1.2.-fat.jar
org.openstreetmap.atlas.checks.maproulette.MapRouletteUploadCommand
  -maproulette="https://staging.maproulette.org:443:example_project:api_key"
  -logfiles=/path/to/flag
  -outputPath=/path/to/challenge/outputfile
  -config=/path/to/checks/config.json
  -checkinComment=#AtlasChecks
  -countries=USA,CAN
  -checks=EdgeCrossingEdgeCheck,SinkIslandCheck
```






