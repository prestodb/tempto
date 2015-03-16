/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.requirements;

import com.teradata.test.Requirement;

public class DataSourceRequirements
{
    public static Requirement dataSource(String dataSourceDesc)
    {
        return new DataSourceRequirement(dataSourceDesc);
    }
}
