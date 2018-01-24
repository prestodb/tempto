/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.examples;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.prestodb.tempto.AfterTestWithContext;
import io.prestodb.tempto.BeforeTestWithContext;
import io.prestodb.tempto.ProductTest;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.RequirementsProvider;
import io.prestodb.tempto.Requires;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.fulfillment.table.ImmutableTableRequirement;
import io.prestodb.tempto.fulfillment.table.MutableTablesState;
import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.fulfillment.table.TableManager;
import org.junit.After;
import org.junit.Before;
import org.testng.annotations.Test;

import static io.prestodb.tempto.Requirements.allOf;
import static io.prestodb.tempto.assertions.QueryAssert.Row.row;
import static io.prestodb.tempto.assertions.QueryAssert.assertThat;
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.testContext;
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.testContextIfSet;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.CREATED;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static io.prestodb.tempto.fulfillment.table.TableRequirements.mutableTable;
import static io.prestodb.tempto.fulfillment.table.hive.HiveTableDefinition.like;
import static io.prestodb.tempto.fulfillment.table.hive.tpch.TpchTableDefinitions.NATION;
import static io.prestodb.tempto.fulfillment.table.hive.tpch.TpchTableDefinitions.REGION;
import static io.prestodb.tempto.query.QueryExecutor.query;
import static org.assertj.core.api.Assertions.assertThat;

public class SimpleQueryTest
        extends ProductTest
{
    private static class SimpleTestRequirements
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return new ImmutableTableRequirement(NATION);
        }
    }

    @Inject()
    @Named("hive")
    TableManager tableManager;

    @Before
    public void someBefore()
    {
        // just to check if having @Before method does not break anything
    }

    @After
    public void someAfter()
    {
        // just to check if having @After method does not break anything
    }

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

    @Test(groups = "query", timeOut = 1000000)
    public void createAndDropMutableTable()
    {
        TableDefinition tableDefinition = like(NATION)
                .setNoData()
                .setName("some_other_table_name")
                .build();

        TableInstance instanceCreated = tableManager.createMutable(tableDefinition, CREATED);
        TableInstance instanceLoaded = tableManager.createMutable(tableDefinition);
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

    private static class MultipleTablesTestRequirements
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return allOf(
                    mutableTable(NATION, "table", LOADED),
                    mutableTable(REGION, "table", LOADED));
        }
    }

    @Test(groups = "query")
    @Requires(MultipleTablesTestRequirements.class)
    public void selectAllFromMultipleTables()
    {
        MutableTablesState mutableTablesState = testContext().getDependency(MutableTablesState.class);
        TableInstance tableInstance = mutableTablesState.get("table");
        assertThat(query("select * from " + tableInstance.getNameInDatabase())).hasAnyRows();
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
