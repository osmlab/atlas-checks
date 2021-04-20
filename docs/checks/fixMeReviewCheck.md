# FixMeReviewCheck

#### Description
The purpose of this check is to flag features that contain the "fixme"/"FIXME" tags with along with a variety of other important tags. Other important tags that need to be used in unison with the fixme tag
are: [WaterwayTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/WaterwayTag.java), [OneWayTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/oneway/OneWayTag.java), 
[BuildingTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/BuildingTag.java), [HighwayTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/HighwayTag.java), 
[NameTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/names/NameTag.java), [ReferenceTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/names/ReferenceTag.java),
[PlaceTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/PlaceTag.java), and [SurfaceTag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/SurfaceTag.java). 

The current supported values for the fixme tag are: "verify", "position", "resurvey", 
"Revisar: este punto fue creado por importación directa", "continue", "name", 
"incomplete", "draw geometry and delete this point", "unfinished", and "recheck".

The flagged features' fixme tag must contain one of the values listed in the supported values list and the feature must contain at least one of the important tags.


#### Configurables
* ***minHighwayTag:*** Minimum highway tag value for object.
* ***fixMeSupportedValues:*** Supported fixme tag values

#### Live Examples

1. [Way:177401829](https://www.openstreetmap.org/way/177401829) has "fixme" tag with priority value (name) along with important supplementary tag(s).
2. [Way:328136807](https://www.openstreetmap.org/way/328136807) has "fixme" tag with priority value (continue) along with important supplementary tag(s).

#### Code Review
This check evaluates [Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java),
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java),
[Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java),
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java),
[Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), and
[Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java) 
determining if they contain the fixme tag and priority fixme tag values along with other important tags.