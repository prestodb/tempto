/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.test.internal.convention.sql;

import com.google.common.base.Splitter;
import com.teradata.test.Requirement;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.convention.SqlResultDescriptor;
import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.SqlQueryDescriptor;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.teradata.test.assertions.QueryAssert.assertThat;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.fulfillment.table.MutableTablesState.mutableTablesState;
import static com.teradata.test.internal.convention.ProcessUtils.execute;
import static java.lang.Character.isAlphabetic;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ConventionBasedTest
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private static final Splitter QUERY_SPLITTER = Splitter.onPattern("[;][ ]*\r?\n");

    private final Optional<Path> beforeScriptPath;
    private final Optional<Path> afterScriptPath;
    private final Path queryFile;
    private final int testNumber;
    private final SqlQueryDescriptor queryDescriptor;
    private final SqlResultDescriptor resultDescriptor;
    private final Requirement requirement;

    public SqlQueryConventionBasedTest(Optional<Path> beforeScriptFile, Optional<Path> afterScriptFile,
            Path queryFile, int testNumber, SqlQueryDescriptor queryDescriptor, SqlResultDescriptor resultDescriptor,
            Requirement requirement)
    {
        this.beforeScriptPath = beforeScriptFile;
        this.afterScriptPath = afterScriptFile;
        this.queryFile = queryFile;
        this.testNumber = testNumber;
        this.queryDescriptor = queryDescriptor;
        this.resultDescriptor = resultDescriptor;
        this.requirement = requirement;
    }

    @Override
    public void test()
    {
        LOGGER.debug("Executing sql test: {}#{}", queryFile.getFileName(), queryDescriptor.getName());

        if (beforeScriptPath.isPresent()) {
            execute(beforeScriptPath.get().toString());
        }

        QueryResult queryResult = runTestQuery();
        assertThat(queryResult).matches(resultDescriptor);

        if (afterScriptPath.isPresent()) {
            execute(afterScriptPath.get().toString());
        }
    }

    private QueryResult runTestQuery()
    {
        QueryExecutor queryExecutor = getQueryExecutor(queryDescriptor);
        if (queryDescriptor.getQueryType().isPresent()) {
            return queryExecutor.executeQuery(resolveTemplates(queryDescriptor.getContent()), queryDescriptor.getQueryType().get());
        }
        else {
            QueryResult queryResult = null;
            List<String> queries = splitQueries(queryDescriptor.getContent());
            checkState(!queries.isEmpty(), "At least one query must be present");

            for (String query : queries) {
                queryResult = queryExecutor.executeQuery(resolveTemplates(query));
            }

            return queryResult;
        }
    }

    private String resolveTemplates(String query)
    {
        try {
            Template template = new Template("name", new StringReader(query), new freemarker.template.Configuration());
            Map<String, Object> data = newHashMap();
            data.put("mutableTables", mutableTablesState().getNameInDatabaseMap());

            Writer writer = new StringWriter();
            template.process(data, writer);
            writer.flush();

            return writer.toString();
        }
        catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> splitQueries(String content)
    {
        return newArrayList(QUERY_SPLITTER.split(content))
                .stream()
                .filter(query -> !query.isEmpty())
                .collect(toList());
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
        String testCaseName;
        if (queryDescriptor.getName().isPresent()) {
            testCaseName = queryDescriptor.getName().get().replaceAll("\\s", "");
        }
        else {
            testCaseName = FilenameUtils.getBaseName(queryFile.getFileName().toString()) + "_" + testNumber;
        }

        if (!isAlphabetic(testCaseName.charAt(0))) {
            return "test_" + testCaseName;
        }

        return testCaseName;
    }

    @Override
    public Requirement getRequirements(Configuration configuration)
    {
        return requirement;
    }

    @Override
    public String[] testGroups()
    {
        return queryDescriptor.getTestGroups().toArray(new String[0]);
    }

    private QueryExecutor getQueryExecutor(SqlQueryDescriptor sqlQueryDescriptor)
    {
        String database = sqlQueryDescriptor.getDatabaseName();
        try {
            return testContext().getDependency(QueryExecutor.class, database);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("Cannot get query executor for database '" + database + "'", e);
        }
    }
}
