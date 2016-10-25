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
import com.teradata.tempto.internal.convention.ConventionBasedTestFactory;
import com.teradata.tempto.spi.Plugin;
import com.teradata.tempto.spi.convention.tabledefinitions.ConventionTableDefinitionDescriptor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.teradata.tempto.internal.convention.ConventionTestsUtils.getConventionsTestsPath;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class ConventionTableDefinitionsProvider
{
    public static final String DATASETS_PATH_PART = "datasets";
    private static final Logger LOGGER = getLogger(ConventionBasedTestFactory.class);
    private final Map<String, Plugin> plugins;

    public ConventionTableDefinitionsProvider(Map<String, Plugin> plugins)
    {
        this.plugins = requireNonNull(plugins, "plugins is null");
    }

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
                        .map(ddlFile -> new ConventionTableDefinitionDescriptor(ddlFile))
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
        TableType tableType = parsedDDLFile.getTableType();
        Plugin plugin = plugins.get(tableType.toString().toLowerCase(Locale.ENGLISH));
        if (plugin == null) {
            throw new IllegalStateException(format("Plugin for %s is not loaded", tableType));
        }
        return plugin.getConventionTableDefinition(tableDefinitionDescriptor);
    }
}
