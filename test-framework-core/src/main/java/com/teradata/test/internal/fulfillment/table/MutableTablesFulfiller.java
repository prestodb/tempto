/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.table;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.fulfillment.table.MutableTableRequirement;
import com.teradata.test.fulfillment.table.MutableTablesState;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableInstance;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManagerDispatcher;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static org.slf4j.LoggerFactory.getLogger;

public class MutableTablesFulfiller
        implements RequirementFulfiller
{

    private static final Logger LOGGER = getLogger(MutableTablesFulfiller.class);

    private TableManagerDispatcher tableManagerDispatcher;

    private final Map<String, TableInstance> tableInstances = newHashMap();

    @Inject
    public MutableTablesFulfiller(TableManagerDispatcher tableManagerDispatcher)
    {
        this.tableManagerDispatcher = tableManagerDispatcher;
    }

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {
        LOGGER.debug("fulfilling tables");

        filter(requirements, MutableTableRequirement.class)
                .stream()
                .forEach(this::createMutableTable);

        return ImmutableSet.of(new MutableTablesState(tableInstances));
    }

    @Override
    public void cleanup()
    {
        LOGGER.debug("cleaning up tables");
        cleanup(tableInstances.values());
    }

    private void createMutableTable(MutableTableRequirement mutableTableRequirement)
    {
        TableDefinition tableDefinition = mutableTableRequirement.getTableDefinition();
        String name = mutableTableRequirement.getName();
        checkState(!tableInstances.containsKey(name));

        TableManager tableManager = tableManagerDispatcher.getTableManagerFor(tableDefinition);
        TableInstance instance = tableManager.createMutable(tableDefinition, mutableTableRequirement.getState());
        tableInstances.put(name, instance);
    }

    private void cleanup(Collection<TableInstance> tableInstances)
    {
        tableInstances.forEach(tableInstance -> tableManagerDispatcher.getTableManagerFor(tableInstance).drop(tableInstance));
    }

    private <T> List<T> filter(Collection collection, Class<T> clazz)
    {
        return newArrayList(Iterables.filter(collection, clazz));
    }
}
