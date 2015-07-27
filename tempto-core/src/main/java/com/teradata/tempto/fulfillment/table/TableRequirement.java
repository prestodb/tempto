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

package com.teradata.tempto.fulfillment.table;

import com.teradata.tempto.Requirement;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public abstract class TableRequirement<T extends TableRequirement>
        implements Requirement
{

    private final TableDefinition tableDefinition;
    private final Optional<String> databaseName;

    protected TableRequirement(TableDefinition tableDefinition, Optional<String> databaseName)
    {
        this.tableDefinition = checkNotNull(tableDefinition);
        this.databaseName = checkNotNull(databaseName);
    }

    public TableDefinition getTableDefinition()
    {
        return tableDefinition;
    }

    public Optional<String> getDatabaseName()
    {
        return databaseName;
    }

    public abstract T copyWithDatabase(String databaseName);

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
