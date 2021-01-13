# Inconsistent Road Classification Check

#### Description

The purpose of this check aims to flag segments that have different road classifications compared to the road segments they're connect to.

#### Configurables

This check has four configurables that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"long.edge.threshold": 1000.0``` - Maximum length (meters) of combined edges
- ```"minimum.highway.type": "tertiary_link"``` - Minimum highway classification type
- ```"maximum.edge.length": 500.0``` - Maximum length (meters) of edge

#### Live Examples
- Line ```highway=teritary``` [id:234258492](https://www.openstreetmap.org/way/234258492), Line ```highway=secondary``` [id:445632970](https://www.openstreetmap.org/way/445632970), and Line ```highway=tertiary``` [id:27116452](https://www.openstreetmap.org/way/27116452)
- Line ```highway=primary``` [id:42821967](https://www.openstreetmap.org/way/42821967), Line ```highway=tertiary``` [id:28893694](https://www.openstreetmap.org/way/28893694), and Line ```highway=primary``` [id:306089807](https://www.openstreetmap.org/way/306089807)
- Line ```highway=secondary``` [id:391612634](https://www.openstreetmap.org/way/391612634), Line ```highway=tertiary``` [id:246156874](https://www.openstreetmap.org/way/246156874), Line ```highway=secondary``` [id:444405397](https://www.openstreetmap.org/way/444405397)

Please see the source code for InconsistentRoadClassificationCheck here: [InconsistentRoadClassificationCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/InconsistentRoadClassificationCheck.java)