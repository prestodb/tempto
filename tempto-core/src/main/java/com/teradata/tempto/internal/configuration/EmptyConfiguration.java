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

package com.teradata.tempto.internal.configuration;

import com.teradata.tempto.configuration.Configuration;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class EmptyConfiguration
        extends AbstractConfiguration
{

    private static final EmptyConfiguration INSTANCE = new EmptyConfiguration();

    public static Configuration emptyConfiguration()
    {
        return INSTANCE;
    }

    private EmptyConfiguration() {}

    @Override
    public Optional<Object> get(String key)
    {
        return Optional.empty();
    }

    @Override
    public Set<String> listKeys()
    {
        return Collections.emptySet();
    }

    @Override
    public Configuration getSubconfiguration(String keyPrefix)
    {
        return emptyConfiguration();
    }
}
