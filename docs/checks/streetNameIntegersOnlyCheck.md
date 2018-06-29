# Street Name Integer Only Check

#### Description

This check flags roads that have only integers in their name. 

An example of an improper name tag in this case is something like: `name=42`  
Items like `name=1st Ave` are allowed, and not flagged.

#### Live Example

The way [id:395169730](https://www.openstreetmap.org/way/395169730) is a road and has the name 1543 (an integer).  

#### Code Review

This check takes a configurable list on name tags to search.  
The defaults are `name`, `name:left`, and `name:right`.

```java
public StreetNameIntegersOnlyCheck(final Configuration configuration)
    {
        super(configuration);
        this.nameKeys = (List<String>) configurationValue(configuration, "name_keys.filter",
                NAME_KEYS_DEFAULT);
    }
```

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
If this is true for any of the name tags, the object is flagged.

```java
@Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Try to convert name to an integer. If it converts without failure it should be flagged.
        for (final String nameKey : this.nameKeys)
        {
            try
            {
                Integer.parseInt(
                        object.getOsmTags().getOrDefault(nameKey, "a").replaceAll(" ", ""));
            }
            catch (final NumberFormatException e)
            {
                continue;
            }
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }

        return Optional.empty();
    }
```

To learn more about the code, please look at the comments in the source code for the check.
[StreetNameIntegersOnlyCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/StreetNameIntegersOnlyCheck.java)