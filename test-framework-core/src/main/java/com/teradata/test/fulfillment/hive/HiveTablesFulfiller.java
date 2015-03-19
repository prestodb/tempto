/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.query.QueryExecutor;
import org.slf4j.Logger;

import javax.inject.Named;

import java.util.Map;
import java.util.Set;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

public class HiveTablesFulfiller
        implements RequirementFulfiller
{

    private static final Logger LOGGER = getLogger(HiveTablesFulfiller.class);

    private final QueryExecutor queryExecutor;
    private final Set<HiveTableInstance> cratedTables = newHashSet();

    @Inject
    public HiveTablesFulfiller(@Named("hive") QueryExecutor queryExecutor)
    {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {
        LOGGER.debug("fulfilling hive tables");
        Iterable<ImmutableHiveTableRequirement> tableRequirements = filter(requirements, ImmutableHiveTableRequirement.class);
        Map<String, HiveTableInstance> instanceMap = newHashMap();

        for (ImmutableHiveTableRequirement tableRequirement : tableRequirements) {
            HiveTableInstance instance = fulfillTable(tableRequirement);
            cratedTables.add(instance);
            instanceMap.put(instance.getName(), instance);
        }
        return ImmutableSet.of(new HiveTablesState(instanceMap));
    }

    private HiveTableInstance fulfillTable(ImmutableHiveTableRequirement tableRequirement)
    {
        LOGGER.debug("fulfilling table {}", tableRequirement.getTableName());
        queryExecutor.executeQuery(format("DROP IF EXISTS TABLE %s", tableRequirement.getTableName()));
        queryExecutor.executeQuery(format("CREATE TABLE %s LOCATION %s", tableRequirement.getTableName(), tableRequirement.getDataSource().getHdfsPath()));
        return new HiveTableInstance(tableRequirement.getTableName(), tableRequirement.getTableName());
    }

    @Override
    public void cleanup()
    {
        LOGGER.debug("cleaning up hive tables");
        for (HiveTableInstance createdTable : cratedTables) {
            cleanupTable(createdTable);
        }
    }

    private void cleanupTable(HiveTableInstance createdTable)
    {
        LOGGER.debug("cleaning up table {}", createdTable.getName());
        try {
            queryExecutor.executeQuery(format("DROP IF EXISTS TABLE %s", createdTable.getName()));
        }
        catch (Exception e) {
            LOGGER.debug("error while cleaning up table {}", createdTable.getName(), e);
        }
    }
}
