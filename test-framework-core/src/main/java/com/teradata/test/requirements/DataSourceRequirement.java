/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.requirements;

import com.teradata.test.Requirement;

public class DataSourceRequirement
        implements Requirement
{
    private final String dataSourceDesc;

    public DataSourceRequirement(String dataSourceDesc)
    {
        this.dataSourceDesc = dataSourceDesc;
    }
}
