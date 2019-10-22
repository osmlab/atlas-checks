# UnusualLayerTagsCheck

#### Description

The purpose of this check is to identify layer tag values when accompanied by invalid tunnel and bridge tags.

#### Live Examples

1. Line [id:521569767](https://www.openstreetmap.org/way/521569767) has _Layer_ Tag > 0, and is within tunnel=BUILDING_PASSAGE.
2. Line [id:479328850](https://www.openstreetmap.org/way/479328850) has _Layer_ Tag < 0, and crosses bridge=YES.

#### Code Review

This check is intended to validate layer tag values when accompanied by tunnel and bridge tags which meet any of the following four requirements.

 1. Layer tag value should be an integer, ranging from -5 to 5, excluding 0 [LayerTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/LayerTag.java)
 2. Ways passed above other Ways with valid layer tag (above), accompanied by the bridge tag containing one of the following values:
        * YES, VIADUCT, AQUEDUCT, BOARDWALK, MOVABLE, SUSPENSION, CULVERT, ABANDONED, LOW_WATER_CROSSING, SIMPLE_BRUNNEL, COVERED [BridgeTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/BridgeTag.java)
 3. Ways passed above other Ways with valid layer tag, accompanied by a tunnel tag containing one of the following values:
        * YES, CULVERT, BUILDING_PASSAGE [TunnelTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/TunnelTag.java)
 4. Ways with junction=ROUNDABOUT tag must not contain layer tag [JunctionTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/JunctionTag.java)

To learn more about the code, please look at the comments in the source code for the check.
[UnusualLayerTagsCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/UnusualLayerTagsCheck.java)