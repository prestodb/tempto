/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query

import spock.lang.Specification

import java.sql.JDBCType
import java.sql.ResultSet

class QueryResultTest
        extends Specification
{
  def "test QueryResult project"()
  {
    setup:
    def jdbcResultSet = Mock(ResultSet)
    def columnTypes = [JDBCType.CHAR, JDBCType.DOUBLE, JDBCType.INTEGER]
    def columnNames = ['char', 'double', 'integer']
    def builder = new QueryResult.QueryResultBuilder(columnTypes, columnNames)
    builder.addRow('aaa', 1.0, 1)
    builder.addRow('bbb', 2.0, 2)
    builder.setJdbcResultSet(jdbcResultSet)
    def queryResult = builder.build()
    def projection = queryResult.project(1, 3)

    expect:
    projection.rows() == [['aaa', 1], ['bbb', 2]]
    projection.columnsCount == 2
    projection.column(1) == ['aaa', 'bbb']
    projection.column(2) == [1, 2]
    projection.getJdbcResultSet().get() == jdbcResultSet
  }
}
