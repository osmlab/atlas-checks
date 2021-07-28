package org.openstreetmap.atlas.checks.database.taginfo;

import java.util.Map;

/**
 * Tag Info key information
 *
 * @author Taylor Smock
 */
public class TagInfoKeys extends TagInfoKeyTagCommon
{
    private static final long serialVersionUID = 8475635006448882620L;
    private final Number valuesAll;
    private final Number valuesNodes;
    private final Number valuesWays;
    private final Number valuesRelations;
    private final Number usersAll;
    private final Number usersNodes;
    private final Number usersWays;
    private final Number usersRelations;
    private final Number cellsNodes;
    private final Number cellsWays;

    public TagInfoKeys(final Map<String, Object> row)
    {
        super(row);
        this.valuesAll = getAndCast(row, "values_all", Number.class);
        this.valuesNodes = getAndCast(row, "values_nodes", Number.class);
        this.valuesWays = getAndCast(row, "values_ways", Number.class);
        this.valuesRelations = getAndCast(row, "values_relations", Number.class);
        this.usersAll = getAndCast(row, "users_all", Number.class);
        this.usersNodes = getAndCast(row, "users_nodes", Number.class);
        this.usersWays = getAndCast(row, "users_ways", Number.class);
        this.usersRelations = getAndCast(row, "users_relations", Number.class);
        this.cellsNodes = getAndCast(row, "cells_nodes", Number.class);
        this.cellsWays = getAndCast(row, "cells_ways", Number.class);
    }

    public Number getCellsNodes()
    {
        return this.cellsNodes;
    }

    public Number getCellsWays()
    {
        return this.cellsWays;
    }

    /**
     * @return The number of unique users (last edit)
     */
    public Number getUsersAll()
    {
        return this.usersAll;
    }

    /**
     * @return The number of unique users on nodes (last edit)
     */
    public Number getUsersNodes()
    {
        return this.usersNodes;
    }

    /**
     * @return The number of unique users on relations (last edit)
     */
    public Number getUsersRelations()
    {
        return this.usersRelations;
    }

    /**
     * @return The number of unique users on ways (last edit)
     */
    public Number getUsersWays()
    {
        return this.usersWays;
    }

    /**
     * @return The number of unique values
     */
    public Number getValuesAll()
    {
        return this.valuesAll;
    }

    /**
     * @return The number of unique values on nodes
     */
    public Number getValuesNodes()
    {
        return this.valuesNodes;
    }

    /**
     * @return The number of unique values on relations
     */
    public Number getValuesRelations()
    {
        return this.valuesRelations;
    }

    /**
     * @return The number of unique values on ways
     */
    public Number getValuesWays()
    {
        return this.valuesWays;
    }
}
