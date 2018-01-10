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

import com.google.common.collect.ImmutableSet;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.context.State;
import io.prestodb.tempto.fulfillment.RequirementFulfiller;
import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.fulfillment.table.TableManagerDispatcher;
import io.prestodb.tempto.fulfillment.table.TableRequirement;
import io.prestodb.tempto.fulfillment.table.TablesState;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class TableRequirementFulfiller<T extends TableRequirement>
        implements RequirementFulfiller
{
    private static final Logger LOGGER = getLogger(TableRequirementFulfiller.class);

    protected final TableManagerDispatcher tableManagerDispatcher;
    private final Class<T> requirementClass;

    public TableRequirementFulfiller(TableManagerDispatcher tableManagerDispatcher, Class<T> requirementClass)
    {
        this.tableManagerDispatcher = tableManagerDispatcher;
        this.requirementClass = requirementClass;
    }

    @Override
    public final Set<State> fulfill(Set<Requirement> requirements)
    {
        LOGGER.debug("fulfilling tables for: " + requirementClass);

        List<TableInstance> tables = requirements.stream()
                .filter(requirement -> requirement.getClass().isAssignableFrom(requirementClass))
                .map(requirement -> (T) requirement)
                .map(requirement -> requirement.copyWithDatabase(getDatabaseName(requirement)))
                .map(requirement -> (T) requirement)
                .distinct()
                .map(this::createTable)
                .collect(toList());

        return ImmutableSet.of(createState(tables));
    }

    private String getDatabaseName(T requirement)
    {
        return getTableManager(requirement).getDatabaseName();
    }

    protected abstract TablesState createState(List<TableInstance> tables);

    private TableInstance createTable(T tableRequirement)
    {
        TableManager tableManager = getTableManager(tableRequirement);
        tableManager.dropStaleMutableTables();
        return createTable(tableManager, tableRequirement);
    }

    private TableManager getTableManager(T tableRequirement)
    {
        return tableManagerDispatcher.getTableManagerFor(tableRequirement.getTableDefinition(), tableRequirement.getTableHandle());
    }

    protected abstract TableInstance createTable(TableManager tableManager, T tableRequirement);
}
