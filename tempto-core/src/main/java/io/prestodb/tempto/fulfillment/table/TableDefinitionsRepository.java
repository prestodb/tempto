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
package io.prestodb.tempto.fulfillment.table;

import com.google.common.collect.MapMaker;
import io.prestodb.tempto.internal.ReflectionHelper;
import io.prestodb.tempto.internal.convention.tabledefinitions.ConventionTableDefinitionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static io.prestodb.tempto.internal.ReflectionHelper.getFieldsAnnotatedWith;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Stores {@link TableDefinition} mapped by names.
 */
public class TableDefinitionsRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TableDefinitionsRepository.class);

    /**
     * An annotation for {@link TableDefinition} static fields
     * that should be registered in {@link TableDefinitionsRepository}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RepositoryTableDefinition
    {
    }

    private static final List<TableDefinition> SCANNED_TABLE_DEFINITIONS;

    private static final TableDefinitionsRepository TABLE_DEFINITIONS_REPOSITORY;

    static {
        // ExceptionInInitializerError is not always appropriately logged, lets log exceptions explicitly here
        try {
            SCANNED_TABLE_DEFINITIONS =
                    getFieldsAnnotatedWith(RepositoryTableDefinition.class)
                            .stream()
                            .map(ReflectionHelper::<TableDefinition>getStaticFieldValue)
                            .collect(toList());

            TABLE_DEFINITIONS_REPOSITORY = new TableDefinitionsRepository(SCANNED_TABLE_DEFINITIONS);
            // TODO: since TestNG has no listener that can be run before tests factory, this has to be initialized here
            new ConventionTableDefinitionsProvider().registerConventionTableDefinitions(TABLE_DEFINITIONS_REPOSITORY);
        }
        catch (RuntimeException e) {
            LOGGER.error("Error during TableDefinitionsRepository initialization", e);
            throw e;
        }
    }

    public static <T extends TableDefinition> T registerTableDefinition(T tableDefinition)
    {
        return tableDefinitionsRepository().register(tableDefinition);
    }

    public static TableDefinition tableDefinition(TableHandle tableHandle)
    {
        return tableDefinitionsRepository().get(tableHandle);
    }

    public static TableDefinitionsRepository tableDefinitionsRepository()
    {
        return TABLE_DEFINITIONS_REPOSITORY;
    }

    private final Map<TableDefinitionRepositoryKey, TableDefinition> tableDefinitions = new MapMaker().makeMap();

    public TableDefinitionsRepository()
    {
    }

    public TableDefinitionsRepository(Collection<TableDefinition> tableDefinitions)
    {
        tableDefinitions.stream().forEach(this::register);
    }

    public <T extends TableDefinition> T register(T tableDefinition)
    {
        TableDefinitionRepositoryKey repositoryKey = asRepositoryKey(tableDefinition.getTableHandle());
        checkState(!tableDefinitions.containsKey(repositoryKey), "duplicated table definition: %s", repositoryKey);
        tableDefinitions.put(repositoryKey, tableDefinition);
        return tableDefinition;
    }

    public TableDefinition get(TableHandle tableHandle)
    {
        TableDefinitionRepositoryKey tableHandleKey = asRepositoryKey(tableHandle);
        TableDefinitionRepositoryKey nameKey = asRepositoryKey(tableHandle.getName());
        if (tableDefinitions.containsKey(tableHandleKey)) {
            return tableDefinitions.get(tableHandleKey);
        }
        else if (tableDefinitions.containsKey(nameKey)) {
            return tableDefinitions.get(nameKey);
        }
        throw new IllegalStateException("no table definition for: " + tableHandleKey);
    }

    private static TableDefinitionRepositoryKey asRepositoryKey(TableHandle tableHandle)
    {
        return new TableDefinitionRepositoryKey(tableHandle.getName(), tableHandle.getSchema());
    }

    private static TableDefinitionRepositoryKey asRepositoryKey(String name)
    {
        return new TableDefinitionRepositoryKey(name, Optional.empty());
    }

    private static class TableDefinitionRepositoryKey
    {
        private final String name;
        private final Optional<String> schema;

        public TableDefinitionRepositoryKey(String name, Optional<String> schema)
        {
            this.name = requireNonNull(name, "name is null");
            this.schema = requireNonNull(schema, "schema is null");
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TableDefinitionRepositoryKey that = (TableDefinitionRepositoryKey) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(schema, that.schema);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, schema);
        }

        @Override
        public String toString()
        {
            if (schema.isPresent()) {
                return schema.get() + "." + name;
            }
            return name;
        }
    }
}
