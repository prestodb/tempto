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
import com.teradata.test.internal.convention.SqlResultFileWrapper;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.Optional;

import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.internal.convention.ProcessUtils.execute;
import static java.lang.Character.isAlphabetic;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ConventionBasedTest
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private final HeaderFileParser headerFileParser;
    private final Optional<File> beforeScriptPath;
    private final Optional<File> afterScriptPath;
    private final File queryFile;
    private final File resultFile;
    private final Requirement requirement;

    public SqlQueryConventionBasedTest(Optional<File> beforeScriptPath, Optional<File> afterScriptPath,
            File queryFile, File resultFile, Requirement requirement)
    {
        this.beforeScriptPath = beforeScriptPath;
        this.afterScriptPath = afterScriptPath;
        this.queryFile = queryFile;
        this.resultFile = resultFile;
        this.requirement = requirement;
        this.headerFileParser = new HeaderFileParser();
    }

    @Override
    public void test()
    {
        LOGGER.debug("Executing sql test: {}", queryFile.getName());

        if (beforeScriptPath.isPresent()) {
            execute(beforeScriptPath.get().toString());
        }

        SqlQueryFileWrapper sqlQueryFileWrapper = getSqlQueryFileWrapper();
        SqlResultFileWrapper resultFileWrapper = getSqlResultFileWrapper();

        QueryExecutor queryExecutor = getQueryExecutor(sqlQueryFileWrapper);

        QueryResult queryResult = queryExecutor.executeQuery(sqlQueryFileWrapper.getContent(), sqlQueryFileWrapper.getQueryType());

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
        String testName = FilenameUtils.getBaseName(queryFile.getParent());
        if (!isAlphabetic(testName.charAt(0))) {
            return "Test" + testName;
        }
        return testName;
    }

    @Override
    public String testCaseName()
    {
        return FilenameUtils.getBaseName(queryFile.getName());
    }

    @Override
    public Requirement getRequirements()
    {
        return requirement;
    }

    @Override
    public String[] testGroups()
    {
        return getSqlQueryFileWrapper().getTestGroups().toArray(new String[0]);
    }

    private SqlQueryFileWrapper getSqlQueryFileWrapper()
    {
        return new SqlQueryFileWrapper(headerFileParser.parseFile(queryFile));
    }

    private SqlResultFileWrapper getSqlResultFileWrapper()
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
}
