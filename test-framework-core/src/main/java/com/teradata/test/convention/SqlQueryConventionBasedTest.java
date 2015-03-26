/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import com.teradata.test.ProductTest;
import com.teradata.test.Requirement;
import com.teradata.test.assertions.QueryAssert;
import com.teradata.test.convention.FileParser.ParsingResult;
import com.teradata.test.initialization.TestMethodRequirementsProvider;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import org.slf4j.Logger;
import org.testng.ITest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ProductTest
        implements TestMethodRequirementsProvider, ITest
{

    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private final FileParser fileParser;
    private final List<SqlQueryConventionBasedTestCaseDefinition> testCases;

    private String currentTestCaseName;

    public SqlQueryConventionBasedTest(List<SqlQueryConventionBasedTestCaseDefinition> testCases)
    {
        this.testCases = testCases;
        this.fileParser = new FileParser();
    }

    @BeforeMethod(groups = "sql_tests")
    public void beforeMethod(Object[] parameters)
    {
        SqlQueryConventionBasedTestCaseDefinition testCase = (SqlQueryConventionBasedTestCaseDefinition) parameters[0];
        currentTestCaseName = testCase.testCaseName;
    }

    @Test(groups = "sql_tests", dataProvider = "testCasesDataProvider")
    public void test(final SqlQueryConventionBasedTestCaseDefinition testCase)
            throws IOException
    {
        LOGGER.debug("Executing sql test: {}", getTestName());

        try (
                InputStream queryInput = new BufferedInputStream(new FileInputStream(testCase.queryFile));
                InputStream resultInput = new BufferedInputStream(new FileInputStream(testCase.resultFile))
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

    @Override
    public Requirement getRequirements(Method testMethod, Object[] parameters)
    {
        SqlQueryConventionBasedTestCaseDefinition testCase = (SqlQueryConventionBasedTestCaseDefinition) parameters[0];
        return testCase.requirement;
    }

    @Override
    public Requirement getAllRequirements()
    {
        List<Requirement> requirements = newArrayList();
        for (SqlQueryConventionBasedTestCaseDefinition testCase : testCases) {
            requirements.add(testCase.requirement);
        }
        return compose(requirements);
    }

    @Override
    public String getTestName()
    {
        return currentTestCaseName;
    }

    @DataProvider(name = "testCasesDataProvider")
    public Object[][] testCasesDataProvider()
    {
        Object[][] parameters = new Object[testCases.size()][1];
        for (int i = 0; i < testCases.size(); ++i) {
            parameters[i][0] = testCases.get(i);
        }
        return parameters;
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
