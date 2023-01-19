#Fix Suggestions

Fix suggestions are a way for atlas checks to recommend a remedy for an issue, as opposed to just flagging the issue. 
These suggestions can be given to editors to help them resolve the issues. 

## Creating a Fix Suggestion

Fix suggestions are created as [FeatureChanges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/change/FeatureChange.java) that are added to the CheckFlag.
FeatureChanges are part of the [ChangeAtlas](https://github.com/osmlab/atlas/tree/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/change) system, which is used in the Atlas generation process. 

To create a FeatureChange for a fix suggestion you first need to make a [CompleteEntity](https://github.com/osmlab/atlas/tree/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/complete) for the AtlasEntity you want to have the suggestion for. 
A CompleteEntity is an AtlasEntity that is self-contained, and is not require to be part of a larger Atlas.
There are CompleteEntities of each AtlasEntity type, CompleteArea, CompleteNode, etc. 
A CompleteEntity is created from an AtlasEntity by using the associated CorrectEntity subclass's `from` method (i.e. `CompleteEdge.from()`).

Once you have a CompleteEntity for your feature, you then need to "fix" that CompleteEntity. 
In this case that means altering the tags, geometry, or other attributes of the CompleteEntity to resolve the issue for which it is flagged.
This is done using the various methods of the CompleteEntity, such as `withAddedTag` or `withPolyline`. 
If you want to suggest deleting a feature then you cna skip this step. 

After making changes to the CompleteEntity it is added to the FeatureChange using `FeatureChange.add()` method. 
If you are suggestion the feature be deleted then use `FeatureChange.remove()`.
The FeatureChange can then be added to the CheckFlag using either the `addFixSuggestion` or `addFixSuggestions` methods.

## Using Fix Suggestions

### Fix Suggestion Outputs

Fix suggestions appear in both the flag and geojson logs. 
In both they appear as an object under the key fix_suggestions, however, 
in the flag logs this key is found in the root object of the line and in the geojson it is under the properties of each feature collection. 
The fix_suggestions object contains elements whose keys are unique feature IDs. 
Each element contains a description of the changes made to a feature.

Here is an example log output containing a fix suggestions for 4 Edges that make up one Way:
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [
            55.4535187,
            25.3628649
          ],
          [
            55.4535376,
            25.3627203
          ]
        ]
      },
      "properties": {
        "area": "yes",
        "last_edit_user_name": "CartographicChels",
        "last_edit_changeset": "70291534",
        "identifier": "209331363000004",
        "itemType": "Edge",
        "last_edit_time": "1557974395000",
        "last_edit_user_id": "9051059",
        "iso_country_code": "ARE",
        "osmIdentifier": "209331363",
        "highway": "footway",
        "last_edit_version": "2"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [
            55.4535376,
            25.3627203
          ],
          [
            55.4536967,
            25.3627287
          ]
        ]
      },
      "properties": {
        "area": "yes",
        "last_edit_user_name": "CartographicChels",
        "last_edit_changeset": "70291534",
        "identifier": "209331363000001",
        "itemType": "Edge",
        "last_edit_time": "1557974395000",
        "last_edit_user_id": "9051059",
        "iso_country_code": "ARE",
        "osmIdentifier": "209331363",
        "highway": "footway",
        "last_edit_version": "2"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [
            55.4536967,
            25.3627287
          ],
          [
            55.4536866,
            25.3628827
          ]
        ]
      },
      "properties": {
        "area": "yes",
        "last_edit_user_name": "CartographicChels",
        "last_edit_changeset": "70291534",
        "identifier": "209331363000002",
        "itemType": "Edge",
        "last_edit_time": "1557974395000",
        "last_edit_user_id": "9051059",
        "iso_country_code": "ARE",
        "osmIdentifier": "209331363",
        "highway": "footway",
        "last_edit_version": "2"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [
            55.4536866,
            25.3628827
          ],
          [
            55.4535187,
            25.3628649
          ]
        ]
      },
      "properties": {
        "area": "yes",
        "last_edit_user_name": "CartographicChels",
        "last_edit_changeset": "70291534",
        "identifier": "209331363000003",
        "itemType": "Edge",
        "last_edit_time": "1557974395000",
        "last_edit_user_id": "9051059",
        "iso_country_code": "ARE",
        "osmIdentifier": "209331363",
        "highway": "footway",
        "last_edit_version": "2"
      }
    }
  ],
  "properties": {
    "id": "209331363000001209331363000002209331363000003209331363000004",
    "instructions": "1. The way with OSM ID 209331363 has a highway value of FOOTWAY, which should not have an area=yes tag. Consider changing this to highway=PEDESTRIAN.",
    "identifiers": [
      "Edge209331363000002",
      "Edge209331363000001",
      "Edge209331363000004",
      "Edge209331363000003"
    ],
    "generator": "AreasWithHighwayTagCheck",
    "timestamp": "Tue Sep 15 12:50:30 PDT 2020"
  },
  "fix_suggestions": {
    "Edge209331363000003": {
      "type": "UPDATE",
      "descriptors": [
        {
          "name": "TAG",
          "type": "UPDATE",
          "key": "highway",
          "value": "pedestrian",
          "originalValue": "footway"
        }
      ]
    },
    "Edge209331363000002": {
      "type": "UPDATE",
      "descriptors": [
        {
          "name": "TAG",
          "type": "UPDATE",
          "key": "highway",
          "value": "pedestrian",
          "originalValue": "footway"
        }
      ]
    },
    "Edge209331363000001": {
      "type": "UPDATE",
      "descriptors": [
        {
          "name": "TAG",
          "type": "UPDATE",
          "key": "highway",
          "value": "pedestrian",
          "originalValue": "footway"
        }
      ]
    },
    "Edge209331363000004": {
      "type": "UPDATE",
      "descriptors": [
        {
          "name": "TAG",
          "type": "UPDATE",
          "key": "highway",
          "value": "pedestrian",
          "originalValue": "footway"
        }
      ]
    }
  }
}
```

### MapRoulette Integration

Fix suggestions can be used to create [Cooperative Challenges](https://learn.maproulette.org/documentation/creating-cooperative-challenges/) in MapRoulette. 
Cooperative Challenges present an uncommitted edit to the editor, that they can then approve or not. 
The [MapRoulette Upload Command](maproulette_upload.md) can turn fix suggestions into Cooperative Challenges automatically.
All that is needed it to have flag files that contain fix suggestions and to set `-includeFixSuggestions=true` in the command arguments.
Currently, this system only works for tag based changes. 
It is planned that geometry and relation member changes will be supported in the future. 
