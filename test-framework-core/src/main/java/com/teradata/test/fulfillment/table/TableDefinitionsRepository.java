/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.table;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions;
import com.teradata.test.internal.convention.tabledefinitions.ConventionTableDefinitionsProvider;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Stores {@link com.teradata.test.fulfillment.table.TableDefinition} mapped by names.
 */
public class TableDefinitionsRepository
{
    private static final TableDefinitionsRepository TABLE_DEFINITIONS_REPOSITORY = new TableDefinitionsRepository(
            ImmutableList.<TableDefinition>builder()
                    .addAll(TpchTableDefinitions.TABLES)
                    .build());

    static {
        // TODO: since TestNG has no listener that can be run before tests factory, this has to be initialized here
        new ConventionTableDefinitionsProvider().registerConventionTableDefinitions(tableDefinitionsRepository());
    }

    public static <T extends TableDefinition> T registerTableDefinition(T tableDefinition)
    {
        return tableDefinitionsRepository().register(tableDefinition);
    }

    public static TableDefinition tableDefinitionForName(String name)
    {
        return tableDefinitionsRepository().getForName(name);
    }

    public static TableDefinitionsRepository tableDefinitionsRepository()
    {
        return TABLE_DEFINITIONS_REPOSITORY;
    }

    private final Map<String, TableDefinition> tableDefinitions = new MapMaker().makeMap();

    public TableDefinitionsRepository()
    {
    }

    public TableDefinitionsRepository(Collection<TableDefinition> tableDefinitions)
    {
        tableDefinitions.stream().forEach(this::register);
    }

    public <T extends TableDefinition> T register(T tableDefinition)
    {
        checkState(!tableDefinitions.containsKey(tableDefinition.getName()), "duplicated table definition: %s", tableDefinition.getName());
        tableDefinitions.put(tableDefinition.getName(), tableDefinition);
        return tableDefinition;
    }

    public TableDefinition getForName(String name)
    {
        checkState(tableDefinitions.containsKey(name), "no table definition for: %s", name);
        return tableDefinitions.get(name);
    }
}
