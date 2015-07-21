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

import java.util.Optional;

public class ImmutableTableRequirement
        extends TableRequirement
{

    public ImmutableTableRequirement(TableDefinition tableDefinition)
    {
        this(tableDefinition, Optional.empty());
    }

    public ImmutableTableRequirement(TableDefinition tableDefinition, Optional<String> databaseName)
    {
        super(tableDefinition, databaseName);
    }

    @Override
    public ImmutableTableRequirement copyWithDatabase(String databaseName)
    {
        return new ImmutableTableRequirement(getTableDefinition(), Optional.of(databaseName));
    }
}
