# Street Name Integer Only Check

#### Description

This check flags roads that have only integers in their name. 

An example of an improper name tag in this case is something like: `name=42`  
Items like `name=1st Ave` are allowed, and not flagged.  

To avoid overlap with [ShortNameCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/ShortNameCheck.java), 
this check can be configured to not flag any name tag with only a single character (i.e. `name=1` would not be flagged).

#### Live Example

The way [id:395169730](https://www.openstreetmap.org/way/395169730) is a road and has the name 1543 (an integer).  

#### Code Review

This check takes a configurable list on name tags to search.  
The defaults are `name`, `name:left`, and `name:right`.

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges]((https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)).

The atlas objects are filter to only include Edges that meet the following conditions:

* Has a `highway` tag that denotes it as being car navigable
* Is not already flagged
* Has one, or more, of the configurable name tags


```java
@Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return HighwayTag.isCarNavigableHighway(object)
                && !this.isFlagged(object.getOsmIdentifier()) && object instanceof Edge
                && this.nameKeys.stream()
                        .anyMatch(nameKey -> object.getOsmTags().containsKey(nameKey));
    }
```

The final filtered list has its name tags tested to see if they only contain integers, ignoring spaces.  
If this is true for any of the name tags, the object is flagged. See the `flag` method of this check. 

To learn more about the code, please look at the comments in the source code for the check.
[StreetNameIntegersOnlyCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/StreetNameIntegersOnlyCheck.java)