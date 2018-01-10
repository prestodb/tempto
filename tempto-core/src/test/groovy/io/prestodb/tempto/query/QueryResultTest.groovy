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

package io.prestodb.tempto.query

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
