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
import io.prestodb.tempto.fulfillment.table.ImmutableTableRequirement;
import io.prestodb.tempto.fulfillment.table.ImmutableTablesState;
import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.fulfillment.table.TableManagerDispatcher;
import io.prestodb.tempto.fulfillment.table.TablesState;

import java.util.List;

public class ImmutableTablesFulfiller
        extends TableRequirementFulfiller<ImmutableTableRequirement>
{
    @Inject
    public ImmutableTablesFulfiller(TableManagerDispatcher tableManagerDispatcher)
    {
        super(tableManagerDispatcher, ImmutableTableRequirement.class);
    }

    @Override
    protected TablesState createState(List<TableInstance> tables)
    {
        return new ImmutableTablesState(tables);
    }

    @Override
    protected TableInstance createTable(TableManager tableManager, ImmutableTableRequirement tableRequirement)
    {
        return tableManager.createImmutable(tableRequirement.getTableDefinition());
    }

    @Override
    public void cleanup(TestStatus status)
    {
    }
}
