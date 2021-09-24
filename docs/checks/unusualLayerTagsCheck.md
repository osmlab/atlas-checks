# UnusualLayerTagsCheck

**Description**
Checking objects that contain the layer tag, or are bridges/tunnels. 

**Requirements**
- Is a Way, Node, Line or Area

**Use Cases**
*Case 1: [Layer tag is 0.]*
- The object has a layer tag equivalent to 0.
- [Way: 46940763](https://www.openstreetmap.org/way/46940763)
  
*Case 2: [Landuse feature not on ground.]*
- If the object has a landuse tag, a valid layer tag and is not a bridge or a tunnel.
- [Way: 94449954](https://www.openstreetmap.org/way/94449954)

*Case 3: [Natural feature not on ground.]*
- The object has a natural tag that is not "water", a valid layer tag and no bridge or tunnel.
- [Way: 25314718](https://www.openstreetmap.org/way/25314718)

*Case 4: [Highway feature not on ground.]*
- The object is a highway, has a valid layer tag and no bridge, tunnel or covered.
- **Exceptions**: highway is steps; highway is service and service is parking_aisle.
- [Way: 121520872](https://www.openstreetmap.org/way/121520872)

*Case 5: [Waterway feature not on ground.]*
- The object has a waterway tag or natural:water tag, a valid layer tag and is not a bridge, a tunnel, covered, or underground.
- [Way: 290833931](https://www.openstreetmap.org/way/290833931)

*Case 6: [Bridge has an invalid layer tag.]*
- The object is a bridge and is missing a layer tag or the layer tag is out of the range [1,5].
- **Exceptions** object is highway:steps or parking_aisle
- [id:479328850](https://www.openstreetmap.org/way/479328850)

*Case 7: [Tunnel has an invalid layer tag.]*
- The object is a tunnel and is missing a layer tag or the layer tag is out of the range [-5,-1].
- **Exceptions** object is highway:steps or parking_aisle
- [id:521569767](https://www.openstreetmap.org/way/521569767)

**References**
[OSM Wiki Issue Page](https://wiki.openstreetmap.org/wiki/Osmose/issues#4110)
[Osmose backend](https://github.com/osm-fr/osmose-backend/blob/master/plugins/TagRemove_Layer.py)

To learn more about the code, please look at the comments in the source code for the check.
[UnusualLayerTagsCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/UnusualLayerTagsCheck.java)