/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

public class TableRequirements
{

    /**
     * Requirement for mutable table.
     * Test code is allowed to mutate (insert/delete rows) for mutable table
     */
    public static MutableTableRequirement mutableTable(TableDefinition tableDefinition)
    {
        return new MutableTableRequirement(tableDefinition);
    }

    /**
     * Requirement for mutable table.
     * Test code is allowed to mutate (insert/delete rows) for mutable table
     */
    public static MutableTableRequirement mutableTable(TableDefinition tableDefinition, String name, MutableTableRequirement.State state)
    {
        return new MutableTableRequirement(tableDefinition, name, state);
    }

    /**
     * Requirement for immutable table.
     */
    public static ImmutableTableRequirement immutableTable(TableDefinition tableDefinition)
    {
        return new ImmutableTableRequirement(tableDefinition);
    }
}
