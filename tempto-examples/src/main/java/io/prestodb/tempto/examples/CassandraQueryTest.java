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

import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.ProductTest;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.RequirementsProvider;
import io.prestodb.tempto.Requires;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalDataSource;
import io.prestodb.tempto.internal.fulfillment.table.cassandra.CassandraTableDefinition;
import org.testng.annotations.Test;

import java.util.List;

import static io.prestodb.tempto.assertions.QueryAssert.Row.row;
import static io.prestodb.tempto.assertions.QueryAssert.assertThat;
import static io.prestodb.tempto.fulfillment.table.TableRequirements.immutableTable;
import static io.prestodb.tempto.query.QueryExecutor.query;
import static java.lang.String.format;
import static java.sql.JDBCType.BIGINT;
import static java.sql.JDBCType.DOUBLE;
import static java.sql.JDBCType.LONGNVARCHAR;

public class CassandraQueryTest
        extends ProductTest
{
    private static final String TEST_DATABASE_NAME = "cassandra";
    private static final String TEST_SCHEMA_NAME = "test";
    private static final String TEST_TABLE_NAME = "test_table";

    private static final CassandraTableDefinition TEST_TABLE_DEFINITION;

    static {
        RelationalDataSource dataSource = () -> ImmutableList.<List<Object>>of(
                ImmutableList.of(1L, "foo", 2L, 754.1985),
                ImmutableList.of(4L, "bar", 3L, 754.2008)
        ).iterator();
        TEST_TABLE_DEFINITION = CassandraTableDefinition.cassandraBuilder(TEST_TABLE_NAME)
                .withDatabase(TEST_DATABASE_NAME)
                .withSchema(TEST_SCHEMA_NAME)
                .setCreateTableDDLTemplate("CREATE TABLE %NAME% (c bigint, d double, a varchar, b bigint, PRIMARY KEY(c, a))")
                .setDataSource(dataSource)
                .build();
    }

    private static class ImmutableTestCassandraTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return immutableTable(TEST_TABLE_DEFINITION);
        }
    }

    @Test(groups = "cassandra_query")
    @Requires(CassandraQueryTest.ImmutableTestCassandraTable.class)
    public void selectFromImmutableTable()
    {
        // Cassandra disregards the order of columns outside primary key
        // Primary key comes first, in the order as it's defined, rest of columns are ordered alphabetically
        // in this example it's gonna be (c, a, b, d). c and a together are the primary key (order in primary key is preserved),
        // then b comes before d even though it was defined later (alphabetical order).
        assertThat(query(format("SELECT *  FROM %s.%s.%s", TEST_DATABASE_NAME, TEST_SCHEMA_NAME, TEST_TABLE_NAME)))
                .hasColumns(BIGINT, LONGNVARCHAR, BIGINT, DOUBLE)
                .containsOnly(
                        row(1L, "foo", 2, 754.1985),
                        row(4L, "bar", 3, 754.2008));
    }
}
