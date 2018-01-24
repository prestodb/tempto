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

package io.prestodb.tempto.sql.view;

import io.prestodb.tempto.context.ContextProvider;
import io.prestodb.tempto.query.QueryExecutionException;
import io.prestodb.tempto.query.QueryExecutor;

import static io.prestodb.tempto.assertions.QueryAssert.assertThat;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class ViewContextProvider
        implements ContextProvider<View>
{
    private final String viewName;
    private final String selectSql;
    private final QueryExecutor queryExecutor;

    public ViewContextProvider(String viewName, String selectSql, QueryExecutor queryExecutor)
    {
        this.viewName = viewName;
        this.selectSql = selectSql;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public View setup()
    {
        tryDropView(viewName);
        createView(viewName);
        return new View(viewName);
    }

    @Override
    public void cleanup(View context)
    {
        tryDropView(context.getName());
    }

    private void createView(String viewName)
    {
        String createViewDDL = format("CREATE VIEW %s AS %s", viewName, selectSql);
        assertThat(queryExecutor.executeQuery(createViewDDL))
                .hasRowsCount(1);
    }

    private void tryDropView(String viewName)
    {
        try {
            assertThat(queryExecutor.executeQuery(format("DROP VIEW %s", viewName)))
                    .hasRowsCount(1);
        }
        catch (QueryExecutionException e) {
            // view might not exist
            // TODO: make it generic
            assertThat(e.getMessage())
                    .contains("Query failed")
                    .contains("does not exist");
        }
    }
}
