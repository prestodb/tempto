/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.configuration;

import java.util.Optional;
import java.util.Set;

public abstract class DelegateConfiguration
        implements Configuration
{

    protected abstract Configuration getDelegate();

    @Override
    public Optional<String> getString(String key)
    {
        return getDelegate().getString(key);
    }

    @Override
    public String getStringMandatory(String key)
    {
        return getDelegate().getStringMandatory(key);
    }

    @Override
    public String getStringMandatory(String key, String errorMessage)
    {
        return getDelegate().getStringMandatory(key, errorMessage);
    }

    @Override
    public Optional<Integer> getInt(String key)
    {
        return getDelegate().getInt(key);
    }

    @Override
    public int getIntMandatory(String key)
    {
        return getDelegate().getIntMandatory(key);
    }

    @Override
    public int getIntMandatory(String key, String errorMessage)
    {
        return getDelegate().getIntMandatory(key, errorMessage);
    }

    @Override
    public Set<String> listKeys()
    {
        return getDelegate().listKeys();
    }

    @Override
    public Set<String> listKeyPrefixes(int prefixesLength)
    {
        return getDelegate().listKeyPrefixes(prefixesLength);
    }

    @Override
    public Configuration getSubconfiguration(String keyPrefix)
    {
        return getDelegate().getSubconfiguration(keyPrefix);
    }
}
