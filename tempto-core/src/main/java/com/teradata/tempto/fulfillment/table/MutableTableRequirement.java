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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class MutableTableRequirement
        implements Requirement
{
    public enum State
    {
        /**
         * Table won't be created by tempto framework, but will be
         * visible in {@link com.teradata.tempto.fulfillment.table.MutableTablesState}
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

    private final TableDefinition tableDefinition;
    private final String name;
    private final State state;

    public MutableTableRequirement(TableDefinition tableDefinition)
    {
        this(tableDefinition, tableDefinition.getName(), LOADED);
    }

    public MutableTableRequirement(TableDefinition tableDefinition, String name, State state)
    {
        this.tableDefinition = checkNotNull(tableDefinition);
        this.name = checkNotNull(name);
        this.state = checkNotNull(state);
    }

    public TableDefinition getTableDefinition()
    {
        return tableDefinition;
    }

    public String getName()
    {
        return name;
    }

    public State getState()
    {
        return state;
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
        private String name;
        private State state;

        public MutableTableRequirementBuilder(TableDefinition tableDefinition)
        {
            this.tableDefinition = tableDefinition;
            this.name = tableDefinition.getName();
        }

        public MutableTableRequirementBuilder withName(String name)
        {
            this.name = name;
            return this;
        }

        public MutableTableRequirementBuilder withState(State state)
        {
            this.state = state;
            return this;
        }

        public MutableTableRequirement build()
        {
            return new MutableTableRequirement(tableDefinition, name, state);
        }
    }
}
