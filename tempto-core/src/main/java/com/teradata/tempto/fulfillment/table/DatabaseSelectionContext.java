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

import com.google.common.base.MoreObjects;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class DatabaseSelectionContext
{
    public static DatabaseSelectionContext forDatabaseName(String databaseName)
    {
        return new DatabaseSelectionContext(Optional.of(databaseName), Optional.empty());
    }

    public static DatabaseSelectionContext none()
    {
        return new DatabaseSelectionContext(Optional.empty(), Optional.empty());
    }

    private final Optional<String> databaseName;
    // in case no databaseName given and multiple table manager definitions for same table definition, this will be used as databaseName
    private final Optional<String> queryExecutorDatabaseName;

    public DatabaseSelectionContext(Optional<String> databaseName, Optional<String> queryExecutorDatabaseName)
    {
        this.databaseName = requireNonNull(databaseName, "databaseName is null");
        this.queryExecutorDatabaseName = requireNonNull(queryExecutorDatabaseName, "queryExecutorDatabaseName is null");
    }

    public Optional<String> getQueryExecutorDatabaseName()
    {
        return queryExecutorDatabaseName;
    }

    public Optional<String> getDatabaseName()
    {
        return databaseName;
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

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("databaseName", databaseName)
                .add("queryExecutorDatabaseName", queryExecutorDatabaseName)
                .toString();
    }
}
