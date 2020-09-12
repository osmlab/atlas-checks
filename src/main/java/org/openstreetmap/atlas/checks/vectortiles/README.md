## Generating tippecanoe line-delimited GeoJSON

First, you need to generate the tippecanoe line-delimited GeoJSON with `IntegrityCheckSparkJob`.

To get that output, make sure you have `tippecanoe` in your `outputFormats` argument:

``` 
-outputFormats=flags,geojson,metrics,tippecanoe
```

Then, the output will be in a tippecanoe subdirectory in your output directory.

``` 
-output=/Users/n/code/atlas-checks/spark/
```

Example command:

``` 
java -Xmx12G -cp ./atlas-checks.jar org.openstreetmap.atlas.checks.distributed.IntegrityCheckSparkJob \
-inputFolder=/Users/n/code/atlas-checks/data \
-startedFolder=/Users/n/code/atlas-checks/spark \
-output=/Users/n/code/atlas-checks/spark/ \
-outputFormats=flags,geojson,metrics,tippecanoe \
-compressOutput=false \
-countries=ECU \
-saveCheckOutput=true \
-cluster=local \
-configFiles=file:/Users/n/code/atlas-checks/config/configuration.json \
-sparkOptions=spark.executor.memory->4g,spark.driver.memory->4g,spark.rdd.compress->true
```

For more details on how to work with viewing and creating vector tiles, see:

https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/utilities/vectortiles/README.md


# TippecanoeConverter

Converts tippecanoe styled line-delimited GeoJSON into vector tiles using tippecanoe.

This CLI is part of the core atlas project, and you want to run this to take your line-delimited GeoJSON of atlas 
checks into an MBTiles file with tippecanoe. Of course, you can do this directly with tippecanoe, but this is a 
convenient wrapper tool. Try this first : ).

Example command:

``` 
java -Xmx12G -cp ./atlas-checks.jar org.openstreetmap.atlas.utilities.vectortiles.TippecanoeConverter \
-geojsonDirectory=/Users/n/code/atlas-checks/spark/tippecanoe \
-overwrite=true \
-mbtiles=/Users/n/code/atlas-checks/spark/checks.mbtiles
```

The `geojsonDirectory` is the location where you have generated tippecanoe line-delimited GeoJSON output of your 
atlas checks. 

Setting `overwrite` to true lets tippecanoe overwrite an existing MBTiles file; this is optional, and having this 
true removes the safety of preventing overwriting a previous MBTiles file. 

`mbtiles` is the path to write the MBTiles file.

You can configure more details about your GeoJSON and tile generation in [TippecanoeCheckSettings](https://github.com/hallahan/atlas-checks/blob/dev/src/main/java/org/openstreetmap/atlas/checks/vectortiles/TippecanoeCheckSettings.java).
The important part is the JSON mutator. Here you can set up what layer and zoom levels you would like your data to 
be in the vector tiles. The default mutator provided should be fine for most purposes, however, this is where you
can go and adjust the minimum zooms for the check flags and flag features.
