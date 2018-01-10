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

package io.prestodb.tempto.fulfillment.ldap;

import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.Requirement;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class LdapObjectRequirement
        implements Requirement
{
    private final List<LdapObjectDefinition> ldapObjectDefinitions;

    public LdapObjectRequirement(List<LdapObjectDefinition> ldapObjectDefinitions)
    {
        this.ldapObjectDefinitions = ImmutableList.copyOf(requireNonNull(ldapObjectDefinitions, "ldapObjectDefinition is null"));
    }

    public List<LdapObjectDefinition> getLdapObjectDefinitions()
    {
        return ldapObjectDefinitions;
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
}
