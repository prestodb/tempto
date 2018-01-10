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
package io.prestodb.tempto.internal.fulfillment.table;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class TableName
{
    private final String database;
    private final Optional<String> schema;
    private final String name;
    private final String nameInDatabase;

    public TableName(String database, Optional<String> schema, String name, String nameInDatabase)
    {
        this.database = requireNonNull(database, "database is null");
        this.schema = requireNonNull(schema, "schema is null");
        this.name = requireNonNull(name, "name is null");
        this.nameInDatabase = requireNonNull(nameInDatabase, "nameInDatabase is null");
    }

    public String getDatabase()
    {
        return database;
    }

    public String getName()
    {
        return name;
    }

    public String getNameInDatabase()
    {
        if (schema.isPresent()) {
            return schema.get() + "." + nameInDatabase;
        }
        return nameInDatabase;
    }

    public String getSchemalessNameInDatabase()
    {
        return nameInDatabase;
    }

    public Optional<String> getSchema()
    {
        return schema;
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
        TableName tableName = (TableName) o;
        return Objects.equals(database, tableName.database) &&
                Objects.equals(schema, tableName.schema) &&
                Objects.equals(name, tableName.name) &&
                Objects.equals(nameInDatabase, tableName.nameInDatabase);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(database, schema, name, nameInDatabase);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("database", database)
                .add("schema", schema)
                .add("name", name)
                .add("nameInDatabase", nameInDatabase)
                .toString();
    }
}
