/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.Requirement;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class ImmutableHiveTableRequirement
        implements Requirement
{

    private final HiveTableDefinition tableDefinition;

    public ImmutableHiveTableRequirement(HiveTableDefinition tableDefinition)
    {
        this.tableDefinition = tableDefinition;
    }

    public HiveTableDefinition getTableDefinition()
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
