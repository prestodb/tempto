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
package com.teradata.tempto.internal.fulfillment.table.cassandra;

import com.teradata.tempto.fulfillment.table.TableHandle;
import com.teradata.tempto.fulfillment.table.jdbc.RelationalDataSource;
import com.teradata.tempto.fulfillment.table.jdbc.RelationalTableDefinition;

import static com.teradata.tempto.fulfillment.table.TableHandle.tableHandle;

public class CassandraTableDefinition
    extends RelationalTableDefinition
{
    public static CassandraTableDefinition cassandraTableDefinition(String name, String createTableDDLTemplate, RelationalDataSource dataSource)
    {
        return cassandraTableDefinition(tableHandle(name), createTableDDLTemplate, dataSource);
    }

    public static CassandraTableDefinition cassandraTableDefinition(TableHandle tableHandle, String createTableDDLTemplate, RelationalDataSource dataSource)
    {
        return new CassandraTableDefinition(tableHandle, createTableDDLTemplate, dataSource);
    }

    private CassandraTableDefinition(String name, String createTableDDLTemplate, RelationalDataSource dataSource)
    {
        super(tableHandle(name), createTableDDLTemplate, dataSource);
    }

    private CassandraTableDefinition(TableHandle handle, String createTableDDLTemplate, RelationalDataSource dataSource)
    {
        super(handle, createTableDDLTemplate, dataSource);
    }

    public static CassandraTableDefinitionBuilder builder(String name)
    {
        return new CassandraTableDefinitionBuilder(name);
    }

    public static class CassandraTableDefinitionBuilder
            extends RelationalTableDefinitionBuilder
    {
        private CassandraTableDefinitionBuilder(String name)
        {
            super(name);
        }

        @Override
        public CassandraTableDefinitionBuilder withSchema(String schema)
        {
            return (CassandraTableDefinitionBuilder) super.withSchema(schema);
        }

        @Override
        public CassandraTableDefinitionBuilder withDatabase(String database)
        {
            return (CassandraTableDefinitionBuilder) super.withDatabase(database);
        }

        @Override
        public CassandraTableDefinitionBuilder setCreateTableDDLTemplate(String createTableDDLTemplate)
        {
            return (CassandraTableDefinitionBuilder) super.setCreateTableDDLTemplate(createTableDDLTemplate);
        }

        @Override
        public CassandraTableDefinitionBuilder setDataSource(RelationalDataSource dataSource)
        {
            return (CassandraTableDefinitionBuilder) super.setDataSource(dataSource);
        }


        @Override
        public CassandraTableDefinition build()
        {
            return (CassandraTableDefinition) super.build();
        }
    }
}
