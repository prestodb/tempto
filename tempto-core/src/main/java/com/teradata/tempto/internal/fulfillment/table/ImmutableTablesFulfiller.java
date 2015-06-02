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
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.teradata.tempto.Requirement;
import com.teradata.tempto.context.State;
import com.teradata.tempto.fulfillment.RequirementFulfiller;
import com.teradata.tempto.fulfillment.table.ImmutableTableRequirement;
import com.teradata.tempto.fulfillment.table.ImmutableTablesState;
import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableInstance;
import com.teradata.tempto.fulfillment.table.TableManager;
import com.teradata.tempto.fulfillment.table.TableManagerDispatcher;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static org.slf4j.LoggerFactory.getLogger;

public class ImmutableTablesFulfiller
        implements RequirementFulfiller
{

    private static final Logger LOGGER = getLogger(ImmutableTablesFulfiller.class);

    private TableManagerDispatcher tableManagerDispatcher;

    private final Map<String, TableInstance> tableInstances = newHashMap();

    @Inject
    public ImmutableTablesFulfiller(TableManagerDispatcher tableManagerDispatcher)
    {
        this.tableManagerDispatcher = tableManagerDispatcher;
    }

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {
        LOGGER.debug("fulfilling tables");

        filter(requirements, ImmutableTableRequirement.class)
                .stream()
                .map(ImmutableTableRequirement::getTableDefinition)
                .forEach(this::createImmutableTable);

        return ImmutableSet.of(new ImmutableTablesState(tableInstances));
    }

    @Override
    public void cleanup()
    {
        // TableManagers are responsible for cleanUp
    }

    private void createImmutableTable(TableDefinition tableDefinition)
    {
        TableManager tableManager = tableManagerDispatcher.getTableManagerFor(tableDefinition);
        TableInstance instance = tableManager.createImmutable(tableDefinition);
        checkState(!tableInstances.containsKey(instance.getName()), "Table with name '%s' already exist.", instance.getName());
        tableInstances.put(instance.getName(), instance);
    }

    private <T> List<T> filter(Collection collection, Class<T> clazz)
    {
        return newArrayList(Iterables.filter(collection, clazz));
    }
}
