# Generalized Coastline Check

A generalized coastline can be seen as any coastline that contains too many long line segments. Although perhaps simple to render, long line segments are problematic in that they don't capture the local structure of coastlines at low zoom levels.

Generalized coastlines may cause a few problems:
1. Cartographic issues
2. Poor user experience
3. Territorial inaccuracies

Poor local accuracy on segments of a coastline results in cartographic problems. Users on the ground near a coastline that are provided with a generalized representation of that coastline may encounter hazardous inaccuracies. Boundaries (country, state, etc.) that rely on precisely mapped coastlines may find that maps with generalized coastlines create problems when carrying out coastal or shipping activities.

This check exists to address the above problems with generalized coastlines. Editors who encounter flagged generalized coastlines may resolve the issue by increasing the number of nodes along a long line segment of the coastline "break it up" and near its sharp angles to smooth it.

The check itself flags any LineItem that has the `natural=coastline` tag, and also has a percentage of [Segment](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/)s
that are longer than a configured value that surpasses some threshold percentage. If a sharp angle configurable is provided, locations of sharp angles of an already flagged coastline are marked.

Optionally and in addition to the above, the check can be configured to flag only those coastlines that have certain tags (default: source=PGS). This and the sharp angle spotting switches can be turned on/off via the configuration specs for the check.

#### Live Examples

1. Line [id:676900620](https://www.openstreetmap.org/way/676900620#map=18/63.77414/-20.81246&layers=T) is longer than
the configured distance of 100 meters.
2. Line [id:603526147](https://www.openstreetmap.org/way/603526147) has 37.5% of segments that are greater than 100 meters.

#### Code Review

The check ensures that the Atlas object being evaluated is a [LineItem](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/LineItem.java). Then, it examines the LineItem's Segments. It
calculates the percentage of Segments whose lengths are greater than some configured value. Then it flags the item if
this percentage is greater than some configured value. If a sharp angle threshold is provided, then angles between segments that exceed that threshold are marked for editing too.

Please see source code for the GeneralizedCoastlineCheck here: [GeneralizedCoastlineCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/lines)
