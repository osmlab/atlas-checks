# SimilarTagValueCheck

#### Description

The purpose of this check is to identify tags whose values are either duplicates or similar
enough to warrant someone to look at them.

Configurables: 
* "value.length.min": Minimum length an individual value must be to be considered for inspection, value.length >= min. 
* "similarity.threshold.min": Minimum edit distance between two values to be added to the flag where a value of 0 is 
   used to include duplicates, value >= min.
* "similarity.threshold.max": Maximum edit distance between two values to be added to the flag, value <= max.
* "filter.commonSimilars": values that can commonly be found together validly on a tag that are similar but with no 
   action needed to be taken.
* "filter.tags": tags that commonly have values that are duplicates/similars that are valid.
* "filter.tagsWithSubCategories": tags that contain one or many sub-categories that commonly have valid 
   duplicate/similar values.

#### Live Examples
Similar tag values
1. The node [5142510561](https://www.openstreetmap.org/way/5142510561) has the similar values: "crayfish" and "Crayfish"

Duplicate tag values
1. The way [173171120](https://www.openstreetmap.org/way/173171120) has multiple duplicate values in the "source" tag

#### Code Review

This check evaluates all atlas objects that can hold OSM tags.
Any duplicate tags are removed in a feature change, while similars are flagged for user review.

#### Validating the object
The incoming object must:
* have at least one tag with multiple values (contains a ";")

#### Flagging the object
We filter out all tags that:
* are tags that commonly contain valid duplicate/similar values
* values that are similar to others that commonly occur on the same tag
* values that either contain: length shorter than the defined min length, a number, non-latin characters
* the last filtering step we remove any tags that do not contain multiple values

We then take the valid tags and compare each value computing similarity between each, using the
Levenshtein Edit Distance algorithm. We keep value pairs with a similarity that falls within our 
similarity threshold.

From there we split the gathered pairs between those that are duplicate values, and those that are similar.
The duplicates are added to the instructions and used to create a fix suggestion.
The similars are just added to the instructions.

#### Fix Suggestion
We create fix suggestions only on duplicate values, as similar values are difficult to determine which one (if not both)
should be kept. The fix, for duplicates, is to remove all but one occurrence of the duplicate value from the tag.
