/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.table;

/**
 * Describes a table that is instantiated during a test run.
 */
public class TableInstance
{
    private final String name;
    private final String nameInDatabase;
    private final TableDefinition tableDefinition;

    public TableInstance(String name, String nameInDatabase, TableDefinition tableDefinition)
    {
        this.name = name;
        this.nameInDatabase = nameInDatabase;
        this.tableDefinition = tableDefinition;
    }

    public String getName()
    {
        return name;
    }

    public String getNameInDatabase()
    {
        return nameInDatabase;
    }

    public TableDefinition tableDefinition()
    {
        return tableDefinition;
    }
}
