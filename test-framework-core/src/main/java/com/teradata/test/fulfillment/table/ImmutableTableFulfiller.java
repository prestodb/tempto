/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ImmutableTableFulfiller
        implements RequirementFulfiller<ImmutableTablesState>
{
    // todo should have some QueryExecutor as dependency

    @Override
    public Set<ImmutableTablesState> fulfill(Set<Requirement> requirements)
    {
        // todo filter out requirement and create tables, upload data to hdfs etc.
        return Collections.emptySet();
    }

    @Override
    public void cleanup()
    {
        // todo remove created tables
    }
}
