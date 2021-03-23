# Road Link Check

#### Description
The purpose of this check is to verify that at least one end of a highway link is connected to another road or link of the same classification. This check also identifies links that are longer than the given configurable distance.

#### Configuration

This check has one configurable that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"length.maximum.miles": 1.0``` - Maximum length (miles) of road link

#### Live Examples

- Road link [id:89146080](https://www.openstreetmap.org/way/89146080) is longer than 1 mile.
- Road link [id:867719370](https://www.openstreetmap.org/way/867719370) is highway type ```secondary_link``` connected to two other highways ([id:867719371](https://www.openstreetmap.org/way/867719371) & [id:867719372](https://www.openstreetmap.org/way/867719372)) with different type values of ```primary```


Please see the source code for RoadLinkCheck here: [RoadLinkCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/RoadLinkCheck.java)