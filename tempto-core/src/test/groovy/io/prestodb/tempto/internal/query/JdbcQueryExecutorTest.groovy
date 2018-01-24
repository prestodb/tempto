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

import io.prestodb.tempto.context.TestContext
import io.prestodb.tempto.internal.context.GuiceTestContext
import io.prestodb.tempto.query.JdbcConnectionsPool
import io.prestodb.tempto.query.JdbcConnectivityParamsState
import io.prestodb.tempto.query.JdbcQueryExecutor
import io.prestodb.tempto.query.QueryResult
import org.apache.commons.dbutils.QueryRunner
import spock.lang.Specification

import java.sql.Connection

import static JdbcUtils.connection
import static JdbcUtils.registerDriver
import static io.prestodb.tempto.assertions.QueryAssert.Row.row
import static io.prestodb.tempto.assertions.QueryAssert.assertThat
import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.TEST_CONFIGURATION_URIS_KEY
import static java.sql.JDBCType.INTEGER
import static java.sql.JDBCType.VARCHAR

class JdbcQueryExecutorTest
        extends Specification
{
    private static final JdbcConnectivityParamsState JDBC_STATE =
            JdbcConnectivityParamsState.builder()
                    .setName('connection_name')
                    .setDriverClass('org.hsqldb.jdbc.JDBCDriver')
                    .setUrl('jdbc:hsqldb:mem:mydb')
                    .setUser('sa')
                    .setPooling(true)
                    .build();

    private static TestContext testContext = new GuiceTestContext();
    private JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(JDBC_STATE, new JdbcConnectionsPool(), testContext);

    def setupSpec()
    {
        System.setProperty(TEST_CONFIGURATION_URIS_KEY, "/configuration/global-configuration-tempto.yaml");
        registerDriver(JDBC_STATE)
    }

    def cleanupSpec()
    {
        testContext.close();
    }

    void setup()
    {
        // TODO: use try with resources when we move to groovy 2.3
        Connection c
        QueryRunner run = new QueryRunner()
        try {
            c = connection(JDBC_STATE)
            run.update(c, 'DROP SCHEMA PUBLIC CASCADE')
            run.update(c,
                    'CREATE TABLE  company ( \
                      comp_name varchar(100) NOT NULL, \
                      comp_id int \
                    )')
            run.update(c, 'INSERT INTO company(comp_id, comp_name) values (1, \'Teradata\')')
            run.update(c, 'INSERT INTO company(comp_id, comp_name) values (2, \'Oracle\')')
            run.update(c, 'INSERT INTO company(comp_id, comp_name) values (3, \'Facebook\')')
        }
        finally {
            if (c != null) {
                c.close()
            }
        }
    }

    def 'test select'()
    {
        when:
        QueryResult result = queryExecutor.executeQuery('SELECT comp_id, comp_name FROM company ORDER BY comp_id')

        then:
        assertThat(result)
                .hasColumns(INTEGER, VARCHAR)
                .containsExactly(
                row(1, 'Teradata'),
                row(2, 'Oracle'),
                row(3, 'Facebook'))
    }

    def 'test update'()
    {
        setup:
        QueryResult result

        when:
        result = queryExecutor.executeQuery('UPDATE company SET comp_name=\'Teradata Kings\' WHERE comp_id=1')

        then:
        assertThat(result)
                .hasColumns(INTEGER)
                .containsExactly(
                row(1))

        when:
        result = queryExecutor.executeQuery('SELECT comp_id, comp_name FROM company ORDER BY comp_id')

        then:
        assertThat(result)
                .hasColumns(INTEGER, VARCHAR)
                .containsExactly(
                row(1, 'Teradata Kings'),
                row(2, 'Oracle'),
                row(3, 'Facebook'))
    }
}
