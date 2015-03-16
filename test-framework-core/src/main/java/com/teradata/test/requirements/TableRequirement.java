/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.requirements;

import com.teradata.test.Requirement;

public class TableRequirement
        implements Requirement
{
    private final String tableName;

    public TableRequirement(String tableName) {this.tableName = tableName;}
}
