/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.requirements;

import com.teradata.test.Requirement;

public final class TableRequirements
{
    public static Requirement table(String tableName)
    {
        return new TableRequirement(tableName);
    }

    private TableRequirements()
    {
    }
}
