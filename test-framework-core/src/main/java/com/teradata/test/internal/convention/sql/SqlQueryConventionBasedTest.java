/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.sql;

import com.teradata.test.Requirement;
import com.teradata.test.assertions.QueryAssert;
import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.SqlQueryFileWrapper;
import com.teradata.test.internal.convention.SqlResultFileWrapper;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.internal.convention.ProcessUtils.execute;
import static com.teradata.test.internal.convention.SqlQueryFileWrapper.sqlQueryFileWrapperFor;
import static com.teradata.test.internal.convention.SqlResultFileWrapper.sqlResultFileWrapperFor;
import static java.lang.Character.isAlphabetic;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ConventionBasedTest
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private final Optional<Path> beforeScriptPath;
    private final Optional<Path> afterScriptPath;
    private final Path queryFile;
    private final Path resultFile;
    private final Requirement requirement;

    public SqlQueryConventionBasedTest(Optional<Path> beforeScriptFile, Optional<Path> afterScriptFile,
            Path queryFile, Path resultFile, Requirement requirement)
    {
        this.beforeScriptPath = beforeScriptFile;
        this.afterScriptPath = afterScriptFile;
        this.queryFile = queryFile;
        this.resultFile = resultFile;
        this.requirement = requirement;
    }

    @Override
    public void test()
    {
        LOGGER.debug("Executing sql test: {}", queryFile.getFileName());

        if (beforeScriptPath.isPresent()) {
            execute(beforeScriptPath.get().toString());
        }

        SqlQueryFileWrapper sqlQueryFileWrapper = sqlQueryFileWrapperFor(queryFile);
        SqlResultFileWrapper resultFileWrapper = sqlResultFileWrapperFor(resultFile);

        QueryExecutor queryExecutor = getQueryExecutor(sqlQueryFileWrapper);

        QueryResult queryResult;
        if (sqlQueryFileWrapper.getQueryType().isPresent()) {
            queryResult = queryExecutor.executeQuery(sqlQueryFileWrapper.getContent(), sqlQueryFileWrapper.getQueryType().get());
        }
        else {
            queryResult = queryExecutor.executeQuery(sqlQueryFileWrapper.getContent());
        }

        QueryAssert queryAssert = assertThat(queryResult)
                .hasColumns(resultFileWrapper.getTypes());

        if (resultFileWrapper.isIgnoreOrder()) {
            queryAssert.contains(resultFileWrapper.getRows());
        }
        else {
            queryAssert.containsExactly(resultFileWrapper.getRows());
        }

        if (!resultFileWrapper.isIgnoreExcessRows()) {
            queryAssert.hasRowsCount(resultFileWrapper.getRows().size());
        }

        if (afterScriptPath.isPresent()) {
            execute(afterScriptPath.get().toString());
        }
    }

    @Override
    public String testName()
    {
        String testName = FilenameUtils.getBaseName(queryFile.getParent().toString());
        if (!isAlphabetic(testName.charAt(0))) {
            return "Test" + testName;
        }
        return testName;
    }

    @Override
    public String testCaseName()
    {
        String testCaseName = FilenameUtils.getBaseName(queryFile.getFileName().toString());
        if (!isAlphabetic(testCaseName.charAt(0))) {
            return "test_" + testCaseName;
        }
        return testCaseName;
    }

    @Override
    public Requirement getRequirements()
    {
        return requirement;
    }

    @Override
    public String[] testGroups()
    {
        return sqlQueryFileWrapperFor(queryFile).getTestGroups().toArray(new String[0]);
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
}
