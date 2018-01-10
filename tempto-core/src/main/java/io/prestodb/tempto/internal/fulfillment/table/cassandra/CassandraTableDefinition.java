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
package io.prestodb.tempto.internal.fulfillment.table.cassandra;

import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalDataSource;
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalTableDefinition;

import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle;
import static java.util.Objects.requireNonNull;

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

    public static CassandraTableDefinitionBuilder cassandraBuilder(String name)
    {
        return new CassandraTableDefinitionBuilder(name);
    }

    public static class CassandraTableDefinitionBuilder
    {
        private TableHandle handle;
        private String createTableDDLTemplate;
        private RelationalDataSource dataSource;

        private CassandraTableDefinitionBuilder(String name)
        {
            this.handle = tableHandle(name);
        }

        public CassandraTableDefinitionBuilder withSchema(String schema)
        {
            this.handle = handle.inSchema(schema);
            return this;
        }

        public CassandraTableDefinitionBuilder withDatabase(String database)
        {
            this.handle = handle.inDatabase(database);
            return this;
        }

        public CassandraTableDefinitionBuilder setCreateTableDDLTemplate(String createTableDDLTemplate)
        {
            this.createTableDDLTemplate = createTableDDLTemplate;
            return this;
        }

        public CassandraTableDefinitionBuilder setDataSource(RelationalDataSource dataSource)
        {
            this.dataSource = requireNonNull(dataSource, "dataSource is null");
            return this;
        }

        public CassandraTableDefinition build()
        {
            return cassandraTableDefinition(handle, createTableDDLTemplate, dataSource);
        }
    }
}
