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

package io.prestodb.tempto.fulfillment.table.jdbc;

import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableHandle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class RelationalTableDefinition
        extends TableDefinition
{
    public static RelationalTableDefinition relationalTableDefinition(String name, String createTableDDLTemplate, RelationalDataSource dataSource)
    {
        return relationalTableDefinition(tableHandle(name), createTableDDLTemplate, dataSource);
    }

    public static RelationalTableDefinition relationalTableDefinition(TableHandle tableHandle, String createTableDDLTemplate, RelationalDataSource dataSource)
    {
        return new RelationalTableDefinition(tableHandle, createTableDDLTemplate, dataSource);
    }

    private static final String NAME_MARKER = "%NAME%";
    private final RelationalDataSource dataSource;

    private final String createTableDDLTemplate;

    protected RelationalTableDefinition(TableHandle handle, String createTableDDLTemplate, RelationalDataSource dataSource)
    {
        super(handle);
        ;
        this.dataSource = checkNotNull(dataSource, "dataSource is null");
        this.createTableDDLTemplate = checkNotNull(createTableDDLTemplate, "createTableDDLTemplate is null");
        checkArgument(createTableDDLTemplate.contains(NAME_MARKER), "Create table DDL must contain %NAME% placeholder");
    }

    public RelationalDataSource getDataSource()
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

    public static RelationalTableDefinitionBuilder builder(String name)
    {
        return new RelationalTableDefinitionBuilder(name);
    }

    public static RelationalTableDefinitionBuilder like(RelationalTableDefinition tableDefinition)
    {
        RelationalTableDefinitionBuilder builder = new RelationalTableDefinitionBuilder(tableDefinition.getName())
                .setCreateTableDDLTemplate(tableDefinition.createTableDDLTemplate)
                .setDataSource(tableDefinition.getDataSource());
        if (tableDefinition.getSchema().isPresent()) {
            builder.withSchema(tableDefinition.getSchema().get());
        }
        return builder;
    }

    public static class RelationalTableDefinitionBuilder
    {
        private TableHandle handle;
        private String createTableDDLTemplate;
        private RelationalDataSource dataSource;

        protected RelationalTableDefinitionBuilder(String name)
        {
            this.handle = tableHandle(name);
        }

        public RelationalTableDefinitionBuilder withSchema(String schema)
        {
            this.handle = handle.inSchema(schema);
            return this;
        }

        public RelationalTableDefinitionBuilder withDatabase(String database)
        {
            this.handle = handle.inDatabase(database);
            return this;
        }

        public RelationalTableDefinitionBuilder setCreateTableDDLTemplate(String createTableDDLTemplate)
        {
            this.createTableDDLTemplate = createTableDDLTemplate;
            return this;
        }

        public RelationalTableDefinitionBuilder setDataSource(RelationalDataSource dataSource)
        {
            this.dataSource = dataSource;
            return this;
        }

        public RelationalTableDefinition build()
        {
            return relationalTableDefinition(handle, createTableDDLTemplate, dataSource);
        }
    }
}
