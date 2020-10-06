# MapRoulette Upload Command

The [MapRouletteUploadCommand](/src/main/java/org/openstreetmap/atlas/checks/maproulette/MapRouletteUploadCommand.java) deserilizes line-delimited Atlas Check flags, and uploads them to the provided MapRoulette server.

The command creates a new Challenge for each Atlas Check and country in the input data. For example: the following Challenges are created if the input data contains flags for EdgeCrossingEdgeCheck and SinkIslandCheck in USA and CAN:

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
* **checkinCommentPrefix** (optional) - Prefix added to default check in comment of [prefix ISO - CheckName]

## Example

The following command will upload EdgeCrossingEdge & SinkIsland checks to the `checks_example_project` Project on staging.maproulette.org. 
Please remember to change the MapRoulette API key. You can find this in your MapRoulette user settings.

```java
java -cp build/libs/atlas-checks-6.1.3-SNAPSHOT-shaded.jar org.openstreetmap.atlas.checks.maproulette.MapRouletteUploadCommand \
-maproulette="https://staging.maproulette.org:443:checks_example_project:api|key" \
-logfiles=build/example/data/output/flag \
-outputPath=build/example \
-config=config/configuration.json \
-checkinComment="#AtlasChecks" \
-countries=USA \
-checks=EdgeCrossingEdgeCheck,SinkIslandCheck
```

A `challenges.txt` file will write to the provided `outputPath`. The contents of this file will look like:
```
project:3646;challenge:4181
project:3646;challenge:4182
```

Here's the project in MapRoulette:

![Example Checks MapRoulette Project](/docs/images/example_mr_project.png)
