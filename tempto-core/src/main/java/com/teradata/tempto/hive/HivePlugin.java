package com.teradata.tempto.hive;

import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableHandle;
import com.teradata.tempto.fulfillment.table.hive.HiveDataSource;
import com.teradata.tempto.internal.convention.tabledefinitions.FileBasedHiveDataSource;
import com.teradata.tempto.spi.Plugin;
import com.teradata.tempto.spi.convention.tabledefinitions.ConventionTableDefinitionDescriptor;

import java.util.Optional;

import static com.teradata.tempto.fulfillment.table.TableHandle.tableHandle;
import static com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition.hiveTableDefinition;

public class HivePlugin
    implements Plugin
{
    @Override
    public TableDefinition getConventionTableDefinition(ConventionTableDefinitionDescriptor descriptor)
    {
        HiveDataSource dataSource = new FileBasedHiveDataSource(descriptor);
        return hiveTableDefinition(
                getTableHandle(descriptor),
                descriptor.getParsedDDLFile().getContent(),
                dataSource);
    }

    @Override
    public String getName()
    {
        return "hive";
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
