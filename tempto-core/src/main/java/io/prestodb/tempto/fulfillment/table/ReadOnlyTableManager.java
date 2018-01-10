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

import com.google.inject.Inject;
import io.prestodb.tempto.internal.fulfillment.table.TableName;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Table manager for read only databases.
 */
@TableManager.Descriptor(tableDefinitionClass = ReadOnlyTableManager.ReadOnlyTableDefinition.class, type = ReadOnlyTableManager.TYPE)
@Singleton
public class ReadOnlyTableManager<T extends TableDefinition>
        implements TableManager<T>
{
    public static final String TYPE = "READ_ONLY";

    private final String databaseName;

    @Inject
    public ReadOnlyTableManager(@Named("databaseName") String databaseName)
    {
        this.databaseName = databaseName;
    }

    @Override
    public TableInstance<T> createImmutable(T tableDefinition, TableHandle tableHandle)
    {
        throw new IllegalStateException("Please set non read-only table manager to create table for database " + databaseName);
    }

    @Override
    public TableInstance<T> createMutable(T tableDefinition, MutableTableRequirement.State state, TableHandle tableHandle)
    {
        throw new IllegalStateException("Please set non read-only table manager to create table for database " + databaseName);
    }

    @Override
    public void dropTable(TableName tableName)
    {
        throw new IllegalStateException("Please set non read-only table manager to drop table for database " + databaseName);
    }

    @Override
    public void dropStaleMutableTables() {}

    @Override
    public String getDatabaseName()
    {
        return databaseName;
    }

    @Override
    public Class<? extends TableDefinition> getTableDefinitionClass()
    {
        return ReadOnlyTableDefinition.class;
    }

    public static final class ReadOnlyTableDefinition
            extends TableDefinition
    {
        public ReadOnlyTableDefinition(TableHandle handle)
        {
            super(handle);
        }
    }
}
