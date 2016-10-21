package com.teradata.tempto.spi;

import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.spi.convention.tabledefinitions.ConventionTableDefinitionDescriptor;

public interface Plugin
{
    TableDefinition getConventionTableDefinition(ConventionTableDefinitionDescriptor descriptor);

    /**
     * Name of plugin, in all lower case
     */
    String getName();
}
