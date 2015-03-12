/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import java.util.Optional;

public class SystemPropertiesConfiguration
        extends BaseConfiguration
{
    @Override
    public Optional<String> getString(String key)
    {
        return Optional.ofNullable(System.getProperty(key));
    }
}
