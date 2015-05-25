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
package com.teradata.test.query;

import com.teradata.test.context.State;

import java.util.Optional;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Contains configuration required to connect to a JDBC server.
 */
public class JdbcConnectivityParamsState
        implements State
{
    private final String name;
    public final String driverClass;
    public final String url;
    public final String user;
    public final String password;
    public final boolean pooling;
    public final Optional<String> jar;

    public JdbcConnectivityParamsState(String name, String driverClass, String url, String user, String password, boolean pooling, Optional<String> jar)
    {
        this.name = name;
        this.driverClass = driverClass;
        this.url = url;
        this.user = user;
        this.password = password;
        this.pooling = pooling;
        this.jar = jar;
    }

    @Override
    public Optional<String> getName()
    {
        return Optional.of(name);
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
        return reflectionToString(this);
    }
}
