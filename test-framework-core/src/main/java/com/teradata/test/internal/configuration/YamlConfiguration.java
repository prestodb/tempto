/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration;

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
        String yamlString = ENV_SUBSTITUTOR.replace(inputStreamToStringSafe(yamlInputStream));
        Yaml yaml = new Yaml();
        Object loadedYaml = yaml.load(yamlString);
        checkArgument(loadedYaml instanceof Map, "yaml does not evaluate to map object; got %s", loadedYaml.getClass().getName());
        mapConfiguration = new MapConfiguration((Map<String, Object>) loadedYaml);
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
