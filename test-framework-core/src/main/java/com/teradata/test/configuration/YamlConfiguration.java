/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class YamlConfiguration
        extends DelegateConfiguration
{

    private final MapConfiguration mapConfiguration;

    private YamlConfiguration(InputStream yamlInputStream)
    {
        Yaml yaml = new Yaml();
        Object loadedYaml = yaml.load(yamlInputStream);
        checkArgument(loadedYaml instanceof Map, "yaml does not evaluate to map object; got %s", loadedYaml.getClass().getName());
        mapConfiguration = new MapConfiguration((Map<String, Object>) loadedYaml);
    }

    @Override
    protected Configuration getDelegate()
    {
        return mapConfiguration;
    }

}
