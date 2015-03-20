/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import java.util.List;

public abstract class TableDefinition
{
    protected final String name;
    protected final List<TableColumn> columns;

    public TableDefinition(String name, List<TableColumn> columns)
    {
        this.name = name;
        this.columns = columns;
    }

    public String getName()
    {
        return name;
    }

    public List<TableColumn> getColumns()
    {
        return columns;
    }
}
