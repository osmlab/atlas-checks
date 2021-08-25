# Connectivity Check

#### Description

This check flags disconnected Edges in OSM that should be connected. 

#### Configuration

This check has some configurables that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"nearby.edge.distance.meters": 2.0``` - Distance (meters) between disconnected Edge and Node
- ```"denylisted.highway.filter": "highway->no"``` - Highway value(s) not included
- ```"checked.highway.values":["motorway","trunk", ...]``` - The attribute describes what highway types are considered 
valid for checking. By default, only car navigable road are taken into account.  

#### Live Examples

- Edge [id:711519077](https://www.openstreetmap.org/way/711519077) should be connected to Node [id:6689667601](https://www.openstreetmap.org/node/6689667601).
- Edge [id:401326080](https://www.openstreetmap.org/way/401326080) should be connected to Node [id:8342927682](https://www.openstreetmap.org/node/8342927682).
- Edge [id:772817107](https://www.openstreetmap.org/way/772817107) should be connected to Node [id:8211029449](https://www.openstreetmap.org/node/8211029449).


Please see the source code for ConnectivityCheck here: [ConnectivityCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/ConnectivityCheck.java)