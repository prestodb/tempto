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
package com.teradata.tempto.fulfillment.table;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.teradata.tempto.internal.convention.tabledefinitions.ConventionTableDefinitionsProvider;
import com.teradata.tempto.internal.ReflectionHelper;
import com.teradata.tempto.spi.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.teradata.tempto.internal.ReflectionHelper.getFieldsAnnotatedWith;
import static com.teradata.tempto.internal.ReflectionHelper.getSubTypesOf;
import static java.util.stream.Collectors.toList;

/**
 * Stores {@link com.teradata.tempto.fulfillment.table.TableDefinition} mapped by names.
 */
public class TableDefinitionsRepository
{

    protected static final Logger LOGGER = LoggerFactory.getLogger(TableDefinitionsRepository.class);

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

    private static final Map<String, Plugin> SCANNED_PLUGINS;

    private static final TableDefinitionsRepository TABLE_DEFINITIONS_REPOSITORY;

    static {
        // ExceptionInInitializerError is not always appropriately logged, lets log exceptions explicitly here
        try {
            SCANNED_TABLE_DEFINITIONS =
                    getFieldsAnnotatedWith(RepositoryTableDefinition.class)
                            .stream()
                            .map(ReflectionHelper::<TableDefinition>getStaticFieldValue)
                            .collect(toList());

            Set<Class<? extends Plugin>> plugins = getSubTypesOf(Plugin.class);
            ImmutableMap.Builder<String, Plugin> builder = ImmutableMap.builder();
            for (Class<? extends Plugin> plugin : plugins) {
                try {
                    Constructor<? extends Plugin> constructor = plugin.getConstructor();
                    Plugin p = constructor.newInstance();
                    builder.put(p.getName(), p);
                }
                catch (NoSuchMethodException e) {
                    LOGGER.warn("Plugin %s does not have appropriate constructor. Skipping", plugin);
                }
                catch (IllegalAccessException|InstantiationException|InvocationTargetException e) {
                    LOGGER.warn("Cannot instantiate plugin %s: %s. Skipping.", plugin, e);
                }
            }
            SCANNED_PLUGINS = builder.build();

            TABLE_DEFINITIONS_REPOSITORY = new TableDefinitionsRepository(SCANNED_TABLE_DEFINITIONS);
            // TODO: since TestNG has no listener that can be run before tests factory, this has to be initialized here
            new ConventionTableDefinitionsProvider(SCANNED_PLUGINS).registerConventionTableDefinitions(TABLE_DEFINITIONS_REPOSITORY);
        } catch (RuntimeException e){
            LOGGER.error("Error during TableDefinitionsRepository initialization", e);
            throw e;
        }
    }

    public static <T extends TableDefinition> T registerTableDefinition(T tableDefinition)
    {
        return tableDefinitionsRepository().register(tableDefinition);
    }

    public static TableDefinition tableDefinitionForName(String name)
    {
        return tableDefinitionsRepository().getForName(name);
    }

    public static TableDefinitionsRepository tableDefinitionsRepository()
    {
        return TABLE_DEFINITIONS_REPOSITORY;
    }

    private final Map<String, TableDefinition> tableDefinitions = new MapMaker().makeMap();

    public TableDefinitionsRepository()
    {
    }

    public TableDefinitionsRepository(Collection<TableDefinition> tableDefinitions)
    {
        tableDefinitions.stream().forEach(this::register);
    }

    public <T extends TableDefinition> T register(T tableDefinition)
    {
        checkState(!tableDefinitions.containsKey(tableDefinition.getName()), "duplicated table definition: %s", tableDefinition.getName());
        tableDefinitions.put(tableDefinition.getName(), tableDefinition);
        return tableDefinition;
    }

    public TableDefinition getForName(String name)
    {
        checkState(tableDefinitions.containsKey(name), "no table definition for: %s", name);
        return tableDefinitions.get(name);
    }
}
