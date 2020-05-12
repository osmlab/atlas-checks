# Invalid Tags Check

This check flags features for invalid tags based on a list of TaggableFilters. The list of TaggableFilters for this check is either a combination of filters passed through the 
check configuration and default list of TaggableFilters or just the default filters. 
The two configurable boolean values, "override.default.filters" and "append.to.default.filters", 
determine the list of taggable filters that each of the atlas entities will be tested against. The rules are
1) If "override.default.filters" is true and "append.to.default.filters" is false,
only the filters that are passed through the configuration file will be used to test the atlas entities. 
2) If "append.to.default.filters" is true and "override.default.filters" is false,
both the configurable filters and default filters will be used to test the atlas features.
3) In all other cases, filters are set to empty list.

The default filters for each AtlasEntity are stored in resource files. File "invalidTags.txt" contains the mapping of AtlasEntity to its corresponding resource file.
Each configurable filter has 2 parts. The first is AtlasEntity class (node, edge, area, etc.). The second is a 
[TaggableFilter](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/filters/TaggableFilter.java).

If a feature is one of the classes given and passes the TaggableFilter then it is flagged.

** Configurable Filter Example:**

Areas and Relations with the tag `boundary=protected_area` should have a `protect_class` tag.  
Filters to flag this would look like the following:  
`["area,"boundary->protected_area&protect_class->!"],
 ["relation,"boundary->protected_area&protect_class->!"]`  
The first string is the AtlasEntity class we want to look for. 
The second string is the taggable filter that is looking for the combination of `boundary=protected_area`
without a `protect_class` tag.

This would flag an osm feature like the following: [Way 673787307](https://www.openstreetmap.org/way/673787307).

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
and passes the TaggableFilter. If both these things are true then the feature is flagged. 

For each filter a feature passes an instruction is added to the flag. The tag keys from the TaggableFilter 
are pulled out and added to the instruction to give guidance on where the problem is. 

To learn more about the code, please look at the comments in the source code for the check:  
[InvalidTagsCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/InvalidTagsCheck.java)
