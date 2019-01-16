# Atlas Check Configuration

Configuration for Atlas Checks uses a HOCON (Human Optimized Config Object Notation) style JSON configuration file.
 This allows you to configure various settings for the checks you want to execute without having to modify code
 to see if it works. An example configuration file would look something like this.
 
 ```json
 {
   "FloatingEdgeCheck": {
     "length": {
       "maximum.kilometers": 100.0,
       "minimum.meters": 100.0
     },
     "challenge": {
       "description": "Tasks contain ways that are disconnected from the road network.",
       "blurb": "Connected Edges",
       "instruction": "Open your favorite editor and remove disconnected edges.",
       "difficulty": "EASY",
       "defaultPriority": "LOW",
       "highPriorityRule": {
         "condition":"OR",
         "rules":["highway=motorway","highway=motorway_link","highway=trunk","highway=trunk_link"]
       },
       "mediumPriorityRule": {
         "condition":"OR",
         "rules":["highway=primary","highway=primary_link","highway=secondary","highway=secondary_link"]
       }
     }
   },
   "SelfIntersectingPolylineCheck": {
     "tags.filter": "highway->*|waterway->*|building->*",
     "challenge": {
       "description": "Verify that the same Polyline does not intersect itself at any point.",
       "blurb": "Modify Polylines such that they do not self intersect.",
       "instruction": "Open your favorite editor and move the polylines such that they are not self intersecting anymore.",
       "difficulty": "NORMAL",
       "defaultPriority": "LOW",
       "highPriorityRule": {
         "condition":"OR",
         "rules":["highway=motorway","highway=motorway_link","highway=trunk","highway=trunk_link"]
       },
       "mediumPriorityRule": {
         "condition":"OR",
         "rules":["highway=primary","highway=primary_link","highway=secondary","highway=secondary_link"]
       }
     }
   }
 }
 ```
 Here is the actual configuration file for reference: [configuration.json](../config/configuration.json)
 
 This configuration file is the default configuration file supplied with Atlas Checks and configures the all checks
 that are included with Atlas Checks.
 
 ### Groups

A configuration file can optionally have a groups object.
The keys in this object are the names of groups, and the values are lists of ISO3 country codes that are present in those groups.
For example, given the following groups object:
```
{
  "groups": {
    "one": ["ABC", "DEF", "GHI"],
    "two": ["JKL", "MNO", "PQR"]
  }
}
```
the rest of the file could refer to the fictional countries with ISO codes ABC, DEF, and GHI with the string “one”, and the fictional countries with the codes JKL, MNO, and PQR with the string “two”.
One use for this is in the override object, discussed below.

 ### Common Keys
 
 There are a couple of common keys that all checks can make use of, and are baked into the framework itself, so no
 extra code is required in the check itself.
 
 #### tags.filter
 This key allows you to filter your check based on tags for each feature that is evaluated. In the example above
 it shows the following value `highway->*|waterway->*|building->*`. This simply means that the check only will evaluate
 features that have the "highway", "waterway" or "building" tag associated with it. There are various types of filtering
 that can be achieved with the tags.filter key.
 
 ###### Base Case
 `highway=primary`
 
 This is the simplest case and probably the one that would most often be used. It simply states that only 
  evaluate features if it has the tag "highway" and the value of that tag is "primary".
 
 ###### OR or AND
 Keys can be separated by "|" (OR) or "&" (AND), which will give logical groupings. The groupings are simplified and so 
 would not encompass the full scope of a boolean expression. "|" is use initially to split the groupings and then
  each group would split again by "&". So for example:
  
 `highway->*|leisure->swimming_pool&area->yes|building->yes`
 
 This rule would only evaluate features that contained the highway tag, or contained the tag leisure with the value swimming_pool and the tag
 area with the value yes, or containing the tag building with the value yes.
 
 ###### Any value or not a specific value
 
The * value indicates that all values are acceptable for the tag. Example:

`highway->*`

