/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.hive;

import com.google.inject.Inject;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableInstance;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.query.QueryExecutor;
import org.slf4j.Logger;

import javax.inject.Named;

import static java.text.MessageFormat.format;
import static org.slf4j.LoggerFactory.getLogger;

public class HiveTableManager
        implements TableManager
{
    private static final Logger LOGGER = getLogger(HiveTableManager.class);

    private final QueryExecutor queryExecutor;
    private final HiveDataSourceWriter hiveDataSourceWriter;

    @Inject
    public HiveTableManager(@Named("hive") QueryExecutor queryExecutor, HiveDataSourceWriter hiveDataSourceWriter)
    {
        this.queryExecutor = queryExecutor;
        this.hiveDataSourceWriter = hiveDataSourceWriter;
    }

    @Override
    public TableInstance createImmutable(TableDefinition tableDefinition)
    {
        HiveTableDefinition hiveTableDefinition = (HiveTableDefinition) tableDefinition;
        LOGGER.debug("creating table {}", tableDefinition.getName());
        queryExecutor.executeQuery(dropTableDDL(tableDefinition.getName()));
        hiveDataSourceWriter.ensureDataOnHdfs(hiveTableDefinition.getDataSource());
        queryExecutor.executeQuery(createTableDDL(hiveTableDefinition));
        return new TableInstance(tableDefinition.getName(), tableDefinition.getName(), tableDefinition);
    }

    @Override
    public TableInstance createMutable(TableDefinition tableDefinition)
    {
        throw new UnsupportedOperationException("not supported yet");
    }

    @Override
    public void drop(TableInstance tableInstance)
    {
        queryExecutor.executeQuery(dropTableDDL(tableInstance.getNameInDatabase()));
    }

    private String createTableDDL(HiveTableDefinition tableDefinition)
    {
        String escapedFormat = tableDefinition.getCreateTableDDLTemplate(tableDefinition.getDataSource().getPath());
        return format(escapedFormat, tableDefinition.getDataSource().getPath());
    }

    private String dropTableDDL(String tableName)
    {
        return format("DROP TABLE IF EXISTS {0}", tableName);
    }
}
