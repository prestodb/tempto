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
public interface TableManager
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @interface Descriptor
    {
        Class<? extends TableDefinition> tableDefinitionClass();
        String type();
    }

    TableInstance createImmutable(TableDefinition tableDefinition);

    TableInstance createMutable(TableDefinition tableDefinition, State state);

    default TableInstance createMutable(TableDefinition tableDefinition)
    {
        return createMutable(tableDefinition, LOADED);
    }

    void dropAllTables();

    public static TableInstance createImmutableTable(TableDefinition tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createImmutable(tableDefinition);
    }

    public static TableInstance createMutableTable(TableDefinition tableDefinition, State state)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createMutable(tableDefinition, state);
    }

    public static TableInstance createMutableTable(TableDefinition tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createMutable(tableDefinition);
    }
}
