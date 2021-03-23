# Node Valence Check

#### Description

This check flags Nodes that have too many edges connected to them (their valence is too high). Additionally, this check only considers car-navigable connections in the Node valence count.

#### Configuration

This check has one configurable that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"connections.maximum": 10``` - Maximum number of Edges connected

#### Live Examples

- Node [id:7676439527](https://www.openstreetmap.org/node/7676439527) has too many Edges connected to it.
- Node [id:7676439526](https://www.openstreetmap.org/node/7676439526) has too many Edges connected to it.


Please see the source code for NodeValenceCheck here: [NodeValenceCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/NodeValenceCheck.java)