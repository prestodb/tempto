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

package com.teradata.tempto.internal.convention.tabledefinitions;

import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableDefinitionsRepository;
import com.teradata.tempto.fulfillment.table.hive.HiveDataSource;
import com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition;
import com.teradata.tempto.internal.convention.AnnotatedFileParser;
import com.teradata.tempto.internal.convention.ConventionBasedTestFactory;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition.hiveTableDefinition;
import static com.teradata.tempto.internal.convention.ConventionTestsUtils.getConventionsTestsPath;
import static com.teradata.tempto.internal.convention.SqlTestsFileUtils.changeExtension;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newInputStream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class ConventionTableDefinitionsProvider
{
    private static final Logger LOGGER = getLogger(ConventionBasedTestFactory.class);
    public static final String DATASETS_PATH_PART = "datasets";

    public void registerConventionTableDefinitions(TableDefinitionsRepository tableDefinitionsRepository)
    {
        getAllConventionBasedTableDefinitions().stream().forEach(tableDefinitionsRepository::register);
    }

    private List<TableDefinition> getAllConventionBasedTableDefinitions()
    {
        Optional<Path> dataSetsPath = getConventionsTestsPath(DATASETS_PATH_PART);
        if (!dataSetsPath.isPresent()) {
            LOGGER.debug("No convention table definitions");
            return emptyList();
        }
        else {
            return getAllConventionTableDefinitionDescriptors(dataSetsPath.get())
                    .stream()
                    .map(this::tableDefinitionFor)
                    .collect(toList());
        }
    }


    private List<ConventionTableDefinitionDescriptor> getAllConventionTableDefinitionDescriptors(Path dataSetsPath)
    {
        if (exists(dataSetsPath)) {
            LOGGER.debug("Data sets configuration for path: {}", dataSetsPath);

            try {
                return StreamSupport.stream(newDirectoryStream(dataSetsPath, "*.ddl").spliterator(), false)
                        .map(ddlFile -> new ConventionTableDefinitionDescriptor(ddlFile, changeExtension(ddlFile, "data"), changeExtension(ddlFile, "data-revision")))
                        .collect(toList());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return emptyList();
        }
    }

    private TableDefinition tableDefinitionFor(ConventionTableDefinitionDescriptor tableDefinitionDescriptor)
    {
        ConventionTableDefinitionDescriptor.ParsedDDLFile parsedDDLFile = tableDefinitionDescriptor.getParsedDDLFile();
        switch (parsedDDLFile.getTableType()) {
            case HIVE:
                return hiveTableDefinitionFor(tableDefinitionDescriptor);
            case JDBC:
                return jdbcTableDefinitionFor(tableDefinitionDescriptor);
            default:
                throw new IllegalArgumentException("unknown table type: " + parsedDDLFile.getTableType());
        }
    }

    private HiveTableDefinition hiveTableDefinitionFor(ConventionTableDefinitionDescriptor tableDefinitionDescriptor)
    {
        HiveDataSource dataSource = new FileBasedHiveDataSource(tableDefinitionDescriptor);
        return hiveTableDefinition(tableDefinitionDescriptor.getName(), tableDefinitionDescriptor.getParsedDDLFile().getContent(), dataSource);
    }

    private TableDefinition jdbcTableDefinitionFor(ConventionTableDefinitionDescriptor tableDefinitionDescriptor)
    {
        JdbcTableDataSource dataSource= new FileBasedJdbcDataSource(tableDefinitionDescriptor);
        return JdbcTableDefinition.jdbcTableDefinition(tableDefinitionDescriptor.getName(), tableDefinitionDescriptor.getParsedDDLFile().getContent(), dataSource);
    }
}
