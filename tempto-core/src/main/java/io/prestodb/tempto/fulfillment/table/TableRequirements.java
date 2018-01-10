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

public class TableRequirements
{
    private TableRequirements() {}

    /**
     * Requirement for mutable table.
     * Test code is allowed to mutate (insert/delete rows) for mutable table
     *
     * @param tableDefinition Encapsulated DDL for table
     * @return Requirement for mutable table
     */
    public static MutableTableRequirement mutableTable(TableDefinition tableDefinition)
    {
        return MutableTableRequirement.builder(tableDefinition).build();
    }

    /**
     * Requirement for mutable table.
     * Test code is allowed to mutate (insert/delete rows) for mutable table
     *
     * @param tableDefinition Encapsulated DDL for table
     * @param name Name for table
     * @param state PREPARED, CREATED, or LOADED
     * @return Requirement for mutable table
     */
    public static MutableTableRequirement mutableTable(TableDefinition tableDefinition, String name, MutableTableRequirement.State state)
    {
        return MutableTableRequirement.builder(tableDefinition).withName(name).withState(state).build();
    }

    /**
     * Requirement for immutable table.
     *
     * @param tableDefinition Encapsulated DDL for table
     * @return ImmutableTableRequirement
     */
    public static ImmutableTableRequirement immutableTable(TableDefinition tableDefinition)
    {
        return new ImmutableTableRequirement(tableDefinition);
    }
}
