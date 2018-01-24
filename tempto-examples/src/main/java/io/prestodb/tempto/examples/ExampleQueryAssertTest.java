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

import org.testng.annotations.Test;

import java.time.LocalDate;

import static io.prestodb.tempto.assertions.QueryAssert.Row.row;
import static io.prestodb.tempto.assertions.QueryAssert.assertThat;
import static io.prestodb.tempto.query.QueryExecutor.param;
import static io.prestodb.tempto.query.QueryExecutor.query;
import static java.sql.JDBCType.DATE;
import static java.sql.JDBCType.INTEGER;
import static java.sql.JDBCType.VARCHAR;

public class ExampleQueryAssertTest
{
    @Test(enabled = false)
    public void testHasColumnName()
    {
        assertThat(query("SELECT * FROM nation WHERE name LIKE 'IR%' ORDER BY name"))
                .hasRowsCount(2)
                .hasColumnsCount(4)
                .column("name", VARCHAR, c -> c
                        .containsExactly("IRELAND", "IRAN")
                        .isSorted());
    }

    @Test(enabled = false)
    public void testHasColumnIndex()
    {
        assertThat(query("SELECT nationkey, name FROM nation"))
                .hasRowsCount(25)
                .hasColumnsCount(2)
                .column(1, INTEGER, c -> c.contains(5, 17));
    }

    @Test(enabled = false)
    public void testContainsOnly()
    {
        assertThat(query("SELECT n.nationkey, n.name, r.name FROM nation n " +
                "INNER JOIN region r ON n.regionkey = r.regionkey " +
                "WHERE name like 'A%' ORDER BY n.name"))
                .hasColumns(INTEGER, VARCHAR, VARCHAR)
                .containsOnly(
                        row(7, "ARGENTINA", "SOUTH AMERICA"),
                        row(1, "ALGERIA", "AFRICA"));
    }

    @Test(enabled = false)
    public void testContainsExactly()
    {
        assertThat(query("SELECT n.nationkey, n.name, r.name FROM nation n " +
                        "INNER JOIN region r ON n.regionkey = r.regionkey " +
                        "WHERE name like 'A%' AND n.created > ? ORDER BY n.name",
                param(DATE, LocalDate.parse("2015-01-01"))))
                .hasColumns(INTEGER, VARCHAR, VARCHAR)
                .containsExactly(
                        row(1, "ALGERIA", "AFRICA"),
                        row(7, "ARGENTINA", "SOUTH AMERICA"));
    }
}
