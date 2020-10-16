# Invalid Tags Check

This check flags features for invalid tags based on a list of TaggableFilters and RegexTaggableFilters. The list of filters for this check are passed through the config and/or through resource files.
The two configurable boolean values, "filters.resource.append" and "filters.resource.override", 
determine the list of taggable filters that each of the atlas entities will be tested against. The rules are
1) If "filters.resource.override" is true and "filters.resource.append" is false,
only the filters that are passed through the configuration file will be used to test the atlas entities. 
2) If "filters.resource.append" is true and "filters.resource.override" is false,
both the configurable filters and filters passed through the resource files will be used to test the atlas features.
3) In all other cases, filters are set to empty list.

Filters for this check are either passed through config or/and through resource files.
File "invalidTags.txt" contains the mapping of AtlasEntity to its corresponding resource file.
Each configurable filter has 2 parts. The first is AtlasEntity class (node, edge, area, etc.). The second is a 
[TaggableFilter](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/filters/TaggableFilter.java)
or a [RegexTaggableFilter](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/filters/RegexTaggableFilter.java).

If a feature is one of the classes given and passes the filter then it is flagged.

** Configurable Filter Example:**

TaggableFilter

Areas and Relations with the tag `boundary=protected_area` should have a `protect_class` tag.  
Filters to flag this would look like the following:  
`["area,"boundary->protected_area&protect_class->!"],
 ["relation,"boundary->protected_area&protect_class->!"]`  
The first string is the AtlasEntity class we want to look for. 
The second string is the taggable filter that is looking for the combination of `boundary=protected_area`
without a `protect_class` tag.

This would flag an osm feature like the following: [Way 673787307](https://www.openstreetmap.org/way/673787307).

RegexTaggableFilter

Lets consider for this example that nodes with the tag `source` should not contain illegal maps as values. 
The RegexTaggableFilter can be configured in two ways. First, the inline configuration regex filter would look like this:
`"filters.classes.regex": [
       ["node", ["source"],["illegal value regex"]]
     ]`
The first string is the AtlasEntity. The next array of string represents the tag names for which the value must be checked. The last 
array of strings represents the regex patterns that are matched with the tag values.
The second option is to create the filter through a resource file. This is a more complete option allowing also the configuration
of an exception map containing certain tag-value pairs that are excepted by the regex match. For this, the filter is passed 
in json format: `{
                   "filters": [
                     {
                       "tagNames": ["source"],
                       "regex": [
                         "illegal value regex 1",
                         "illegal value regex 2"
                       ],
                       "exceptions":[
                         {
                           "tagName" : "source",
                           "values": ["open version map", "public map"]
                         }
                       ]
                     }
                   ]
                 }` 
To be mentioned here that the resource file for the `RegexTaggableFilter` must contain the word `regex` in it's name.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; in our case, we’re are looking at
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java),
[Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java),
[Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java),
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java),
[Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), and
[Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

Due to the general nature of this check, most features are valid for this check.
The only validation requirement is that a feature of the same type and OSM ID has not been flagged before. 
Many other checks only look at the OSM ID when checking if a feature is flagged. Because this check deals 
with multiple types of features, and ids are not unique across types, this check has to check the type and id.

Once a feature has passed validation it is checked against the filters that are set in the constructor of the check, based on the config values mentioned in the check description.
For each filter the feature is checked that it is of a type given in the first part of the filter
and passes the filter. If both these things are true then the feature is flagged. 

For each filter a feature passes an instruction is added to the flag. The tag keys from the TaggableFilter and the 
RegexTaggableFilter are pulled out and added to the instruction to give guidance on where the problem is. 

To learn more about the code, please look at the comments in the source code for the check:  
[InvalidTagsCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/InvalidTagsCheck.java)
