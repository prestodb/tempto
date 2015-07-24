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

import com.teradata.tempto.fulfillment.NamedObjectsState;

import java.util.Map;
import java.util.Map.Entry;

import static com.teradata.tempto.context.ThreadLocalTestContextHolder.testContext;
import static java.util.stream.Collectors.toMap;

public class MutableTablesState
        extends NamedObjectsState<TableInstance>
{

    public static MutableTablesState mutableTablesState()
    {
        return testContext().getDependency(MutableTablesState.class);
    }

    public MutableTablesState(Map<String, TableInstance> mutableTableInstances)
    {
        super(mutableTableInstances, "mutable table");
    }

    public Map<String, String> getNameInDatabaseMap()
    {
        return objects.entrySet()
                .stream()
                .collect(toMap(Entry::getKey, e -> e.getValue().getNameInDatabase()));
    }
}
