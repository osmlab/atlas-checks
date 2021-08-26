# StreetNameCheck

#### Description
This check flags all the objects (Nodes, Ways and Relations) with "ss" or "ß" found in objects.

#### Configurables
All these variables are lists and correspond to each other with their indeces. For example, if you are looking to add a
country to the list at index 4, then all the other values for that country have to be added to the same index. If there 
are no elements to put, then add an empty list "[]".
- ***countries*** a list of the countries where the check needs to flag items
- ***containsValues*** a list of values that we are looking for in the name or street_name tags
- ***notContainsValues*** a list of values that mustn't be flagged in this check
- ***tags*** what specific value we are looking at for "containsValue". These do not affect code.
- ***correctTags*** the proper value that needs to be substituted in.

#### Live Examples

1. [Way:44446981](https://www.openstreetmap.org/way/44446981) in AUT the way has a name that contains the value "Strasse".
2. [Way:168119181](https://www.openstreetmap.org/way/168119181) in LIE the way has a name that contains the value "Straße"

#### Code Review
This check evaluates [Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java) and
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), and 
[Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).
It flags the object that contain the values that are provided in the configuration.json file.

##### Validating the Object
We first validate that the incoming object is:
* Hasn't been flagged previously
* A Way (Main Edge), a Node or a Relation


##### Flagging the Object
* The object has a name or street_name tag that contain the values provided in the containsValues variables in the config file.

##### Not flagging the Object
* The object contains a containsValue variables but in addition contains the notContainsValue variable.


To learn more about the code, please look at the comments in the source code for the check: [StreetNameCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/StreetNameCheck.java)
