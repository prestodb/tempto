/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.table;

import com.google.common.collect.MapMaker;
import com.teradata.test.internal.convention.tabledefinitions.ConventionTableDefinitionsProvider;
import com.teradata.test.internal.initialization.ModulesHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.teradata.test.internal.initialization.ModulesHelper.getFieldsAnnotatedWith;
import static java.util.stream.Collectors.toList;

/**
 * Stores {@link com.teradata.test.fulfillment.table.TableDefinition} mapped by names.
 */
public class TableDefinitionsRepository
{
    /**
     * An annotation for {@link TableDefinition} static fields
     * that should be registered in {@link TableDefinitionsRepository}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RepositoryTableDefinition
    {
    }

    private static List<TableDefinition> SCANNED_TABLE_DEFINITIONS =
            getFieldsAnnotatedWith(RepositoryTableDefinition.class)
                    .stream()
                    .map(ModulesHelper::<TableDefinition>getStaticFieldValue)
                    .collect(toList());

    private static TableDefinitionsRepository TABLE_DEFINITIONS_REPOSITORY = new TableDefinitionsRepository(SCANNED_TABLE_DEFINITIONS);

    static {
        // TODO: since TestNG has no listener that can be run before tests factory, this has to be initialized here
        new ConventionTableDefinitionsProvider().registerConventionTableDefinitions(TABLE_DEFINITIONS_REPOSITORY);
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
