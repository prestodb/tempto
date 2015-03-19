/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

public class HiveTableInstance
{
    private final String name;
    private final String nameInDatabase;
    // some other table info

    public HiveTableInstance(String name, String nameInDatabase)
    {
        this.name = name;
        this.nameInDatabase = nameInDatabase;
    }

    public String getName()
    {
        return name;
    }

    public String getNameInDatabase()
    {
        return nameInDatabase;
    }
}
