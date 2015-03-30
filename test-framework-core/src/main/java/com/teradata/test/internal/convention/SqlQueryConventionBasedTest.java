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

import static com.google.common.collect.Sets.newHashSet;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.internal.convention.ProcessUtils.execute;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ProductTest
        implements RequirementsProvider, WithName, WithTestGroups
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private static final String GROUPS_HEADER_PROPERTY = "groups";
    private static final Splitter GROUPS_HEADER_PROPERTY_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

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
            ParsingResult parsedQueryFile = headerFileParser.parseFile(queryFile);
            String groupsProperty = parsedQueryFile.getProperty(GROUPS_HEADER_PROPERTY).orElse("");
            return newHashSet(GROUPS_HEADER_PROPERTY_SPLITTER.split(groupsProperty));
        }
        catch (IOException e) {
            throw new RuntimeException("cannot parse query file", e);
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
}
