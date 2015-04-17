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
import com.teradata.test.fulfillment.table.ImmutableTableRequirement;
import com.teradata.test.fulfillment.table.ImmutableTablesState;
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
        LOGGER.debug("cleaning up tables");
        tableInstances.values().forEach(tableInstance -> tableManagerDispatcher.getTableManagerFor(tableInstance).drop(tableInstance));
    }

    private void createImmutableTable(TableDefinition tableDefinition)
    {
        TableManager tableManager = tableManagerDispatcher.getTableManagerFor(tableDefinition);
        TableInstance instance = tableManager.createImmutable(tableDefinition);
        checkState(!tableInstances.containsKey(instance.getName()));
        tableInstances.put(instance.getName(), instance);
    }

    private <T> List<T> filter(Collection collection, Class<T> clazz)
    {
        return newArrayList(Iterables.filter(collection, clazz));
    }
}
