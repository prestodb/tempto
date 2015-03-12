/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.Requirement;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.context.State;

import java.util.Collections;
import java.util.List;

public class ImmutableTableFulfiller
        implements RequirementFulfiller<ImmutableTablesState>
{
    // todo should have some QueryExecutor as dependency

    @Override
    public List<ImmutableTablesState> fulfill(List<Requirement> requirements)
    {
        // todo filter out requirement and create tables, upload data to hdfs etc.
        return Collections.emptyList();
    }

    @Override
    public void cleanup()
    {
        // todo remove created tables
    }
}
