# Bad Value Tag Check

This check flags features for bad value tags based on a list of RegexTaggableFilter. The list of RegexTaggableFilter for this check are passed through a resource file.

File "badTagValue.txt" contains the mapping of AtlasEntity to its corresponding filters.
Each configurable filter has 2 parts. The first is AtlasEntity class (node, edge, area, etc.). The second is a 
RegexTaggableFilter.

If a feature is one of the classes given and passes the RegexTaggableFilter then it is flagged.

#### Configurable Filter Example:

Any primitive with source containing "google" should be flagged since it is an illegal source.
The filter for this would look like the following:  
`{
   "filters": [
     {
       "tagName": "source",
       "regex": [
         ".*(?i)\\bgoogle\\b.*"
       ]
     }
   ]
 }`  
 
This is composed of a `tagName`, containing the name of the tag that needs to be checked, and `regex`, a list
of regex string that will be checked against the tag value.

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
The only validation requirement is that a feature of the same OSM ID has not been flagged before. 

Once a feature has passed validation it is checked against the filters that are set in the constructor of the check, based on the config values mentioned in the check description.
For each filter the feature is checked that it is of a type given in the first part of the filter
and passes the RegexTaggableFilter. If both these things are true then the feature is flagged. 

For each filter a feature passes an instruction is added to the flag. The tag keys from the RegexTaggableFilter 
are pulled out and added to the instruction to give guidance on where the problem is. 

To learn more about the code, please look at the comments in the source code for the check:  
[InvalidTagsCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/BadValueTagCheck.java)
