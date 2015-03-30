/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration;

import com.beust.jcommander.internal.Sets;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.configuration.KeyUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.teradata.test.internal.configuration.EmptyConfiguration.emptyConfiguration;
import static com.teradata.test.configuration.KeyUtils.joinKey;
import static java.util.Optional.empty;

/**
 * This class constructs and stores a map based configuration using nested maps.
 * <p/>
 * For leafs - value is stored in map.
 * For non-leaf - nested map is stored.
 * <p/>
 * Outer map is responsible for first part in all keys stored in configuration.
 * Maps stored at first internal level are responsible for second part in stored keys and so on.
 * <p/>
 * For example, for a configuration storing these hierarchical entries:
 * a.b.c = 3
 * a.x = 10
 * b.c = 15
 * <p/>
 * the following map structure would be created
 * <p/>
 * {
 * a : {
 *  b : {
 *   c : 3
 *  },
 *  x : 10
 *  },
 * b : {
 *  c : 15
 *  }
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
    public Optional<Object> get(String key)
    {
        Optional<Object> object = getObject(key);
        if (object.isPresent() && object.get() instanceof Map) {
            throw new IllegalArgumentException("Provided key <" + key + "> for non leaf configuration property");
        }
        return object;
    }

    private Optional<Object> getObject(String key)
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
            }
            else {
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
        }
        else {
            return emptyConfiguration();
        }
    }
}
