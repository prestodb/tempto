/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration;

import com.google.common.collect.ImmutableMap;
import com.teradata.test.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;

public class YamlConfiguration
        extends DelegateConfiguration
{
    private static final StrSubstitutor ENV_SUBSTITUTOR = new StrSubstitutor(System.getenv());

    private final MapConfiguration mapConfiguration;

    public YamlConfiguration(String yamlString)
    {
        this(new ByteArrayInputStream(yamlString.getBytes(UTF_8)));
    }

    public YamlConfiguration(InputStream yamlInputStream)
    {
        String yamlString = replaceEnvVariables(yamlInputStream);
        yamlString = replaceConfigurationVariables(yamlString);
        mapConfiguration = loadConfiguration(yamlString);
    }

    private String replaceEnvVariables(InputStream yamlInputStream)
    {
        return ENV_SUBSTITUTOR.replace(inputStreamToStringSafe(yamlInputStream));
    }

    private String replaceConfigurationVariables(String yamlString)
    {
        while (true) {
            // TODO solve circular variable references
            MapConfiguration temporaryConfiguration = loadConfiguration(yamlString);
            StrSubstitutor variableSubstitutor = new StrSubstitutor(toMap(temporaryConfiguration));
            String yamlStringTemporary = variableSubstitutor.replace(yamlString);
            if (yamlString.equals(yamlStringTemporary)) {
                break;
            }
            else {
                yamlString = yamlStringTemporary;
            }
        }
        return yamlString;
    }

    private MapConfiguration loadConfiguration(String yamlString)
    {
        Yaml yaml = new Yaml();
        Object loadedYaml = yaml.load(yamlString);
        checkArgument(loadedYaml instanceof Map, "yaml does not evaluate to map object; got %s", loadedYaml.getClass().getName());
        Map<String, Object> loadedYamlMap = (Map<String, Object>) loadedYaml;
        return new MapConfiguration(loadedYamlMap);
    }

    private Map<String, Object> toMap(MapConfiguration configuration)
    {
        ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.<String, Object>builder();

        for (String key : configuration.listKeys()) {
            mapBuilder.put(key, configuration.get(key).get());
        }

        return mapBuilder.build();
    }

    @Override
    protected Configuration getDelegate()
    {
        return mapConfiguration;
    }

    private String inputStreamToStringSafe(InputStream yamlInputStream)
    {
        try {
            return IOUtils.toString(yamlInputStream, UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
