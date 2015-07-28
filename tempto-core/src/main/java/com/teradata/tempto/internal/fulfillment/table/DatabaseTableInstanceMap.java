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

package com.teradata.tempto.internal.fulfillment.table;

import com.teradata.tempto.fulfillment.table.TableInstance;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.common.base.Preconditions.checkState;

public class DatabaseTableInstanceMap
{
    private final String database;
    private final Map<String, TableInstance> tableInstances = newHashMap();

    public DatabaseTableInstanceMap(String database)
    {
        this.database = database;
    }

    public void put(TableInstance instance)
    {
        checkState(!tableInstances.containsKey(instance.getName()),
                "Table with name '%s' already exist in '%s' database.", instance.getName(), database);
        tableInstances.put(instance.getName(), instance);
    }

    public String getDatabase()
    {
        return database;
    }

    public Map<String, TableInstance> getTableInstances()
    {
        return tableInstances;
    }

    public boolean contains(String name)
    {
        return tableInstances.containsKey(name);
    }

    public Optional<TableInstance> get(String name)
    {
        return Optional.ofNullable(tableInstances.get(name));
    }

    public Set<String> getTableNames()
    {
        return tableInstances.keySet();
    }
}
