/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.sql.view;

import com.teradata.test.context.ContextProvider;
import com.teradata.test.query.QueryExecutionException;
import com.teradata.test.query.QueryExecutor;

import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.query.QueryType.UPDATE;
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
        assertThat(queryExecutor.executeQuery(createViewDDL, UPDATE))
                .hasRowsCount(1);
    }

    private void tryDropView(String viewName)
    {
        try {
            assertThat(queryExecutor.executeQuery(format("DROP VIEW %s", viewName), UPDATE))
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
