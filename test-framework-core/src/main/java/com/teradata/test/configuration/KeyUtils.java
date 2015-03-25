/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

public final class KeyUtils
{
    private KeyUtils() {}

    private static final char KEY_SEPARATOR = '.';
    private static final Splitter KEY_SPLITTER = Splitter.on(KEY_SEPARATOR);
    private static final Joiner KEY_JOINER = Joiner.on(KEY_SEPARATOR).skipNulls();

    public static List<String> splitKey(String key)
    {
        return newArrayList(KEY_SPLITTER.split(key));
    }

    public static String joinKey(List<String> keyParts)
    {
        return KEY_JOINER.join(keyParts);
    }

    public static String joinKey(String... keyParts)
    {
        return joinKey(asList(keyParts));
    }

    public static String getKeyPrefix(String key, int prefixLen)
    {
        List<String> keyParts = splitKey(key);
        if (keyParts.size() <= prefixLen) {
            return key;
        }
        else {
            return KEY_JOINER.join(keyParts.subList(0, prefixLen));
        }
    }
}
