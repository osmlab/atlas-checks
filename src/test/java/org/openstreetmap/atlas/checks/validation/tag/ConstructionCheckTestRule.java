package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

public class ConstructionCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "0, 0";

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=2017", "highway=construction"})})
    private Atlas yyyyDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=2017-1", "highway=construction"})})
    private Atlas yyyydDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=1-2017", "highway=construction"})})
    private Atlas dyyyyDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=Jan-2017", "highway=construction"})})
    private Atlas MMMyyyyDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=January 2017", "highway=construction"})})
    private Atlas MMMMyyyyDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=2017-1-1", "highway=construction"})})
    private Atlas yyyyMdDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=1-1-2017", "highway=construction"})})
    private Atlas dMyyyyDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=1-Jan-2017", "highway=construction"})})
    private Atlas dMMMyyyyDateFormatterAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"open_date=1 January 2017", "highway=construction"})})
    private Atlas dMMMMyyyyDateFormatterAtlas;

    public Atlas yyyyDateFormatterAtlas()
    {
        return this.yyyyDateFormatterAtlas;
    }
    public Atlas yyyydDateFormatterAtlas()
    {
        return this.yyyydDateFormatterAtlas;
    }
    public Atlas dyyyyDateFormatterAtlas()
    {
        return this.dyyyyDateFormatterAtlas;
    }
    public Atlas MMMyyyyDateFormatterAtlas()
    {
        return this.MMMyyyyDateFormatterAtlas;
    }
    public Atlas MMMMyyyyDateFormatterAtlas()
    {
        return this.MMMMyyyyDateFormatterAtlas;
    }
    public Atlas yyyyMdDateFormatterAtlas()
    {
        return this.yyyyMdDateFormatterAtlas;
    }
    public Atlas dMyyyyDateFormatterAtlas()
    {
        return this.dMyyyyDateFormatterAtlas;
    }
    public Atlas dMMMyyyyDateFormatterAtlas()
    {
        return this.dMMMyyyyDateFormatterAtlas;
    }
    public Atlas dMMMMyyyyDateFormatterAtlas()
    {
        return this.dMMMMyyyyDateFormatterAtlas;
    }

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"any=construction", "open_date=2017"})})
    private Atlas isConstructionAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"any=construction:", "open_date=2017"})})
    private Atlas isStartsWithConstructionColonAtlas;
    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"any=construction:date", "open_date=2017"})})
    private Atlas isNotConstructionColonDateAtlas;

    public Atlas isConstructionAtlas()
    {
        return this.isConstructionAtlas;
    }
    public Atlas isStartsWithConstructionColonAtlas()
    {
        return this.isStartsWithConstructionColonAtlas;
    }
    public Atlas isNotConstructionColonDateAtlas()
    {
        return this.isNotConstructionColonDateAtlas;
    }

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"check_date=2017", "any=construction"})})
    private Atlas oldCheckDateAtlas;

    public Atlas oldCheckDateAtlas()
    {
        return oldCheckDateAtlas;
    }

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = TEST_1), tags = {"last_edit_time=1483257600", "any=construction"})})
    private Atlas oldLastEditTimeAtlas;

    public Atlas oldLastEditTimeAtlas()
    {
        return oldLastEditTimeAtlas;
    }
}
