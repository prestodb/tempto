/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.configuration;

import java.util.Optional;
import java.util.Set;

public interface Configuration
{
    Optional<Object> get(String key);

    Optional<String> getString(String key);

    String getStringMandatory(String key);

    String getStringMandatory(String key, String errorMessage);

    Optional<Integer> getInt(String key);

    int getIntMandatory(String key);

    int getIntMandatory(String key, String errorMessage);

    Optional<Boolean> getBoolean(String key);

    boolean getBooleanMandatory(String key);

    boolean getBooleanMandatory(String key, String errorMessage);

    /**
     * Lists all keys in configuration
     */
    Set<String> listKeys();

    /**
     * Lists configuration key prefixes of at most given length
     *
     * E.g. for configuration with keys:
     * a.b.c
     * a.d.e
     * b
     *
     * listKeyPrefixes(1) would return ["a", "b"]
     *
     * and
     *
     * listKeyPrefixes(2) would return ["a.b", "a.d", "b"]
     */
    Set<String> listKeyPrefixes(int prefixesLength);

    /**
     * Returns configuration containing all keys starting with given prefix.
     * Keys for returned configuration are stripped of this prefix.
     */
    Configuration getSubconfiguration(String keyPrefix);
}
