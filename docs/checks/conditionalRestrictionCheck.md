# Conditional Restriction Check

This check flags features that contain a `:conditional` tag that is not constructed following the rules set in the 
OSM wiki: [Conditional restrictions](https://wiki.openstreetmap.org/wiki/Conditional_restrictions).

#### Live Examples

1. Way [id:19696701](https://www.openstreetmap.org/way/19696701) has the value `no (maxstay<3 hours)` which does not 
respect the `<restriction-value> @ <condition>` format.
2. Way [id:525881134](https://www.openstreetmap.org/way/525881134) has the key `psv:lanes:backward:conditional` which
does not respect the `<restriction-type>[:<transportation mode>][:<direction>]` format.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; 
in our case, we’re are looking at
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java) and 
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
 
 This check verifies all elements that have at least one tag containing `:conditional`. For these cases it verifies that 
 the key follows either one of the two acceptable forms: 
 1. `<restriction-type>[:<transportation mode>][:<direction>]:conditional`
 2. `<transportation mode>[:<direction>]:conditional` - in case of restriction type as access.
 
 For each part there are lists of possible values that are compared with the checked element.
 
 The values are also verified to ensure they are following the `<restriction-value> @ <condition>[;<restriction-value> @ <condition>]`
 format. In case of a restriction of type access, the value is also compared with a list of predefined values.
 
 To learn more about the code, please look at the comments in the source code for the check:  
 [ConditionalRestrictionCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/ConditionalRestrictionCheck.java)
 