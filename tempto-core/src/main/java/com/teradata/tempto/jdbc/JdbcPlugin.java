package com.teradata.tempto.jdbc;

import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableHandle;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource;
import com.teradata.tempto.internal.convention.tabledefinitions.FileBasedJdbcDataSource;
import com.teradata.tempto.spi.Plugin;
import com.teradata.tempto.spi.convention.tabledefinitions.ConventionTableDefinitionDescriptor;

import java.util.Optional;

import static com.teradata.tempto.fulfillment.table.TableHandle.tableHandle;
import static com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition.jdbcTableDefinition;

public class JdbcPlugin
        implements Plugin
{
    @Override
    public TableDefinition getConventionTableDefinition(ConventionTableDefinitionDescriptor descriptor)
    {
        JdbcTableDataSource dataSource = new FileBasedJdbcDataSource(descriptor);
        return jdbcTableDefinition(
                getTableHandle(descriptor),
                descriptor.getParsedDDLFile().getContent(),
                dataSource);
    }

    @Override
    public String getName()
    {
        return "jdbc";
    }

    private TableHandle getTableHandle(ConventionTableDefinitionDescriptor tableDefinitionDescriptor)
    {
        TableHandle tableHandle = tableHandle(tableDefinitionDescriptor.getName());
        Optional<String> schema = tableDefinitionDescriptor.getParsedDDLFile().getSchema();
        if (schema.isPresent()) {
            tableHandle = tableHandle.inSchema(schema.get());
        }
        return tableHandle;
    }
}
