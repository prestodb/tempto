/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.Requirement;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class MutableTableRequirement
        implements Requirement
{

    private final TableDefinition tableDefinition;

    public MutableTableRequirement(TableDefinition tableDefinition)
    {
        this.tableDefinition = tableDefinition;
    }

    public TableDefinition getTableDefinition()
    {
        return tableDefinition;
    }

    @Override
    public boolean equals(Object o)
    {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }
}
