/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.hive;

import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.table.TableInstance;

import java.util.Optional;

public class HiveTableInstance
        extends TableInstance
{
    private final Optional<String> mutableDataHdfsDataPath;

    public HiveTableInstance(String name, String nameInDatabase, HiveTableDefinition tableDefinition, Optional<String> mutableDataHdfsDataPath)
    {
        super(name, nameInDatabase, tableDefinition);
        this.mutableDataHdfsDataPath = mutableDataHdfsDataPath;
    }

    public Optional<String> getMutableDataHdfsDataPath()
    {
        return mutableDataHdfsDataPath;
    }
}
