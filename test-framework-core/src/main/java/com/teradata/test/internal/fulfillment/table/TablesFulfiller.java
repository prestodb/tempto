/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.table;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableInstance;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManagerDispatcher;
import com.teradata.test.fulfillment.table.TablesState;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.beust.jcommander.internal.Sets.newHashSet;
import static com.google.common.collect.Iterables.filter;
import static org.slf4j.LoggerFactory.getLogger;

public class TablesFulfiller
        implements RequirementFulfiller
{

    private static final Logger LOGGER = getLogger(TablesFulfiller.class);

    private TableManagerDispatcher tableManagerDispatcher;
    private final Set<TableInstance> cratedTables = newHashSet();

    @Inject
    public TablesFulfiller(TableManagerDispatcher tableManagerDispatcher)
    {
        this.tableManagerDispatcher = tableManagerDispatcher;
    }

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {
        LOGGER.debug("fulfilling tables");
        Iterable<ImmutableHiveTableRequirement> tableRequirements = filter(requirements, ImmutableHiveTableRequirement.class);
        Map<String, TableInstance> instanceMap = newHashMap();

        for (ImmutableHiveTableRequirement tableRequirement : tableRequirements) {
            TableDefinition tableDefinition = tableRequirement.getTableDefinition();
            TableManager tableManager = tableManagerDispatcher.getTableManagerFor(tableDefinition);
            TableInstance instance = tableManager.createImmutable(tableRequirement.getTableDefinition());
            cratedTables.add(instance);
            instanceMap.put(instance.getName(), instance);
        }
        return ImmutableSet.of(new TablesState(instanceMap));
    }

    @Override
    public void cleanup()
    {
        LOGGER.debug("cleaning up tables");
        cratedTables.forEach(tableInstance -> tableManagerDispatcher.getTableManagerFor(tableInstance).drop(tableInstance));
    }
}
