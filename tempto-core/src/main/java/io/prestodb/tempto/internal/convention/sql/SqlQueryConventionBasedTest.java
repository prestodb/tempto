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

package io.prestodb.tempto.internal.convention.sql;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.assertions.QueryAssert;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.internal.convention.ConventionBasedTest;
import io.prestodb.tempto.internal.convention.SqlQueryDescriptor;
import io.prestodb.tempto.internal.convention.SqlResultDescriptor;
import io.prestodb.tempto.query.QueryExecutor;
import io.prestodb.tempto.query.QueryResult;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.JDBCType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static io.prestodb.tempto.assertions.QueryAssert.assertThat;
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.testContext;
import static io.prestodb.tempto.fulfillment.table.MutableTablesState.mutableTablesState;
import static io.prestodb.tempto.internal.convention.ConventionTestsUtils.getConventionTestResultsDumpPath;
import static io.prestodb.tempto.internal.convention.ProcessUtils.execute;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.stream.Collectors.joining;
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
        QueryResult queryResult = null;
        List<String> queries = splitQueries(queryDescriptor.getContent());
        checkState(!queries.isEmpty(), "At least one query must be present");

        for (String query : queries) {
            String sql = resolveTemplates(query);
            queryResult = queryExecutor.executeQuery(sql);
        }
        dumpResultsIfNeeded(queryResult);

        return queryResult;
    }

    private void dumpResultsIfNeeded(QueryResult queryResult)
    {
        getConventionTestResultsDumpPath().ifPresent(path -> {
            try {
                dumpResults(queryResult, path);
            }
            catch (IOException e) {
                throw Throwables.propagate(e);
            }
        });
    }

    private void dumpResults(QueryResult queryResult, Path path)
            throws IOException
    {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        checkState(Files.isDirectory(path), "%s have to point to the directory", path);
        String testFileName = queryFile.getFileName().toString();
        String resultsFileName = testFileName.substring(0, testFileName.lastIndexOf(".")) + ".result";

        Path resultFilePath = Paths.get(path.toString(), resultsFileName);
        try (BufferedWriter writer = newBufferedWriter(resultFilePath)) {
            String types = queryResult.getColumnTypes().stream().map(JDBCType::getName).collect(joining("|"));
            writer.write("-- delimiter: |; types: " + types);
            writer.newLine();
            for (List<Object> row : queryResult.rows()) {
                writer.write(new QueryAssert.Row(row).toString());
                writer.newLine();
            }
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
        }
        else {
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
