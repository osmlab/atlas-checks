# SimilarTagValueCheck

#### Description

The purpose of this check is to identify tags whose values are either duplicates or similar
enough to warrant someone to look at them.

#### Live Examples


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
