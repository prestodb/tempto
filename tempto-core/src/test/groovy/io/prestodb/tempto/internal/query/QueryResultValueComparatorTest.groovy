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

package io.prestodb.tempto.internal.query

import io.prestodb.tempto.configuration.Configuration
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

import static java.sql.JDBCType.BIGINT
import static java.sql.JDBCType.BINARY
import static java.sql.JDBCType.BIT
import static java.sql.JDBCType.CHAR
import static java.sql.JDBCType.DATE
import static java.sql.JDBCType.DECIMAL
import static java.sql.JDBCType.DOUBLE
import static java.sql.JDBCType.FLOAT
import static java.sql.JDBCType.INTEGER
import static java.sql.JDBCType.LONGVARBINARY
import static java.sql.JDBCType.LONGVARCHAR
import static java.sql.JDBCType.NUMERIC
import static java.sql.JDBCType.SMALLINT
import static java.sql.JDBCType.TIME
import static java.sql.JDBCType.TIMESTAMP
import static java.sql.JDBCType.TIMESTAMP_WITH_TIMEZONE
import static java.sql.JDBCType.TIME_WITH_TIMEZONE
import static java.sql.JDBCType.TINYINT
import static java.sql.JDBCType.VARBINARY
import static java.sql.JDBCType.VARCHAR

class QueryResultValueComparatorTest
        extends Specification
{
    @Unroll
    def 'queryResultValueComparator(#type).compare(#actual,#expected) = #result'()
    {
        setup:
        Configuration configuration = Mock(Configuration)
        configuration.getDouble(_) >> Optional.empty()

        expect:
        QueryResultValueComparator.comparatorForType(type, configuration).compare(actual, expected) == result

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

        DOUBLE                  | Double.valueOf(1.0)                      | Double.valueOf(1.00000001)               | -1
        DOUBLE                  | Double.valueOf(1.0)                      | Double.valueOf(1.000000000000001)        | -1
        DOUBLE                  | Double.valueOf(1.0)                      | Double.valueOf(1.0000000000000001)       | 0
        FLOAT                   | Float.valueOf(1.0)                       | Float.valueOf(1.0000001)                 | -1
        FLOAT                   | Float.valueOf(1.0)                       | Float.valueOf(1.00000001)                | 0

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

    @Unroll
    def 'queryResultValueComparator(#type).compare(#actual,#expected) = #result with 0.01 tolerance'()
    {
        setup:
        Configuration configuration = Mock(Configuration)
        configuration.getDouble(_) >> Optional.of(Double.valueOf(0.01))

        expect:
        QueryResultValueComparator.comparatorForType(type, configuration).compare(actual, expected) == result

        where:
        type   | actual                    | expected                | result
        DOUBLE | Double.valueOf(1.0)       | Double.valueOf(1.0)     | 0
        DOUBLE | Double.valueOf(1.009999)  | Double.valueOf(1.0)     | 0
        DOUBLE | Double.valueOf(1.01)      | Double.valueOf(1.0)     | 1
        FLOAT  | Double.valueOf(1.0)       | Double.valueOf(1.0)     | 0
        FLOAT  | Double.valueOf(1.009999)  | Double.valueOf(1.0)     | 0
        FLOAT  | Double.valueOf(1.01)      | Double.valueOf(1.0)     | 1

        DOUBLE | Double.valueOf(1000.0)    | Double.valueOf(1000.0)  | 0
        DOUBLE | Double.valueOf(1010.0)    | Double.valueOf(1000.0)  | 0
        DOUBLE | Double.valueOf(1010.001)  | Double.valueOf(1000.0)  | 1
        FLOAT  | Double.valueOf(1000.0)    | Double.valueOf(1000.0)  | 0
        FLOAT  | Double.valueOf(1010.0)    | Double.valueOf(1000.0)  | 0
        FLOAT  | Double.valueOf(1010.001)  | Double.valueOf(1000.0)  | 1

        DOUBLE | Double.valueOf(-1000.0)   | Double.valueOf(-1000.0) | 0
        DOUBLE | Double.valueOf(-1010.0)   | Double.valueOf(-1000.0) | 0
        DOUBLE | Double.valueOf(-1010.001) | Double.valueOf(-1000.0) | -1
        FLOAT  | Double.valueOf(-1000.0)   | Double.valueOf(-1000.0) | 0
        FLOAT  | Double.valueOf(-1010.0)   | Double.valueOf(-1000.0) | 0
        FLOAT  | Double.valueOf(-1010.001) | Double.valueOf(-1000.0) | -1
    }

    private byte[] byteArray(int value)
    {
        return [value];
    }
}
