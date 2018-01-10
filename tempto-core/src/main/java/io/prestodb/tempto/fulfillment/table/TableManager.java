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

import io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State;
import io.prestodb.tempto.internal.fulfillment.table.TableName;

import java.io.Closeable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static io.prestodb.tempto.fulfillment.table.TableManagerDispatcher.getTableManagerDispatcher;

/**
 * Provides functionality of creating/dropping tables based on {@link TableDefinition}.
 */
public interface TableManager<T extends TableDefinition>
        extends Closeable
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @interface Descriptor
    {
        Class<? extends TableDefinition> tableDefinitionClass();

        String type();
    }

    default TableInstance<T> createImmutable(T tableDefinition)
    {
        return createImmutable(tableDefinition, tableDefinition.getTableHandle());
    }

    TableInstance<T> createImmutable(T tableDefinition, TableHandle tableHandle);

    default TableInstance<T> createMutable(T tableDefinition)
    {
        return createMutable(tableDefinition, LOADED);
    }

    default TableInstance<T> createMutable(T tableDefinition, State state)
    {
        return createMutable(tableDefinition, state, tableDefinition.getTableHandle());
    }

    TableInstance<T> createMutable(T tableDefinition, State state, TableHandle tableHandle);

    void dropTable(TableName tableName);

    void dropStaleMutableTables();

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

    default void close()
    {
    }
}
