/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.sql;

import com.teradata.test.sql.view.ViewContextProvider;

import static com.teradata.test.query.QueryExecutor.defaultQueryExecutor;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public final class SqlContexts
{

    /**
     * Helper method designed to be used with lambda expressions containing assertions used
     * on newly created view:
     * <pre>
     *  executeWith(createViewAs("SELECT * FROM nation", view -> {
     *        assertThat(query(format("SELECT * FROM %s", view.getName())))
     *            .hasRowsCount(25);
     *  }));
     * </pre>
     *
     * @param selectSql sql select statement used to create view
     * @return viewContextProvider
     */
    public static ViewContextProvider createViewAs(String selectSql)
    {
        String viewName = generateRandomName("TEST_VIEW_");
        return new ViewContextProvider(viewName, selectSql, defaultQueryExecutor());
    }

    private static String generateRandomName(String prefix)
    {
        return prefix + randomAlphanumeric(4);
    }

    private SqlContexts()
    {
    }
}
