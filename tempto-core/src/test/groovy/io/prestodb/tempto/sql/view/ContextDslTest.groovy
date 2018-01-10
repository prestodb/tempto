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

package io.prestodb.tempto.sql.view

import io.prestodb.tempto.context.ContextDsl
import io.prestodb.tempto.context.ContextRunnable
import io.prestodb.tempto.query.QueryExecutor
import io.prestodb.tempto.query.QueryResult
import spock.lang.Specification

import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.TEST_CONFIGURATION_URIS_KEY

class ContextDslTest
        extends Specification
{
    def setupSpec()
    {
        System.setProperty(TEST_CONFIGURATION_URIS_KEY, "/configuration/global-configuration-tempto.yaml");
    }

    def 'executeWithView'()
    {
        setup:
        String viewName = "test_view";
        String selectSql = "SELECT * FROM nation"
        ContextRunnable testRunnable = Mock(ContextRunnable)
        QueryExecutor queryExecutor = Mock(QueryExecutor)
        queryExecutor.executeQuery("DROP VIEW test_view") >> QueryResult.forSingleIntegerValue(1)
        queryExecutor.executeQuery("CREATE VIEW test_view AS SELECT * FROM nation") >> QueryResult.forSingleIntegerValue(1)
        queryExecutor.executeQuery("DROP VIEW test_view") >> QueryResult.forSingleIntegerValue(1)

        ViewContextProvider contextProvider = new ViewContextProvider(viewName, selectSql, queryExecutor)

        when:
        ContextDsl.executeWith(contextProvider, testRunnable)

        then:
        1 * testRunnable.run(_)
    }
}
