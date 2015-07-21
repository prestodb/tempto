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
import com.teradata.tempto.context.State;
import com.teradata.tempto.internal.fulfillment.table.DatabaseTableInstanceMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public abstract class TablesState
        implements State
{
    private final Map<String, DatabaseTableInstanceMap> databaseTableInstances;
    private final String tableDescription;

    public TablesState(Map<String, DatabaseTableInstanceMap> databaseTableInstances, String tableDescription)
    {
        this.databaseTableInstances = requireNonNull(databaseTableInstances, "databaseTableInstances is null");
        this.tableDescription = requireNonNull(tableDescription, "tableDescription is null");
    }

    public TableInstance get(String name)
    {
        return get(name, Optional.empty());
    }

    public TableInstance get(String name, Optional<String> database)
    {
        if (database.isPresent()) {
            DatabaseTableInstanceMap databaseTableInstanceMap = getDatabaseTableInstanceMap(database.get());
            return databaseTableInstanceMap.get(name).orElseThrow(() ->
                    new IllegalArgumentException(format("No '%s' instance found for name '%s' in database '%s'.", tableDescription, name, database.get())));
        }
        List<TableInstance> tableInstances = databaseTableInstances.values().stream()
                .filter(it -> it.contains(name))
                .map(it -> it.get(name).get())
                .collect(toList());

        switch (tableInstances.size()) {
            case 0:
                throw new IllegalArgumentException(format("No '%s' instance found for name '%s'.", tableDescription, name));
            case 1:
                return getOnlyElement(tableInstances);
            default:
                throw new IllegalArgumentException(format("%s instance for name '%s' was found in multiple databases. Please specify database.", tableDescription, name));
        }
    }

    public Map<String, String> getNameInDatabaseMap(String databaseName)
    {
        if (!databaseTableInstances.containsKey(databaseName)) {
            return ImmutableMap.of();
        }
        return getDatabaseTableInstanceMap(databaseName).getTableInstances().entrySet()
                .stream()
                .collect(toMap(Entry::getKey, e -> e.getValue().getNameInDatabase()));
    }

    public DatabaseTableInstanceMap getDatabaseTableInstanceMap(String database)
    {
        checkState(databaseTableInstances.containsKey(database), "There is no tables for database '%s'.", database);
        return databaseTableInstances.get(database);
    }
}