Simply indicates that you want to filter by all features that simply contain the highway tag and the value of the tag is irrelevant.

The ! value allows you to state that for a feature with tag X it must not contain value Y. Example:

`highway->!no`

In this example we will accept any feature that does not contain the tag highway with the value no.

###### Multiple values for a tag

You can also check for multiple values in a tag by separating the values by a comma. Example:

`highway->primary,secondary,tertiary`

#### override

This key allows you to specify certain values for your configuration for particular countries.
The properties of this object must have keys that are equal to ISO3 country codes or group names defined in the groups object, as explained above.
The values inside this object are themselves objects, which mimic the structure and properties of the default configuration for this check.
Values defined inside of this country-specific override object take precedence over the default values, when run on a particular country.
An example:
```
{
  "CheckResourceLoader": {...},
  "groups": {
    "one": ["ABC", "DEF", "GHI"]
  },
  "CheckOne": {
    "value": 1.0,
    "word": "apple"
    "override": {
      "JKL": {
        "value": 2.0,
        "word": "banana"
      },
      "one": {
        "value": 1.5,
        "word": "orange"
      },
      "ABC": {
        "value": 3.0,
        "word": "mango"
      }
    }
  }
}
```
For the fictional country with ISO code `JKL`, `CheckOne` will be run with `value` equal to 2.0 and `word` equal to banana, while in `DEF` and `GHI`, `value` will be 1.5 and `word` will be orange.
In `ABC`, the specific value takes precedence over the group value, so `value` will be equal to 3.0 and `word` will be mango.
In the rest of the world, the default values of 1.0 and apple will be used.
You can also use the dot notation as shorthand to populate simple objects. For example:
```
"CheckTwo": {
    "value": 1.0,
    "foo": "bar",
    "override.DEF.foo": "baz"
}
```
would set `foo` to be "baz" in DEF, and "bar" in every other country.
### Check Configuration

In the example above there was the following:

```json
"length": {
   "maximum.kilometers": 100.0,
   "minimum.meters": 100.0
 },
```
These are specific properties for the check itself, in the case of the FloatingEdgeCheck this is setting the 
configuration for the longest length an edge can be and the shortest length an edge can be before those edges
are classified as floating edges. These variables would be initialized in your check class in the default constructor
using the following:

`configurationValue(configuration, "length.minimum.meters", DISTANCE_MINIMUM_METERS_DEFAULT, Distance::meters)`

In the code above we are using a helper class that can be found in the base class that helps us get values easily.
In this case we are retrieving the key "length.minimum.meters", if the key is not found then we will use the default
which is stored in the constant variable DISTANCE_MINIMUM_METERS_DEFAULT in the check class. Once we have the value,
the value is transformed from a double to a Distance object using the Distance#meters method.

### MapRoulette Configuration

The Atlas Checks framework can also automatically upload all the checks directly to MapRoulette for evaluation and
fixing by the community. This may or may not be a desirable result, however it is possible, and configuring the 
Challenge information for the check is easily done in the configuration.

```json
"challenge": {
  "description": "Verify that the same Polyline does not intersect itself at any point.",
  "blurb": "Modify Polylines such that they do not self intersect.",
  "instruction": "Open your favorite editor and move the polylines such that they are not self intersecting anymore.",
  "difficulty": "NORMAL",
  "defaultPriority": "LOW",
  "highPriorityRule": {
    "condition":"OR",
    "rules":["highway=motorway","highway=motorway_link","highway=trunk","highway=trunk_link"]
  },
  "mediumPriorityRule": {
    "condition":"OR",
    "rules":["highway=primary","highway=primary_link","highway=secondary","highway=secondary_link"]
  }
}
```

Not all values for a Challenge in the MapRoulette API is supported, but all that are supported are shown
above. The priority rules have also been simplified, so that your json does not become overly complex. In
the priority json, the main difference is the structure of the rules, which is really your tag key value
pair combinations.
