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

package io.prestodb.tempto.internal.fulfillment.table;

import com.google.inject.Inject;
import io.prestodb.tempto.fulfillment.TestStatus;
import io.prestodb.tempto.fulfillment.table.MutableTableRequirement;
import io.prestodb.tempto.fulfillment.table.MutableTablesState;
import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.fulfillment.table.TableManagerDispatcher;
import io.prestodb.tempto.fulfillment.table.TablesState;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static io.prestodb.tempto.fulfillment.TestStatus.FAILURE;

public class MutableTablesFulfiller
        extends TableRequirementFulfiller<MutableTableRequirement>
{
    private MutableTablesState mutableTablesState;

    @Inject
    public MutableTablesFulfiller(TableManagerDispatcher tableManagerDispatcher)
    {
        super(tableManagerDispatcher, MutableTableRequirement.class);
    }

    @Override
    protected TablesState createState(List<TableInstance> tables)
    {
        return mutableTablesState = new MutableTablesState(tables);
    }

    @Override
    protected TableInstance createTable(TableManager tableManager, MutableTableRequirement tableRequirement)
    {
        return tableManager.createMutable(tableRequirement.getTableDefinition(), tableRequirement.getState(), tableRequirement.getTableHandle());
    }

    @Override
    public final void cleanup(TestStatus testStatus)
    {
        checkState(mutableTablesState != null, "No mutable tables, call fullfil method first");
        if (testStatus == FAILURE) {
            return;
        }
        for (TableManager tableManager : tableManagerDispatcher.getAllTableManagers()) {
            mutableTablesState.getTableNames(tableManager.getDatabaseName()).stream()
                    .forEach(tableManager::dropTable);
        }
    }
}
