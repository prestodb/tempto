/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement;

import static com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions.NATION;

// TODO: just a placeholder - probably not needed
public class SqlQueryConventionBasedTestRequirement
        implements RequirementsProvider
{
    @Override
    public Requirement getRequirements()
    {
        return new ImmutableHiveTableRequirement(NATION);
    }
}
