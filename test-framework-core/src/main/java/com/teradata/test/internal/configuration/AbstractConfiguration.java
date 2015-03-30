/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration;

import com.teradata.test.configuration.Configuration;
import com.teradata.test.configuration.KeyUtils;

import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractConfiguration
        implements Configuration
{

    @Override
    public Optional<String> getString(String key)
    {
        Optional<Object> optionalValue = get(key);
        return optionalValue.map(Object::toString);
    }

    @Override
    public String getStringMandatory(String key)
    {
        return getStringMandatory(key, standardValueNotFoundMessage(key));
    }

    @Override
    public String getStringMandatory(String key, String errorMessage)
    {
        Optional<String> value = getString(key);
        checkValuePresent(value, errorMessage);
        return value.get();
    }

    @Override
    public Optional<Integer> getInt(String key)
    {
        Optional<Object> optionalValue = get(key);
        checkValueOfType(key, optionalValue, Integer.class);
        return (Optional) optionalValue;
    }

    @Override
    public int getIntMandatory(String key)
    {
        return getIntMandatory(key, standardValueNotFoundMessage(key));
    }

    @Override
    public int getIntMandatory(String key, String errorMessage)
    {
        Optional<Integer> value = getInt(key);
        checkValuePresent(value, errorMessage);
        return value.get();
    }

    @Override
    public Optional<Boolean> getBoolean(String key)
    {
        Optional<Object> optionalValue = get(key);
        checkValueOfType(key, optionalValue, Boolean.class);
        return (Optional) optionalValue;
    }

    @Override
    public boolean getBooleanMandatory(String key)
    {
        return getBooleanMandatory(key, standardValueNotFoundMessage(key));
    }

    @Override
    public boolean getBooleanMandatory(String key, String errorMessage)
    {
        Optional<Boolean> value = getBoolean(key);
        checkValuePresent(value, errorMessage);
        return value.get();
    }

    @Override
    public Set<String> listKeyPrefixes(int prefixesLength)
    {
        Set<String> keys = listKeys();
        return keys.stream()
                .map(key -> KeyUtils.getKeyPrefix(key, prefixesLength))
                .collect(toSet());
    }

    private void checkValueOfType(String key, Optional<Object> optionalValue, Class<?> expectedClass)
    {
        if (optionalValue.isPresent()) {
            Object value = optionalValue.get();
            if (!(expectedClass.isAssignableFrom(value.getClass()))) {
                throw new IllegalStateException(format("expected %s value for key %s but got %s", expectedClass.getName(), key, value.getClass().getName()));
            }
        }
    }

    private void checkValuePresent(Optional<?> value, String valueNotFoundErrorMessage)
    {
        if (!(value.isPresent())) {
            throw new IllegalStateException(valueNotFoundErrorMessage);
        }
    }

    private String standardValueNotFoundMessage(String key) {return String.format("could not find value for key %s", key);}
}
