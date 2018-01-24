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

package io.prestodb.tempto.internal.configuration;

import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.configuration.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Configuration which can be built from multiple configuration. So one configuration can be overridden with another.
 * Returned value depends on order of configurations passed to constructor. Configuration are considered from last to first.
 * First found value is returned.
 */
public class HierarchicalConfiguration
        extends AbstractConfiguration
{
    private final List<Configuration> configurations;

    public HierarchicalConfiguration(Configuration... configurations)
    {
        this(asList(configurations));
    }

    public HierarchicalConfiguration(List<Configuration> configurations)
    {
        this.configurations = ImmutableList.copyOf(configurations);
    }

    @Override
    public Optional<Object> get(String key)
    {
        return configurations.stream()
                .map(configuration -> configuration.get(key))
                .reduce((first, second) -> exist(second) ? second : first)
                .get();
    }

    private static boolean exist(Optional<Object> optional)
    {
        return optional != null && optional.isPresent();
    }

    @Override
    public Set<String> listKeys()
    {
        return configurations.stream()
                .flatMap(configuration -> configuration.listKeys().stream())
                .collect(toSet());
    }

    @Override
    public Configuration getSubconfiguration(String keyPrefix)
    {
        List<Configuration> subConfigurations = configurations.stream()
                .map(configuration -> configuration.getSubconfiguration(keyPrefix))
                .collect(toList());
        return new HierarchicalConfiguration(subConfigurations);
    }
}
