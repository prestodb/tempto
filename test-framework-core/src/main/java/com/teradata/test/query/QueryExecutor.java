/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import java.sql.JDBCType;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;

/**
 * Interface for executors of a sql queries.
 */
public interface QueryExecutor
{

    String DEFAULT_DB_NAME = "default";

    /**
     * Executes statement. Can be either SELECT or DDL/DML.
     * DDL/DML integer result is wrapped into QueryResult
     * @return Result of executed statement.
     */
    QueryResult executeQuery(String sql, QueryParam... params);

    /**
     * Force given query to be executed as update
     */
    QueryResult executeUpdate(String sql, QueryParam... params);

    /**
     * Force given query to be executed as select
     */
    QueryResult executeSelect(String sql, QueryParam... params);

    /**
     * Executes given query on DB setup in test context.
     */
    public static QueryResult query(String sql, QueryParam... params)
    {
        QueryExecutor executor = testContext().getDependency(QueryExecutor.class, DEFAULT_DB_NAME);
        return executor.executeQuery(sql, params);
    }

    public static QueryParam param(JDBCType type, Object value)
    {
        return new QueryParam(type, value);
    }

    public static class QueryParam
    {
        final JDBCType type;
        final Object value;

        private QueryParam(JDBCType type, Object value)
        {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return toStringHelper(this)
                    .add("type", type)
                    .add("value", value)
                    .toString();
        }
    }
}
