/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.google.common.base.Splitter;
import com.teradata.test.ProductTest;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.assertions.QueryAssert;
import com.teradata.test.internal.convention.HeaderFileParser.ParsingResult;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import com.teradata.test.testmarkers.WithName;
import com.teradata.test.testmarkers.WithTestGroups;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ProductTest
        implements RequirementsProvider, WithName, WithTestGroups
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);
    private static final int SUCCESS_EXIT_CODE = 0;

    private static final String GROUPS_HEADER_PROPERTY = "groups";
    private static final Splitter GROUPS_HEADER_PROPERTY_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private final HeaderFileParser headerFileParser;
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
        this.headerFileParser = new HeaderFileParser();
    }

    @Test
    public void test()
            throws IOException, InterruptedException
    {
        LOGGER.debug("Executing sql test: {}", getTestName());

        if (beforeScriptFile.isPresent()) {
            execute(beforeScriptFile.get());
        }

        ParsingResult parsedQueryFile = headerFileParser.parseFile(queryFile);
        ParsingResult parsedResultFile = headerFileParser.parseFile(resultFile);
        SqlResultFileWrapper resultFileWrapper = new SqlResultFileWrapper(parsedResultFile);

        QueryExecutor queryExecutor = getQueryExecutor(parsedQueryFile);
        QueryResult result = queryExecutor.executeQuery(parsedQueryFile.getContent());

        QueryAssert queryAssert = assertThat(result)
                .hasRowsCount(parsedResultFile.getContentLines().size())
                .hasColumns(resultFileWrapper.getTypes());

        if (resultFileWrapper.isIgnoreOrder()) {
            queryAssert.hasRows(resultFileWrapper.getRows());
        }
        else {
            queryAssert.hasRowsInOrder(resultFileWrapper.getRows());
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

    @Override
    public Set<String> getTestGroups()
    {
        try {
            ParsingResult parsedQueryFile = headerFileParser.parseFile(queryFile);
            String groupsProperty = parsedQueryFile.getProperty(GROUPS_HEADER_PROPERTY).orElse("");
            return newHashSet(GROUPS_HEADER_PROPERTY_SPLITTER.split(groupsProperty));
        }
        catch (IOException e) {
            throw new RuntimeException("cannot parse query file", e);
        }
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
