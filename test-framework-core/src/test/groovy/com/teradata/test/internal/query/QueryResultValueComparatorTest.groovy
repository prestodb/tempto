/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.query

import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

import static java.sql.JDBCType.*

class QueryResultValueComparatorTest
        extends Specification
{

  @Unroll
  def 'queryResultValueComparator(#type).compare(#actual,#expected) = #result'()
  {
    expect:
    QueryResultValueComparator.comparatorForType(type).compare(actual, expected) == result

    where:
    type                    | actual                                   | expected                                 | result
    CHAR                    | null                                     | null                                     | 0
    CHAR                    | null                                     | "a"                                      | -1
    CHAR                    | "a"                                      | null                                     | -1

    CHAR                    | "a"                                      | "a"                                      | 0
    VARCHAR                 | "a"                                      | "a"                                      | 0
    LONGVARCHAR             | "a"                                      | "a"                                      | 0
    VARCHAR                 | "a"                                      | "b"                                      | -1
    VARCHAR                 | "b"                                      | "a"                                      | 1
    VARCHAR                 | "b"                                      | 1                                        | -1

    BINARY                  | byteArray(0)                             | byteArray(0)                             | 0
    VARBINARY               | byteArray(0)                             | byteArray(0)                             | 0
    LONGVARBINARY           | byteArray(0)                             | byteArray(0)                             | 0
    BINARY                  | byteArray(0)                             | byteArray(1)                             | -1
    BINARY                  | byteArray(1)                             | byteArray(0)                             | 1
    BINARY                  | byteArray(0)                             | 0                                        | -1

    BIT                     | true                                     | true                                     | 0
    BIT                     | true                                     | false                                    | 1
    BIT                     | false                                    | true                                     | -1
    BIT                     | false                                    | 0                                        | -1

    BIGINT                  | 1L                                       | 1                                        | 0
    INTEGER                 | 1                                        | 1                                        | 0
    SMALLINT                | 1 as short                               | 1                                        | 0
    TINYINT                 | 1 as byte                                | 1                                        | 0
    BIGINT                  | 1L                                       | 0L                                       | 1
    BIGINT                  | 0L                                       | 1L                                       | -1
    BIGINT                  | 0L                                       | "a"                                      | -1

    DOUBLE                  | Double.valueOf(0.0)                      | Double.valueOf(0.0)                      | 0
    DOUBLE                  | Double.valueOf(1.0)                      | Double.valueOf(0.0)                      | 1
    DOUBLE                  | Double.valueOf(0.0)                      | Double.valueOf(1.0)                      | -1
    DOUBLE                  | Double.valueOf(0.0)                      | "a"                                      | -1

    NUMERIC                 | BigDecimal.valueOf(0.0)                  | BigDecimal.valueOf(0.0)                  | 0
    DECIMAL                 | BigDecimal.valueOf(0.0)                  | BigDecimal.valueOf(0.0)                  | 0
    NUMERIC                 | BigDecimal.valueOf(1.0)                  | BigDecimal.valueOf(0.0)                  | 1
    NUMERIC                 | BigDecimal.valueOf(0.0)                  | BigDecimal.valueOf(1.0)                  | -1
    NUMERIC                 | BigDecimal.valueOf(0.0)                  | "a"                                      | -1

    DATE                    | Date.valueOf("2015-02-15")               | Date.valueOf("2015-02-15")               | 0
    DATE                    | Date.valueOf("2015-02-16")               | Date.valueOf("2015-02-15")               | 1
    DATE                    | Date.valueOf("2015-02-15")               | Date.valueOf("2015-02-16")               | -1
    DATE                    | Date.valueOf("2015-02-15")               | "a"                                      | -1

    TIME                    | Time.valueOf("10:10:10")                 | Time.valueOf("10:10:10")                 | 0
    TIME_WITH_TIMEZONE      | Time.valueOf("10:10:10")                 | Time.valueOf("10:10:10")                 | 0
    TIME                    | Time.valueOf("11:10:10")                 | Time.valueOf("10:10:10")                 | 1
    TIME                    | Time.valueOf("10:10:10")                 | Time.valueOf("11:10:10")                 | -1
    TIME                    | Time.valueOf("10:10:10")                 | "a"                                      | -1

    TIMESTAMP               | Timestamp.valueOf("2015-02-15 10:10:10") | Timestamp.valueOf("2015-02-15 10:10:10") | 0
    TIMESTAMP_WITH_TIMEZONE | Timestamp.valueOf("2015-02-15 10:10:10") | Timestamp.valueOf("2015-02-15 10:10:10") | 0
    TIMESTAMP               | Timestamp.valueOf("2015-02-16 10:10:10") | Timestamp.valueOf("2015-02-15 10:10:10") | 1
    TIMESTAMP               | Timestamp.valueOf("2015-02-15 10:10:10") | Timestamp.valueOf("2015-02-16 10:10:10") | -1
    TIMESTAMP               | Timestamp.valueOf("2015-02-15 10:10:10") | "a"                                      | -1
  }

  byte[] byteArray(int value)
  {
    return [value];
  }
}
