/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.sql;

import com.teradata.test.Requirement;
import com.teradata.test.assertions.QueryAssert;
import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.HeaderFileParser;
import com.teradata.test.internal.convention.HeaderFileParser.ParsingResult;
import com.teradata.test.internal.convention.SqlQueryFileWrapper;
import com.teradata.test.internal.convention.SqlQueryFileWrapper.QueryType;
import com.teradata.test.internal.convention.SqlResultFileWrapper;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.internal.convention.ProcessUtils.execute;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ConventionBasedTest
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private final HeaderFileParser headerFileParser;
    private final String testCaseName;
    private final Optional<File> beforeScriptPath;
    private final Optional<File> afterScriptPath;
    private final File queryFile;
    private final File resultFile;
    private final Requirement requirement;

    public SqlQueryConventionBasedTest(String testCaseName, Optional<File> beforeScriptPath, Optional<File> afterScriptPath,
            File queryFile, File resultFile, Requirement requirement)
    {
        this.testCaseName = testCaseName;
        this.beforeScriptPath = beforeScriptPath;
        this.afterScriptPath = afterScriptPath;
        this.queryFile = queryFile;
        this.resultFile = resultFile;
        this.requirement = requirement;
        this.headerFileParser = new HeaderFileParser();
    }

    @Test
    public void test()
            throws IOException
    {
        LOGGER.debug("Executing sql test: {}", getTestName());

        if (beforeScriptPath.isPresent()) {
            execute(beforeScriptPath.get().toString());
        }

        SqlQueryFileWrapper sqlQueryFileWrapper = getSqlQueryFileWrapper();
        SqlResultFileWrapper resultFileWrapper = getSqlResultFileWrapper();

        QueryExecutor queryExecutor = getQueryExecutor(sqlQueryFileWrapper);
        QueryResult result = executeQuery(
                queryExecutor, sqlQueryFileWrapper.getContent(), sqlQueryFileWrapper.getQueryType()
        );
        QueryAssert queryAssert = assertThat(result)
                .hasColumns(resultFileWrapper.getTypes());

        if (resultFileWrapper.isIgnoreOrder()) {
            queryAssert.hasRows(resultFileWrapper.getRows());
        }
        else {
            queryAssert.hasRowsExact(resultFileWrapper.getRows());
        }

        if (!resultFileWrapper.isIgnoreExcessRows()) {
            queryAssert.hasRowsCount(resultFileWrapper.getRows().size());
        }

        if (afterScriptPath.isPresent()) {
            execute(afterScriptPath.get().toString());
        }
    }

    @Override
    public Requirement getRequirements()
    {
        return requirement;
    }

    @Override
    public String getTestName()
    {
        return testCaseName;
    }

    @Override
    public Set<String> getTestGroups()
    {
        try {
            return getSqlQueryFileWrapper().getTestGroups();
        }
        catch (IOException e) {
            throw new RuntimeException("cannot parse query file", e);
        }
    }

    private SqlQueryFileWrapper getSqlQueryFileWrapper()
            throws IOException
    {
        return new SqlQueryFileWrapper(headerFileParser.parseFile(queryFile));
    }

    private SqlResultFileWrapper getSqlResultFileWrapper()
            throws IOException
    {
        ParsingResult parsedResultFile = headerFileParser.parseFile(resultFile);
        return new SqlResultFileWrapper(parsedResultFile);
    }

    private QueryExecutor getQueryExecutor(SqlQueryFileWrapper sqlQueryFileWrapper)
    {
        String database = sqlQueryFileWrapper.getDatabaseName();
        try {
            return testContext().getDependency(QueryExecutor.class, database);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("Cannot get query executor for database '" + database + "'", e);
        }
    }

    private QueryResult executeQuery(QueryExecutor queryExecutor, String query, Optional<QueryType> queryType)
    {
        if (!queryType.isPresent()) {
            return queryExecutor.executeQuery(query);
        }

        switch (queryType.get()) {
            case SELECT:
                return queryExecutor.executeSelect(query);
            case UPDATE:
                return queryExecutor.executeUpdate(query);
            default:
                throw new IllegalArgumentException("Invalid query type");
        }
    }
}
