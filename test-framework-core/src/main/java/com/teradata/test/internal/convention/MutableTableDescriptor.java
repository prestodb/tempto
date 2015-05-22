/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.convention;

import com.teradata.test.fulfillment.table.MutableTableRequirement.State;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MutableTableDescriptor
{
    public final String tableDefinitionName;
    public final State state;
    public final String name;

    MutableTableDescriptor(String tableDefinitionName, State state, String name)
    {
        this.tableDefinitionName = checkNotNull(tableDefinitionName);
        this.state = checkNotNull(state);
        this.name = checkNotNull(name);
    }
}
