/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tempto.fulfillment.table.jdbc;

import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableHandle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.teradata.tempto.fulfillment.table.TableHandle.tableHandle;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class JdbcTableDefinition
        extends TableDefinition
{
    public static JdbcTableDefinition jdbcTableDefinition(String name, String createTableDDLTemplate, JdbcTableDataSource dataSource)
    {
        return jdbcTableDefinition(tableHandle(name), createTableDDLTemplate, dataSource);
    }

    public static JdbcTableDefinition jdbcTableDefinition(TableHandle tableHandle, String createTableDDLTemplate, JdbcTableDataSource dataSource)
    {
        return new JdbcTableDefinition(tableHandle, createTableDDLTemplate, dataSource);
    }

    private static final String NAME_MARKER = "%NAME%";
    private final JdbcTableDataSource dataSource;

    private final String createTableDDLTemplate;

    private JdbcTableDefinition(TableHandle handle, String createTableDDLTemplate, JdbcTableDataSource dataSource)
    {
        super(handle);
        ;
        this.dataSource = checkNotNull(dataSource, "dataSource is null");
        this.createTableDDLTemplate = checkNotNull(createTableDDLTemplate, "createTableDDLTemplate is null");
        checkArgument(createTableDDLTemplate.contains(NAME_MARKER), "Create table DDL must contain %NAME% placeholder");
    }

    public JdbcTableDataSource getDataSource()
    {
        return dataSource;
    }

    public String getCreateTableDDL(String name)
    {
        return createTableDDLTemplate.replace(NAME_MARKER, name);
    }

    @Override
    public boolean equals(Object o)
    {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }

    public static JdbcTableDefinitionBuilder builder(String name)
    {
        return new JdbcTableDefinitionBuilder(name);
    }

    public static JdbcTableDefinitionBuilder like(JdbcTableDefinition tableDefinition)
    {
        JdbcTableDefinitionBuilder builder = new JdbcTableDefinitionBuilder(tableDefinition.getName())
                .setCreateTableDDLTemplate(tableDefinition.createTableDDLTemplate)
                .setDataSource(tableDefinition.getDataSource());
        if (tableDefinition.getSchema().isPresent()) {
            builder.withSchema(tableDefinition.getSchema().get());
        }
        return builder;
    }

    public static class JdbcTableDefinitionBuilder
    {
        private TableHandle handle;
        private String createTableDDLTemplate;
        private JdbcTableDataSource dataSource;

        private JdbcTableDefinitionBuilder(String name)
        {
            this.handle = tableHandle(name);
        }

        public JdbcTableDefinitionBuilder withSchema(String schema)
        {
            this.handle = handle.inSchema(schema);
            return this;
        }

        public JdbcTableDefinitionBuilder withDatabase(String database)
        {
            this.handle = handle.inDatabase(database);
            return this;
        }

        public JdbcTableDefinitionBuilder setCreateTableDDLTemplate(String createTableDDLTemplate)
        {
            this.createTableDDLTemplate = createTableDDLTemplate;
            return this;
        }

        public JdbcTableDefinitionBuilder setDataSource(JdbcTableDataSource dataSource)
        {
            this.dataSource = dataSource;
            return this;
        }

        public JdbcTableDefinition build()
        {
            return jdbcTableDefinition(handle, createTableDDLTemplate, dataSource);
        }
    }
}
