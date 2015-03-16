/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import org.testng.annotations.Test;

import java.time.LocalDate;

import static com.teradata.test.assertions.QueryAssert.Row.row;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.query.QueryExecutor.query;
import static java.sql.JDBCType.INTEGER;
import static java.sql.JDBCType.VARCHAR;

public class ExampleQueryAssertTest
{

    @Test
    public void testHasColumnName()
    {
        assertThat(query("SELECT * FROM nation WHERE name LIKE 'IR%' ORDER BY name"))
                .hasRowsCount(2)
                .hasColumnsCount(4)
                .column("name", VARCHAR, c -> c
                        .containsExactly("IRELAND", "IRAN")
                        .isSorted());
    }

    @Test
    public void testHasColumnIndex()
    {
        assertThat(query("SELECT nationkey, name FROM nation"))
                .hasRowsCount(25)
                .hasColumnsCount(2)
                .column(1, INTEGER, c -> c.contains(5, 17));
    }

    @Test
    public void testContainsExactly()
    {
        assertThat(query("SELECT n.nationkey, n.name, r.name FROM nation n " +
                "INNER JOIN region r ON n.regionkey = r.regionkey " +
                "WHERE name like 'A%' ORDER BY n.name"))
                .hasColumns(INTEGER, VARCHAR, VARCHAR)
                .hasRows(
                        row(7, "ARGENTINA", "SOUTH AMERICA"),
                        row(1, "ALGERIA", "AFRICA"));
    }

    @Test
    public void testContainsExactlyInOrder()
    {
        assertThat(query("SELECT n.nationkey, n.name, r.name FROM nation n " +
                "INNER JOIN region r ON n.regionkey = r.regionkey " +
                "WHERE name like 'A%' AND n.created > ? ORDER BY n.name", LocalDate.parse("2015-01-01")))
                .hasColumns(INTEGER, VARCHAR, VARCHAR)
                .hasRowsInOrder(
                        row(1, "ALGERIA", "AFRICA"),
                        row(7, "ARGENTINA", "SOUTH AMERICA"));
    }
}
