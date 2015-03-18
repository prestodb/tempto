/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import com.teradata.test.configuration.Configuration;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class BaseConfiguration
        implements Configuration
{
    public abstract Optional<String> getString(String key);

    public String getStringMandatory(String key)
    {
        return getStringMandatory(key, key + " must be specified.");
    }

    public String getStringMandatory(String key, String errorMessage)
    {
        Optional<String> optionalString = getString(key);
        checkArgument(optionalString.isPresent(), errorMessage);
        return optionalString.get();
    }
}
