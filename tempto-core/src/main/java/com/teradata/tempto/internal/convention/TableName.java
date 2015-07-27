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

package com.teradata.tempto.internal.convention;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class TableName
{
    public static TableName parse(String value)
    {
        if (value.contains(".")) {
            List<String> parts = Splitter.on('.').splitToList(value);
            checkState(parts.size() == 2, "Invalid table name syntax. Expected at most one occurrence of '.' in '%s'.", value);
            return new TableName(parts.get(1), Optional.of(parts.get(0)));
        }
        else {
            return new TableName(value, Optional.empty());
        }
    }

    private final Optional<String> prefix;

    private final String name;

    public TableName(String name, Optional<String> prefix)
    {
        this.prefix = prefix;
        this.name = name;
    }

    public Optional<String> getPrefix()
    {
        return prefix;
    }

    public String getName()
    {
        return name;
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
        return prefix.isPresent() ? String.format("%s.%s", prefix.get(), name) : name;
    }
}
