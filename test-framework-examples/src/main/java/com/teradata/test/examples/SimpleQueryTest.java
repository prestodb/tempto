/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.teradata.test.AfterTestWithContext;
import com.teradata.test.BeforeTestWithContext;
import com.teradata.test.ProductTest;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.Requires;
import com.teradata.test.fulfillment.table.ImmutableTableRequirement;
import com.teradata.test.fulfillment.table.TableInstance;
import com.teradata.test.fulfillment.table.TableManager;
import org.testng.annotations.Test;

import static com.teradata.test.assertions.QueryAssert.Row.row;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContextIfSet;
import static com.teradata.test.fulfillment.hive.HiveTableDefinition.like;
import static com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions.NATION;
import static com.teradata.test.fulfillment.table.TableManager.dropTableOnTestContextClose;
import static com.teradata.test.query.QueryExecutor.query;
import static org.assertj.core.api.Assertions.assertThat;

public class SimpleQueryTest
        extends ProductTest
{

    private static class SimpleTestRequirements
            implements RequirementsProvider
    {

        @Override
        public Requirement getRequirements()
        {
            return new ImmutableTableRequirement(NATION);
        }
    }

    @Inject()
    @Named("hive")
    TableManager tableManager;

    @BeforeTestWithContext
    public void beforeTest()
    {
        assertThat(testContextIfSet().isPresent()).isTrue();
    }

    @AfterTestWithContext
    public void afterTest()
    {
        assertThat(testContextIfSet().isPresent()).isTrue();
    }

    @Test(groups = "query")
    public void createAndDropMutableTable()
    {
        TableInstance instance = tableManager.createMutable(
                like(NATION)
                        .withData("v1.0", "")
                        .withName("some_other_table_name")
                        .build()
        );
        dropTableOnTestContextClose(instance);
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
                .contains(row(25));
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
