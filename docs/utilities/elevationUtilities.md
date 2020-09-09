# ElevationUtilities

#### Description
This is a general utilities class that allows checks to get elevation data. The class currently only understands HGT files, which by specification are 1 degree by 1 degree tiles. There is a [script](../../../scripts/elevationData) which can be used to get NASA SRTM elevation data (~90m accuracy throughout the world, some locations have no data).

#### Configuration

```json
{
    "ElevationUtilities": {
        "elevation.srtm_extent": 1.0 (degree),
        "elevation.srtm_ext": "hgt" (file extension),
        "elevation.path": "elevation"
    }
}
```
