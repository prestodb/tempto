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

import com.google.common.collect.ImmutableSet;
import com.teradata.tempto.Requirement;
import com.teradata.tempto.context.State;
import com.teradata.tempto.fulfillment.RequirementFulfiller;
import com.teradata.tempto.fulfillment.table.TableInstance;
import com.teradata.tempto.fulfillment.table.TableManager;
import com.teradata.tempto.fulfillment.table.TableManagerDispatcher;
import com.teradata.tempto.fulfillment.table.TableRequirement;
import com.teradata.tempto.fulfillment.table.TablesState;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class TableRequirementFulfiller<T extends TableRequirement>
        implements RequirementFulfiller
{

    private static final Logger LOGGER = getLogger(TableRequirementFulfiller.class);

    private final TableManagerDispatcher tableManagerDispatcher;
    private final Class<T> requirementClass;

    private final Map<String, DatabaseTableInstanceMap> databaseTableInstances = newHashMap();

    public TableRequirementFulfiller(TableManagerDispatcher tableManagerDispatcher, Class<T> requirementClass)
    {
        this.tableManagerDispatcher = tableManagerDispatcher;
        this.requirementClass = requirementClass;
    }

    @Override
    public final Set<State> fulfill(Set<Requirement> requirements)
    {
        LOGGER.debug("fulfilling tables for: " + requirementClass);

        requirements.stream()
                .filter(requirement -> requirement.getClass().isAssignableFrom(requirementClass))
                .map(requirement -> (T) requirement)
                .map(requirement -> requirement.copyWithDatabase(getDatabaseName(requirement)))
                .map(requirement -> (T) requirement)
                .collect(toSet()).stream()
                .forEach(this::createTable);

        return ImmutableSet.of(createState(databaseTableInstances));
    }

    private String getDatabaseName(T requirement)
    {
        return getTableManager(requirement).getDatabaseName();
    }

    protected abstract TablesState createState(Map<String, DatabaseTableInstanceMap> databaseTableInstances);

    private void createTable(T tableRequirement)
    {
        TableManager tableManager = getTableManager(tableRequirement);
        TableInstance instance = createTable(tableManager, tableRequirement);
        addDatabaseTable(instance, tableManager.getDatabaseName());
    }

    private TableManager getTableManager(T tableRequirement)
    {
        return tableManagerDispatcher.getTableManagerFor(tableRequirement.getTableDefinition(), tableRequirement.getDatabaseSelectionContext());
    }

    private void addDatabaseTable(TableInstance instance, String databaseName)
    {
        if (!databaseTableInstances.containsKey(databaseName)) {
            databaseTableInstances.put(databaseName, new DatabaseTableInstanceMap(databaseName));
        }
        databaseTableInstances.get(databaseName).put(instance);
    }

    protected abstract TableInstance createTable(TableManager tableManager, T tableRequirement);

    @Override
    public final void cleanup()
    {
        // TableManagers are responsible for cleanUp
    }
}
