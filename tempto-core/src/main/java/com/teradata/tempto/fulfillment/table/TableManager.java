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

import com.teradata.tempto.fulfillment.table.MutableTableRequirement.State;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static com.teradata.tempto.fulfillment.table.TableManagerDispatcher.getTableManagerDispatcher;

/**
 * Provides functionality of creating/dropping tables based on {@link TableDefinition}.
 */
public interface TableManager<T extends TableDefinition>
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @interface Descriptor
    {

        Class<? extends TableDefinition> tableDefinitionClass();
        String type();

    }
    TableInstance<T> createImmutable(T tableDefinition);

    default TableInstance<T> createMutable(T tableDefinition)
    {
        return createMutable(tableDefinition, LOADED);
    }

    default TableInstance<T> createMutable(T tableDefinition, State state)
    {
        return createMutable(tableDefinition, state, tableDefinition.getName());
    }

    TableInstance<T> createMutable(T tableDefinition, State state, String name);

    void dropTable(String nameInDatabase);

    void dropAllMutableTables();

    static <T extends TableDefinition> TableInstance<T> createImmutableTable(T tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createImmutable(tableDefinition);
    }

    static <T extends TableDefinition> TableInstance<T> createMutableTable(T tableDefinition, State state)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createMutable(tableDefinition, state);
    }

    static <T extends TableDefinition> TableInstance<T> createMutableTable(T tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createMutable(tableDefinition);
    }

    String getDatabaseName();

    Class<? extends TableDefinition> getTableDefinitionClass();
}
