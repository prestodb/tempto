/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import com.teradata.test.ProductTest;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.assertions.QueryAssert;
import com.teradata.test.convention.FileParser.ParsingResult;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import org.slf4j.Logger;
import org.testng.ITest;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ProductTest
        implements RequirementsProvider, ITest
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);
    private static final int SUCCESS_EXIT_CODE = 0;

    private final FileParser fileParser;
    private final String testCaseName;
    private final Optional<File> beforeScriptFile;
    private final Optional<File> afterScriptFile;
    private final File queryFile;
    private final File resultFile;
    private final Requirement requirement;

    public SqlQueryConventionBasedTest(String testCaseName, Optional<File> beforeScriptFile, Optional<File> afterScriptFile,
            File queryFile, File resultFile, Requirement requirement)
    {
        this.testCaseName = testCaseName;
        this.beforeScriptFile = beforeScriptFile;
        this.afterScriptFile = afterScriptFile;
        this.queryFile = queryFile;
        this.resultFile = resultFile;
        this.requirement = requirement;
        this.fileParser = new FileParser();
    }

    @Test(groups = "sql_tests")
    public void test()
            throws IOException, InterruptedException
    {
        LOGGER.debug("Executing sql test: {}", getTestName());

        if (beforeScriptFile.isPresent()) {
            execute(beforeScriptFile.get());
        }

        try (
                InputStream queryInput = new BufferedInputStream(new FileInputStream(queryFile));
                InputStream resultInput = new BufferedInputStream(new FileInputStream(resultFile))
        ) {
            ParsingResult queryFile = fileParser.parseFile(queryInput);
            ParsingResult resultFile = fileParser.parseFile(resultInput);
            SqlResultFileWrapper resultFileWrapper = new SqlResultFileWrapper(resultFile);

            QueryExecutor queryExecutor = getQueryExecutor(queryFile);
            QueryResult result = queryExecutor.executeQuery(queryFile.getContent());

            QueryAssert queryAssert = assertThat(result)
                    .hasRowsCount(resultFile.getContentLines().size())
                    .hasColumns(resultFileWrapper.getTypes());

            if (resultFileWrapper.isIgnoreOrder()) {
                queryAssert.hasRows(resultFileWrapper.getRows());
            }
            else {
                queryAssert.hasRowsInOrder(resultFileWrapper.getRows());
            }
        }

        if (afterScriptFile.isPresent()) {
            execute(afterScriptFile.get());
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

    private void execute(File file)
            throws IOException, InterruptedException
    {
        Process process = Runtime.getRuntime().exec(file.toString());
        process.waitFor();
        checkState(process.exitValue() == SUCCESS_EXIT_CODE, file.toString() + " exited with status code: " + process.exitValue());
    }

    private QueryExecutor getQueryExecutor(ParsingResult queryFile)
    {
        QueryExecutor queryExecutor;
        Optional<String> database = queryFile.getProperty("database");
        if (database.isPresent()) {
            queryExecutor = testContext().getDependency(QueryExecutor.class, database.get());
        }
        else {
            queryExecutor = testContext().getDependency(QueryExecutor.class);
        }
        return queryExecutor;
    }
}
