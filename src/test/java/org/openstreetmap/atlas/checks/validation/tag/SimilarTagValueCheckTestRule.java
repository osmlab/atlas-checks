package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link SimilarTagValueCheck}
 *
 * @author v-brjor
 */
public class SimilarTagValueCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "0, 0";

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = { "cuisine=cake;cake" }) })
    private Atlas duplicateCommonSimilarsTest;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = { "hasDupe=dupe;dupe" }) })
    private Atlas hasDuplicateTagTest;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = { "hasSimilar=similar;similer" }) })
    private Atlas hasSimilarTagTest;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = { "cuisine=cafe;cake" }) })
    private Atlas ignoreCommonSimilarsTest;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = { "turn:lanes:forward=dupe;dupe" }) })
    private Atlas ignoreTagSubclassTest;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = { "ref=dupe;dupe" }) })
    private Atlas ignoreTagTest;

    public Atlas getDuplicateCommonSimilarsTest() {
        return this.duplicateCommonSimilarsTest;
    }

    public Atlas getHasDuplicateTagTest()
    {
        return this.hasDuplicateTagTest;
    }

    public Atlas getHasSimilarTagTest()
    {
        return this.hasSimilarTagTest;
    }

    public Atlas getIgnoreCommonSimilarsTest() {
        return this.ignoreCommonSimilarsTest;
    }

    public Atlas getIgnoreTagSubclassTest() {
        return this.ignoreTagSubclassTest;
    }

    public Atlas getIgnoreTagTest() {
        return this.ignoreTagTest;
    }
}
