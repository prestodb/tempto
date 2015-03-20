/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.configuration;

import java.util.Optional;
import java.util.Set;

import static com.teradata.test.configuration.KeyUtils.getKeyPrefix;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractConfiguration
        implements Configuration
{

    @Override
    public Optional<String> getString(String key)
    {
        Optional<Object> optionalValue = get(key);
        checkValueOfType(key, optionalValue, String.class);
        return (Optional) optionalValue;
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
    public Set<String> listKeyPrefixes(int prefixesLength)
    {
        Set<String> keys = listKeys();
        return keys.stream()
                .map(key -> getKeyPrefix(key, prefixesLength))
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
