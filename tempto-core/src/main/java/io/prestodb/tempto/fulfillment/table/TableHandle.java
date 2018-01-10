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
package io.prestodb.tempto.fulfillment.table;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class TableHandle
{
    public static TableHandle tableHandle(String name)
    {
        return new TableHandle(Optional.empty(), Optional.empty(), name);
    }

    public static TableHandle parse(String value)
    {
        if (value.contains(".")) {
            List<String> parts = Splitter.on('.').splitToList(value);
            checkArgument(parts.size() <= 3, "Invalid table name syntax. Expected at most two occurrences of '.' in '%s'.", value);
            if (parts.size() == 2) {
                return tableHandle(parts.get(1)).inSchema(parts.get(0));
            }
            return tableHandle(parts.get(2)).inDatabase(parts.get(0)).inSchema(parts.get(1));
        }
        else {
            return tableHandle(value);
        }
    }

    private final Optional<String> database;

    private final Optional<String> schema;
    private final String name;
    private final boolean requireNoSchema;

    private TableHandle(Optional<String> database, Optional<String> schema, String name)
    {
        this(database, schema, name, false);
    }

    private TableHandle(Optional<String> database, Optional<String> schema, String name, boolean requireNoSchema)
    {
        checkArgument(!(requireNoSchema && schema.isPresent()), "Schema given while required no schema.");
        this.database = requireNonNull(database, "database is null");
        this.schema = requireNonNull(schema, "schema is null");
        this.name = requireNonNull(name, "name is null");
        this.requireNoSchema = requireNoSchema;
    }

    public TableHandle withName(String name)
    {
        return new TableHandle(database, schema, name);
    }

    public TableHandle inDatabase(String database)
    {
        return new TableHandle(Optional.of(database), schema, name);
    }

    public TableHandle inSchema(String schema)
    {
        return new TableHandle(database, Optional.of(schema), name);
    }

    public TableHandle withNoSchema()
    {
        return new TableHandle(database, Optional.empty(), name, true);
    }

    public String getName()
    {
        return name;
    }

    public Optional<String> getDatabase()
    {
        return database;
    }

    public Optional<String> getSchema()
    {
        return schema;
    }

    public boolean noSchema()
    {
        return requireNoSchema;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableHandle that = (TableHandle) o;
        return Objects.equals(requireNoSchema, that.requireNoSchema) &&
                Objects.equals(database, that.database) &&
                Objects.equals(schema, that.schema) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(database, schema, name, requireNoSchema);
    }

    @Override
    public String toString()
    {
        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this);
        if (database.isPresent()) {
            toStringHelper.add("database", database.get());
        }
        if (schema.isPresent()) {
            toStringHelper.add("schema", schema.get());
        }
        toStringHelper.add("name", name);
        if (requireNoSchema) {
            toStringHelper.addValue("requireNoSchema");
        }
        return toStringHelper.toString();
    }
}
