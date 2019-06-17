# Generalized Coastline Check

This check flags any LineItem that has the `natural=coastline` tag, and also has a percentage of [Segment](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/)s
that are longer than a configured value that surpasses some threshold percentage.

#### Live Examples

1. Line [id:676900620](https://www.openstreetmap.org/way/676900620#map=18/63.77414/-20.81246&layers=T) is longer than
the configured distance of 100 meters.
2. Line [id:603526147](https://www.openstreetmap.org/way/603526147) has 37.5% of segments that are greater than 100 meters.

#### Code Review

The check ensures that the Atlas object being evaluated is a [LineItem](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/LineItem.java). Then, it examines the LineItem's Segments. It
calculates the percentage of Segments whose lengths are greater than some configured value. Then it flags the item if
this percentage is greater than some configured value.

Please see source code for the GeneralizedCoastlineCheck here: [GeneralizedCoastlineCheck](https://github.com/seancoulter/atlas-checks/tree/dev/src/main/java/org/openstreetmap/atlas/checks/validation/linear/lines)
