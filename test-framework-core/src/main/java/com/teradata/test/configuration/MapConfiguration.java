/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.configuration;

import com.beust.jcommander.internal.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.teradata.test.configuration.EmptyConfiguration.emptyConfiguration;
import static com.teradata.test.configuration.KeyUtils.joinKey;
import static java.util.Optional.empty;

/**
 * Map based configuration.
 * Configuration is stored in nested maps.
 *
 * For leafs - value is store in map.
 * For non-leaf - nested map is stored.
 *
 * Outer map is responsible for first part in all keys stored in configuration.
 * Maps stored at first internal level are responsible for second part in stored keys and so on.
 *
 * E.g for configuration storing following entries:
 * a.b.c = 3
 * a.x = 10
 * b.c = 15
 *
 * following map structure would be created
 *
 * {
 *     a : {
 *         b : {
 *             c : 3
 *         },
 *         x : 10
 *     },
 *     b : {
 *         c : 15
 *     }
 * }
 */
public class MapConfiguration
        extends AbstractConfiguration
{
    private final Map<String, Object> map;

    public MapConfiguration(Map<String, Object> map)
    {
        this.map = map;
    }

    @Override
    protected Optional<Object> getObject(String key)
    {
        List<String> keyParts = KeyUtils.splitKey(key);
        Iterator<String> keyPartsIterator = keyParts.iterator();

        Map<String, Object> currentMap = map;
        while (keyPartsIterator.hasNext()) {
            String keyPart = keyPartsIterator.next();
            Object currentObject = currentMap.get(keyPart);
            if (currentObject == null) {
                return empty();
            }
            if (!keyPartsIterator.hasNext()) {
                return Optional.of(currentObject);
            }
            else {
                if (currentObject instanceof Map) {
                    currentMap = (Map<String, Object>) currentObject;
                }
                else {
                    return empty();
                }
            }
        }
        return empty();
    }


    @Override
    public Set<String> listKeys()
    {
        Set<String> acc = Sets.newHashSet();
        listKeys(map, null, acc);
        return acc;
    }

    private void listKeys(Map<String, Object> map, String currentPrefix, Set<String> acc)
    {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String currentKey = joinKey(currentPrefix, entry.getKey());
            if (entry.getValue() instanceof Map) {
                listKeys((Map<String, Object>) entry.getValue(), currentKey, acc);
            } else {
                acc.add(currentKey);
            }
        }
    }

    @Override
    public Configuration getSubconfiguration(String keyPrefix)
    {
        Optional<Object> object = getObject(keyPrefix);
        if (object.isPresent() && object.get() instanceof Map) {
            return new MapConfiguration((Map<String, Object>) object.get());
        } else {
            return emptyConfiguration();
        }

    }
}
