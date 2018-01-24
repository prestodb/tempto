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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class MutableTableRequirement
        extends TableRequirement
{
    public enum State
    {
        /**
         * Table won't be created by tempto framework, but will be
         * visible in {@link MutableTablesState}
         * and will be dropped by tempto framework.
         */
        PREPARED,
        /**
         * Table will be created and dropped by tempto framework, but
         * data won't be loaded.
         */
        CREATED,
        /**
         * Table with data will be created and dropped by tempto framework.
         */
        LOADED
    }

    private final State state;

    private MutableTableRequirement(TableHandle handle, State state, TableDefinition tableDefinition)
    {
        super(tableDefinition, handle);
        this.state = checkNotNull(state);
    }

    public State getState()
    {
        return state;
    }

    @Override
    public TableRequirement copyWithDatabase(String databaseName)
    {
        TableHandle tableHandle = getTableHandle().inDatabase(databaseName);
        return builder(getTableDefinition())
                .withState(getState())
                .withTableHandle(tableHandle)
                .build();
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

    public static MutableTableRequirementBuilder builder(TableDefinition tableDefinition)
    {
        return new MutableTableRequirementBuilder(tableDefinition);
    }

    public static class MutableTableRequirementBuilder
    {
        private final TableDefinition tableDefinition;
        private TableHandle tableHandle;
        private State state = LOADED;

        public MutableTableRequirementBuilder(TableDefinition tableDefinition)
        {
            this.tableDefinition = tableDefinition;
            this.tableHandle = tableDefinition.getTableHandle();
        }

        public MutableTableRequirementBuilder withName(String name)
        {
            this.tableHandle = tableHandle.withName(name);
            return this;
        }

        public MutableTableRequirementBuilder withSchema(String schema)
        {
            this.tableHandle = tableHandle.inSchema(schema);
            return this;
        }

        public MutableTableRequirementBuilder withDatabase(String database)
        {
            this.tableHandle = tableHandle.inDatabase(database);
            return this;
        }

        public MutableTableRequirementBuilder withTableHandle(TableHandle tableHandle)
        {
            this.tableHandle = tableHandle;
            return this;
        }

        public MutableTableRequirementBuilder withState(State state)
        {
            this.state = state;
            return this;
        }

        public MutableTableRequirement build()
        {
            return new MutableTableRequirement(tableHandle, state, tableDefinition);
        }
    }
}
