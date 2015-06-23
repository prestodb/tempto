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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class JdbcTableDefinition
        extends TableDefinition
{
    private static final String NAME_MARKER = "%NAME%";

    private final JdbcTableDataSource dataSource;
    private final String createTableDDLTemplate;

    private JdbcTableDefinition(String name, String createTableDDLTemplate, JdbcTableDataSource dataSource)
    {
        super(name);
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

    public static JdbcTableDefinition jdbcTableDefinition(String name, String createTableDDLTemplate, JdbcTableDataSource dataSource)
    {
        return new JdbcTableDefinition(name, createTableDDLTemplate, dataSource);
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
}
