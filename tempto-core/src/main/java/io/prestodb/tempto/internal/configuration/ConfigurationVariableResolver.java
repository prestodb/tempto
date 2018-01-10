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

import io.prestodb.tempto.configuration.Configuration;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolves ${variable} from configuration value fields and replaces them with environment
 * variables or other configuration values (referenced as absolute keys).
 */
public class ConfigurationVariableResolver
{
    public Configuration resolve(Configuration configuration)
    {
        configuration = resolveVariables(configuration, System.getenv());
        configuration = resolveVariables(configuration, (Map) System.getProperties());
        return resolveVariables(configuration, configuration.asMap());
    }

    private Configuration resolveVariables(Configuration configuration, Map<String, ? extends Object> variables)
    {
        StrSubstitutor strSubstitutor = new StrSubstitutor(variables);
        return new MapConfiguration(resolveVariables(configuration, strSubstitutor));
    }

    private Map<String, Object> resolveVariables(Configuration configuration, StrSubstitutor strSubstitutor)
    {
        return configuration.listKeyPrefixes(1)
                .stream()
                .map(prefix -> resolveConfigurationEntry(configuration, prefix, strSubstitutor))
                .collect(Collectors.toMap(entry -> entry.getLeft(), entry -> entry.getRight()));
    }

    private Pair<String, Object> resolveConfigurationEntry(Configuration configuration, String prefix, StrSubstitutor strSubstitutor)
    {
        Optional<Object> optionalValue = configuration.get(prefix);
        if (optionalValue.isPresent()) {
            Object value = optionalValue.get();
            if (value instanceof String) {
                return Pair.of(prefix, strSubstitutor.replace(value));
            }
            else if (value instanceof List) {
                List<String> resolvedList = new ArrayList<String>();
                for (String entry : (List<String>) value) {
                    resolvedList.add(strSubstitutor.replace(entry));
                }
                return Pair.of(prefix, resolvedList);
            }
            else {
                return Pair.of(prefix, value);
            }
        }
        else {
            return Pair.of(prefix, resolveVariables(configuration.getSubconfiguration(prefix), strSubstitutor));
        }
    }
}
