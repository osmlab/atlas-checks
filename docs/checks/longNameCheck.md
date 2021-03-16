# Long Name Check

This check flags features with names longer than a configurable length. 
Its expected use is to detect descriptions in name tags or name tags containing multiple names. 

Multiple localized and non-localized tags are checked. 
The list of tags can be found [here](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/names/NameFinder.java#L39).

The configuration key `name.max` sets the length at or over which a name will be flagged.

It is recommended to limit the features the check examines using the standard `tags.filter` configurable.
Different feature types are expected to have different permissible name lengths (i.e. roads vs buildings). 

#### Live Examples

1. Way [id:636173935](https://www.openstreetmap.org/way/636173935) has over 40 characters in the name tag, and the name is actually a description of the feature.

#### Fix Suggestions

None

#### Code Review

This check looks at all AtlasObjects.

The tags of each object are filtered down to those contained in [NameFinder#STANDARD_TAGS_NON_REFERENCE](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/names/NameFinder.java#L39).
Both localized and non-localized version of the tags are allowed through the filter.
If any of the remaining tags are over the configurable character limit the object is flagged.

The instructions include the tags that were over the limit. 

To learn more about the code, please look at the comments in the source code for the check:
[LongNameCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/LongNameCheck.java)
