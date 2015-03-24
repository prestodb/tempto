/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import com.teradata.test.Requires;
import com.teradata.test.assertions.QueryAssert;
import com.teradata.test.convention.FileParser.ParsingResult;
import com.teradata.test.initialization.TestInitializationListener;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import org.slf4j.Logger;
import org.testng.ITest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static org.slf4j.LoggerFactory.getLogger;

@Listeners(TestInitializationListener.class)
public class SqlQueryConventionBasedTest
        implements ITest
{

    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private final String testName;
    private final File queryFile;
    private final File resultFile;
    private final FileParser fileParser;

    public SqlQueryConventionBasedTest(String testName, File queryFile, File resultFile)
    {
        this.testName = testName;
        this.queryFile = queryFile;
        this.resultFile = resultFile;
        this.fileParser = new FileParser();
    }

    // TODO: create something like RequirementAware interface and remove this annotation
    @Requires(SqlQueryConventionBasedTestRequirement.class)
    @Test(groups = "sql_tests")
    public void test()
            throws IOException
    {
        LOGGER.debug("Executing sql test: {}", getTestName());

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

    @Override
    public String getTestName()
    {
        return testName + "." + queryFile.getName();
    }
}
