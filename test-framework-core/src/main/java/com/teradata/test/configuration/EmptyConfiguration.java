/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.configuration;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class EmptyConfiguration extends AbstractConfiguration
{

    private static EmptyConfiguration INSTANCE = new EmptyConfiguration();

    public static Configuration emptyConfiguration() {
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
