/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration;

import com.google.common.collect.ImmutableMap;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.configuration.KeyUtils;

import java.util.Map;
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
        return checkValueOfTypeOrParseIfNeeded(key, optionalValue, Integer.class, Integer::parseInt);
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
        return checkValueOfTypeOrParseIfNeeded(key, optionalValue, Boolean.class, Boolean::parseBoolean);
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

    private <T> Optional<T> checkValueOfTypeOrParseIfNeeded(String key, Optional<Object> optionalValue, Class<T> expectedClass, Parser<T> parser)
    {
        if (optionalValue.isPresent()) {
            Object value = optionalValue.get();
            if (value instanceof String) {
                return Optional.of(parser.parse((String) value));
            }
            else if (!(expectedClass.isAssignableFrom(value.getClass()))) {
                throw new IllegalStateException(format("expected %s value for key %s but got %s", expectedClass.getName(), key, value.getClass().getName()));
            }
        }
        return (Optional<T>) optionalValue;
    }

    private void checkValuePresent(Optional<?> value, String valueNotFoundErrorMessage)
    {
        if (!(value.isPresent())) {
            throw new IllegalStateException(valueNotFoundErrorMessage);
        }
    }

    private String standardValueNotFoundMessage(String key) {return String.format("could not find value for key %s", key);}

    @Override
    public Map<String, Object> asMap()
    {
        ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.<String, Object>builder();

        for (String key : listKeys()) {
            mapBuilder.put(key, get(key).get());
        }

        return mapBuilder.build();
    }

    @Override
    public int hashCode()
    {
        return asMap().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof AbstractConfiguration) {
            return asMap().equals(((Configuration) o).asMap());
        }
        else {
            return false;
        }
    }

    private interface Parser<T>
    {
        T parse(String s);
    }
}
