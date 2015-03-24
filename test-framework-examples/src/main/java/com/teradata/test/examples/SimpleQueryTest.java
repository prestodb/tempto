/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.teradata.test.ProductTest;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.Requires;
import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement;
import org.testng.annotations.Test;

import static com.teradata.test.assertions.QueryAssert.Row.row;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions.NATION;
import static com.teradata.test.query.QueryExecutor.query;

public class SimpleQueryTest
        extends ProductTest
{

    private static class SimpleTestRequirements
            implements RequirementsProvider
    {

        @Override
        public Requirement getRequirements()
        {
            return new ImmutableHiveTableRequirement(NATION);
        }
    }

    @Test(groups = "query")
    @Requires(SimpleTestRequirements.class)
    public void selectAllFromNation()
    {
        assertThat(query("select * from nation")).hasRowsCount(25);
    }

    @Test(groups = {"smoke", "query"})
    @Requires(SimpleTestRequirements.class)
    public void selectCountFromNation()
    {
        assertThat(query("select count(*) from nation"))
                .hasRowsCount(1)
                .hasRows(row(25));
    }

    @Test(groups = "failing")
    public void failingTest()
    {
        assertThat(1).isEqualTo(2);
    }

    @Test(groups = "skipped", enabled = false)
    public void disabledTest()
    {
        assertThat(1).isEqualTo(2);
    }

}
