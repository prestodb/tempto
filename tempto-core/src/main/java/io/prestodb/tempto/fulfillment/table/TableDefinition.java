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

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class TableDefinition
{
    protected final TableHandle handle;

    public TableDefinition(TableHandle handle)
    {
        this.handle = requireNonNull(handle, "handle is null");
    }

    public String getName()
    {
        return handle.getName();
    }

    public Optional<String> getSchema()
    {
        return handle.getSchema();
    }

    public Optional<String> getDatabase()
    {
        return handle.getDatabase();
    }

    public TableHandle getTableHandle()
    {
        return handle;
    }
}
