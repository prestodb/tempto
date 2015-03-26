/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

public abstract class TableDefinition
{
    protected final String name;

    public TableDefinition(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
