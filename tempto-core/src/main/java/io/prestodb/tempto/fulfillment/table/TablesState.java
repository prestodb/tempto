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

import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.context.State;
import io.prestodb.tempto.internal.fulfillment.table.TableName;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.getOnlyElement;
import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public abstract class TablesState
        implements State
{
    private final List<TableInstance> tables;
    private final String tableDescription;

    public TablesState(List<TableInstance> tables, String tableDescription)
    {
        this.tables = ImmutableList.copyOf(requireNonNull(tables, "tables is null"));
        this.tableDescription = requireNonNull(tableDescription, "tableDescription is null");
    }

    public TableInstance get(TableDefinition tableDefinition)
    {
        return get(tableDefinition.getTableHandle());
    }

    public TableInstance get(String name)
    {
        return get(tableHandle(name));
    }

    public TableInstance get(TableHandle tableHandle)
    {
        List<TableInstance> tableInstances = tables.stream()
                .filter(it -> matches(it, tableHandle))
                .collect(toList());

        switch (tableInstances.size()) {
            case 0:
                throw new IllegalArgumentException(format("No %s instance found for name %s", tableDescription, tableHandle));
            case 1:
                return getOnlyElement(tableInstances);
            default:
                throw new IllegalArgumentException(format(
                        "Multiple %s instances found for %s, please use more detailed table handle. Found %s",
                        tableDescription,
                        tableHandle,
                        tableInstances.stream().map(TableInstance::getTableName).collect(toList())));
        }
    }

    public List<TableName> getTableNames(String databaseName)
    {
        return tables.stream()
                .filter(it -> it.getDatabase().equalsIgnoreCase(databaseName))
                .map(TableInstance::getTableName)
                .collect(toList());
    }

    public Map<String, String> getNameInDatabaseMap(String databaseName)
    {
        return getTableNames(databaseName).stream()
                .collect(toMap(TableName::getName, TableName::getNameInDatabase));
    }

    private boolean matches(TableInstance table, TableHandle handle)
    {
        if (!table.getName().equalsIgnoreCase(handle.getName())) {
            return false;
        }
        Optional<String> tableSchema = table.getSchema();
        if (handle.getSchema().isPresent()) {
            if (!tableSchema.isPresent()) {
                return false;
            }
            if (!tableSchema.get().equalsIgnoreCase(handle.getSchema().get())) {
                return false;
            }
        }
        if (!tableSchema.isPresent() && handle.getSchema().isPresent()) {
            return false;
        }

        if (handle.noSchema() && tableSchema.isPresent()) {
            return false;
        }

        return !handle.getDatabase().isPresent() || handle.getDatabase().get().equalsIgnoreCase(table.getDatabase());
    }

    public Set<String> getDatabaseNames()
    {
        return tables.stream()
                .map(it -> it.getDatabase())
                .collect(toSet());
    }
}

