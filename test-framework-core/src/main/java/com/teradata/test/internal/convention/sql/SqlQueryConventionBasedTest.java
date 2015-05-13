/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.sql;

import com.teradata.test.Requirement;
import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.SqlQueryFile;
import com.teradata.test.convention.SqlResultFile;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.internal.convention.ProcessUtils.execute;
import static com.teradata.test.internal.convention.SqlQueryFile.sqlQueryFileFor;
import static com.teradata.test.convention.SqlResultFile.sqlResultFileFor;
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

        SqlQueryFile sqlQueryFile = sqlQueryFileFor(queryFile);
        SqlResultFile sqlResultFile = sqlResultFileFor(resultFile);

        QueryExecutor queryExecutor = getQueryExecutor(sqlQueryFile);

        QueryResult queryResult;
        if (sqlQueryFile.getQueryType().isPresent()) {
            queryResult = queryExecutor.executeQuery(sqlQueryFile.getContent(), sqlQueryFile.getQueryType().get());
        }
        else {
            queryResult = queryExecutor.executeQuery(sqlQueryFile.getContent());
        }

        assertThat(queryResult).matchesFile(sqlResultFile);

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
        return sqlQueryFileFor(queryFile).getTestGroups().toArray(new String[0]);
    }

    private QueryExecutor getQueryExecutor(SqlQueryFile sqlQueryFile)
    {
        String database = sqlQueryFile.getDatabaseName();
        try {
            return testContext().getDependency(QueryExecutor.class, database);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("Cannot get query executor for database '" + database + "'", e);
        }
    }
}
