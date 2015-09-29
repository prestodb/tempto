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

package com.teradata.tempto.internal.convention.sql;

import com.google.common.base.Splitter;
import com.teradata.tempto.Requirement;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.fulfillment.table.TableDefinitionsRepository;
import com.teradata.tempto.internal.convention.ConventionBasedTest;
import com.teradata.tempto.internal.convention.SqlQueryDescriptor;
import com.teradata.tempto.internal.convention.SqlResultDescriptor;
import com.teradata.tempto.query.QueryExecutor;
import com.teradata.tempto.query.QueryResult;
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
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.teradata.tempto.assertions.QueryAssert.assertThat;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.tempto.fulfillment.table.MutableTablesState.mutableTablesState;
import static com.teradata.tempto.internal.convention.ProcessUtils.execute;
import static java.lang.Character.isAlphabetic;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        extends ConventionBasedTest
{
    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private static final Splitter QUERY_SPLITTER = Splitter.onPattern("[;][ ]*\r?\n");

    private final Optional<Path> beforeScriptPath;
    private final Optional<Path> afterScriptPath;
    private final Path queryFile;
    private final String testNamePrefix;
    private final int testNumber;
    private final int queriesCount;
    private final SqlQueryDescriptor queryDescriptor;
    private final SqlResultDescriptor resultDescriptor;
    private final Requirement requirement;

    public SqlQueryConventionBasedTest(Optional<Path> beforeScriptFile, Optional<Path> afterScriptFile,
            Path queryFile, String testNamePrefix, int queryNumber, int queriesCount, SqlQueryDescriptor queryDescriptor, SqlResultDescriptor resultDescriptor,
            Requirement requirement)
    {
        this.beforeScriptPath = beforeScriptFile;
        this.afterScriptPath = afterScriptFile;
        this.queryFile = queryFile;
        this.testNamePrefix = testNamePrefix;
        this.testNumber = queryNumber;
        this.queriesCount = queriesCount;
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
            String sql = resolveTemplates(queryDescriptor.getContent());
            return queryExecutor.executeQuery(sql, queryDescriptor.getQueryType().get());
        }
        else {
            QueryResult queryResult = null;
            List<String> queries = splitQueries(queryDescriptor.getContent());
            checkState(!queries.isEmpty(), "At least one query must be present");

            for (String query : queries) {
                String sql = resolveTemplates(query);
                queryResult = queryExecutor.executeQuery(sql);
            }

            return queryResult;
        }
    }

    private String resolveTemplates(String query)
    {
        try {
            Template template = new Template("name", new StringReader(query), new freemarker.template.Configuration());
            Map<String, Object> data = newHashMap();
            Map<String, Map<String, String>> tableNamesPerDatabase = mutableTablesState().getDatabaseNames().stream()
                    .collect(toMap(databaseName -> databaseName, databaseName -> mutableTablesState().getNameInDatabaseMap(databaseName)));
            data.put("mutableTables", tableNamesPerDatabase);

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
    public String getTestName()
    {
        StringBuilder fullNameBuilder = new StringBuilder();
        fullNameBuilder.append(testNamePrefix);
        fullNameBuilder.append(".");
        String testFileNamePart = FilenameUtils.getBaseName(queryFile.getFileName().toString());
        fullNameBuilder.append(testFileNamePart);
        if (queryDescriptor.getName().isPresent()) {
            fullNameBuilder.append(".");
            fullNameBuilder.append(queryDescriptor.getName().get().replaceAll("\\s", ""));
        } else {
            if (queriesCount > 1) {
                fullNameBuilder.append("_");
                fullNameBuilder.append(testNumber);
            }
        }
        return fullNameBuilder.toString();
    }

    @Override
    public Requirement getRequirements(Configuration configuration)
    {
        return requirement;
    }

    @Override
    public Set<String> getTestGroups()
    {
        return queryDescriptor.getTestGroups();
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
