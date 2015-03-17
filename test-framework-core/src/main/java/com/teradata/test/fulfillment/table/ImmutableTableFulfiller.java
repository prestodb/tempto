/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class ImmutableTableFulfiller
        implements RequirementFulfiller
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ImmutableTableFulfiller.class);

    // todo should have some QueryExecutor as dependency

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {
        LOGGER.debug("Immutable table fulfillment");
        // todo filter out requirement and create tables, upload data to hdfs etc.
        return Collections.emptySet();
    }

    @Override
    public void cleanup()
    {
        LOGGER.debug("Immutable table cleanup");
        // todo remove created tables
    }
}
