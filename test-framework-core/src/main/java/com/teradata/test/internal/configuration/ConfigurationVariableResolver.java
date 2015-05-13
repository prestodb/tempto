/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration;

import com.teradata.test.configuration.Configuration;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.tuple.Pair;

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
        configuration = replaceEnvVariables(configuration);
        return replaceConfigurationVariables(configuration);
    }

    private Configuration replaceEnvVariables(Configuration configuration)
    {
        return resolveVariables(configuration, System.getenv());
    }

    private Configuration replaceConfigurationVariables(Configuration configuration)
    {
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
            else {
                return Pair.of(prefix, value);
            }
        }
        else {
            return Pair.of(prefix, resolveVariables(configuration.getSubconfiguration(prefix), strSubstitutor));
        }
    }
}
