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
package com.teradata.tempto.examples;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.teradata.tempto.ProductTest;
import com.teradata.tempto.Requirement;
import com.teradata.tempto.RequirementsProvider;
import com.teradata.tempto.Requires;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.fulfillment.table.ImmutableTableRequirement;
import com.teradata.tempto.fulfillment.table.MutableTableRequirement;
import com.teradata.tempto.fulfillment.table.MutableTablesState;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition;
import com.teradata.tempto.query.QueryExecutor;
import org.testng.annotations.Test;

import javax.inject.Named;

import java.util.List;
import java.util.Optional;

import static com.teradata.tempto.assertions.QueryAssert.Row.row;
import static com.teradata.tempto.assertions.QueryAssert.assertThat;
import static com.teradata.tempto.fulfillment.table.ImmutableTablesState.immutableTablesState;
import static com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition.jdbcTableDefinition;

public class PostgresqlQueryTest
        extends ProductTest
{

    @Inject
    @Named("psql")
    private QueryExecutor queryExecutor;

    @Inject
    private MutableTablesState mutableTablesState;

    private static final JdbcTableDefinition TEST_TABLE_DEFINITION;
    static {
        JdbcTableDataSource dataSource = () -> ImmutableList.<List<Object>>of(
                ImmutableList.of(1, "x"),
                ImmutableList.of(2, "y")
        ).iterator();
        TEST_TABLE_DEFINITION = jdbcTableDefinition("test_table", "CREATE TABLE %NAME% (a int, b varchar(100))", dataSource);
    }

    private static class ImmutableTestJdbcTable
            implements RequirementsProvider
    {

        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return new ImmutableTableRequirement(TEST_TABLE_DEFINITION, Optional.of("psql"));
        }
    }

    private static class MutableTestJdbcTable
            implements RequirementsProvider
    {

        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return MutableTableRequirement.builder(TEST_TABLE_DEFINITION).withDatabase("psql").build();
        }
    }


    @Test(groups = "psql_query")
    @Requires(ImmutableTestJdbcTable.class)
    public void selectFromImmutableTable()
    {
        String nameInDatabase = immutableTablesState().get("test_table", Optional.of("psql")).getNameInDatabase();
        assertThat(queryExecutor.executeQuery("select * from " + nameInDatabase)).containsOnly(row(1, "x"), row(2, "y"));
    }

    @Test(groups = "psql_query")
    @Requires(MutableTestJdbcTable.class)
    public void selectFromMutableTable()
    {
        String tableName = mutableTablesState.get("test_table").getNameInDatabase();
        assertThat(queryExecutor.executeQuery("select * from " + tableName)).containsOnly(row(1, "x"), row(2, "y"));
    }

}
