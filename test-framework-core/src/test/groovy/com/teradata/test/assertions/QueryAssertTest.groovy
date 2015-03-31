/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.assertions

import com.teradata.test.query.QueryResult
import org.assertj.core.api.AbstractListAssert
import spock.lang.Specification

import static com.teradata.test.assertions.QueryAssert.Row.row
import static com.teradata.test.assertions.QueryAssert.assertThat
import static java.sql.JDBCType.INTEGER
import static java.sql.JDBCType.VARCHAR

public class QueryAssertTest
        extends Specification
{

  private final QueryResult NATION_JOIN_REGION_QUERY_RESULT = new QueryResult(
          [INTEGER, VARCHAR, VARCHAR],
          [
                  "n.nationkey": 1,
                  "n.name"     : 2,
                  "r.name"     : 3
          ],
          [
                  [1, "ALGERIA", "AFRICA"],
                  [2, "ARGENTINA", "SOUTH AMERICA"]
          ]);

  private final ColumnValuesAssert EMPTY_COLUMN_VALUE_ASSERT = new ColumnValuesAssert<Object>() {
    @Override
    void assertColumnValues(AbstractListAssert columnAssert)
    {
      // DO NOTHING
    }
  }

  def 'hasResultCount fails'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasRowsCount(3)

    then:
    def e = thrown(AssertionError)
    e.message == 'Expected result count to be <3>, but was <2>'
  }

  def 'hasResultCount'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasRowsCount(2)

    then:
    noExceptionThrown()
  }

  def 'extractingColumn fails - no such column index'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .column(30, INTEGER, EMPTY_COLUMN_VALUE_ASSERT)

    then:
    def e = thrown(AssertionError)
    e.message == 'Result contains only <3> columns, extracting column <30>'
  }

  def 'extractingColumn fails - no such column name'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .column('unknown_column', INTEGER, EMPTY_COLUMN_VALUE_ASSERT)

    then:
    def e = thrown(AssertionError)
    e.message == 'No column with name: <unknown_column>'
  }

  def 'extractingColumn fails - invalid type'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .column('n.nationkey', VARCHAR, EMPTY_COLUMN_VALUE_ASSERT)

    then:
    def e = thrown(AssertionError)
    e.message == 'Expected <1> column, to be type: <VARCHAR>, but was: <INTEGER>'
  }

  def 'hasColumnCount with index'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .column(1, INTEGER, EMPTY_COLUMN_VALUE_ASSERT)

    then:
    noExceptionThrown()
  }

  def 'hasColumnCount with name'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .column('n.nationkey', INTEGER, EMPTY_COLUMN_VALUE_ASSERT)

    then:
    noExceptionThrown()
  }

  def 'hasColumns - wrong column count'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasColumns(VARCHAR)

    then:
    def e = thrown(AssertionError)
    e.message == 'Expected column count to be <1>, but was <3> - columns <[INTEGER, VARCHAR, VARCHAR]>'
  }

  def 'hasColumns - different column types'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasColumns(VARCHAR, VARCHAR, VARCHAR)

    then:
    def e = thrown(AssertionError)
    e.message == 'Expected <0> column of type <VARCHAR>, but was <INTEGER>, actual columns: [INTEGER, VARCHAR, VARCHAR]'
  }

  def 'hasColumns'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasColumns(INTEGER, VARCHAR, VARCHAR)

    then:
    noExceptionThrown()
  }

  def 'hasRows - different number of rows'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .hasRows(
            row(1, "ALGERIA", "AFRICA"),
            row(2, "ARGENTINA", "SOUTH AMERICA"),
            row(3, "AUSTRIA", "EUROPE")
    )

    then:
    def e = thrown(AssertionError)
    e.message == 'Expected result count to be <3>, but was <2>'
  }

  def 'hasRows - different value - no row'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .hasRows(
            row(2, "ARGENTINA", "SOUTH AMERICA"),
            row(1, "ALGERIA", "valid_value"),
    )

    then:
    def e = thrown(AssertionError)
    e.message == 'Could not find rows:\n' +
            '[1, ALGERIA, valid_value]\n' +
            '\n' +
            'actual rows:\n' +
            '[1, ALGERIA, AFRICA]\n' +
            '[2, ARGENTINA, SOUTH AMERICA]'
  }

  def 'hasRows'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .hasRows(
            row(2, "ARGENTINA", "SOUTH AMERICA"),
            row(1, "ALGERIA", "AFRICA"),
    )

    then:
    noExceptionThrown()
  }

  def 'hasRowsInOrder - different number of rows'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .hasRowsInOrder(
            row(1, "ALGERIA", "AFRICA"),
            row(2, "ARGENTINA", "SOUTH AMERICA"),
            row(3, "AUSTRIA", "EUROPE")
    )

    then:
    def e = thrown(AssertionError)
    e.message == 'Expected result count to be <3>, but was <2>'
  }

  def 'hasRowsInOrder - different value - no row'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .hasRowsInOrder(
            row(1, "ALGERIA", "AFRICA"),
            row(2, "ARGENTINA", "valid_value")
    )

    then:
    def e = thrown(AssertionError)
    e.message == 'Not equal rows:\n' +
            '1 - expected: <2|ARGENTINA|valid_value|>\n1 - actual:   <2|ARGENTINA|SOUTH AMERICA|>'
  }

  def 'hasRowsInOrder - different order'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .hasRowsInOrder(
            row(2, "ARGENTINA", "SOUTH AMERICA"),
            row(1, "ALGERIA", "AFRICA")
    )

    then:
    def e = thrown(AssertionError)
    e.message == 'Not equal rows:\n' +
            '0 - expected: <2|ARGENTINA|SOUTH AMERICA|>\n0 - actual:   <1|ALGERIA|AFRICA|>\n' +
            '1 - expected: <1|ALGERIA|AFRICA|>\n1 - actual:   <2|ARGENTINA|SOUTH AMERICA|>'
  }

  def 'hasRowsInOrder'()
  {
    when:
    assertThat(NATION_JOIN_REGION_QUERY_RESULT)
            .hasRowsInOrder(
            row(1, "ALGERIA", "AFRICA"),
            row(2, "ARGENTINA", "SOUTH AMERICA")
    )

    then:
    noExceptionThrown()
  }
}