# UnusualLayerTagsCheck

#### Description

The purpose of this check is to identify _layer_ Tag values when accompanied by invalid _tunnel_ and _bridge_ Tags.

#### Live Examples

1. Line [id:521569767](https://www.openstreetmap.org/way/521569767) has _Layer_ Tag > 0, and is within _tunnel=BUILDING\_PASSAGE_.
2. Line [id:479328850](https://www.openstreetmap.org/way/479328850) has _Layer_ Tag < 0, and crosses _bridge=YES_.

#### Code Review

This check is intended to validate _layer_ Tag values when accompanied by _tunnel_ and _bridge_ Tags which meet any of the following four requirements.

 1. The _layer_ Tag value should be an integer, ranging from -5 to 5, and excluding 0 (per [LayerTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/LayerTag.java))
 2. Ways passing above other Ways with valid _layer_ Tags (see item 1), that are accompanied by a _bridge_ Tag containing any of the following values (per [BridgeTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/BridgeTag.java)):
    * _YES_,
    * _VIADUCT_,
    * _AQUEDUCT_,
    * _BOARDWALK_,
    * _MOVABLE_,
    * _SUSPENSION_,
    * _CULVERT_,
    * _ABANDONED_,
    * _LOW\_WATER\_CROSSING_,
    * _SIMPLE\_BRUNNEL_,
    * or _COVERED_
 3. Ways passing above other Ways with valid _layer_ Tags, that are accompanied by a _tunnel_ Tag containing any of the following values (per [TunnelTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/TunnelTag.java)):
    * _YES_,
    * _CULVERT_,
    * or _BUILDING\_PASSAGE_
 4. Ways with a _junction=ROUNDABOUT_ Tag must not contain a _layer_ Tag (per [JunctionTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/JunctionTag.java))

To learn more about the code, please look at the comments in the source code for the check.
[UnusualLayerTagsCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/UnusualLayerTagsCheck.java)