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

import io.prestodb.tempto.internal.fulfillment.table.TableName;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Describes a table that is instantiated during a test run.
 */
public class TableInstance<T extends TableDefinition>
{
    private final TableName name;
    private final T tableDefinition;

    protected TableInstance(TableName name, T tableDefinition)
    {
        this.name = requireNonNull(name, "name is null");
        this.tableDefinition = requireNonNull(tableDefinition, "tableDefinition is null");
    }

    public String getName()
    {
        return name.getName();
    }

    public String getDatabase()
    {
        return name.getDatabase();
    }

    public Optional<String> getSchema()
    {
        return name.getSchema();
    }

    public String getNameInDatabase()
    {
        return name.getNameInDatabase();
    }

    public T tableDefinition()
    {
        return tableDefinition;
    }

    public TableName getTableName()
    {
        return name;
    }
}
