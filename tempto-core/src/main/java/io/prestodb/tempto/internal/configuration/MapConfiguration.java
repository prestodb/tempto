/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.internal.configuration;

import com.beust.jcommander.internal.Sets;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.configuration.KeyUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.prestodb.tempto.configuration.KeyUtils.joinKey;
import static io.prestodb.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration;
import static java.util.Optional.empty;

/**
 * <p>
 * This class constructs and stores a map based configuration using nested maps.
 * </p>
 * <p>
 * For leafs - value is stored in map.
 * For non-leaf - nested map is stored.
 * </p>
 * <p>
 * Outer map is responsible for first part in all keys stored in configuration.
 * Maps stored at first internal level are responsible for second part in stored keys and so on.
 * </p>
 * <p>
 * For example, for a configuration storing these hierarchical entries:
 * a.b.c = 3
 * a.x = 10
 * b.c = 15
 * </p>
 * <p>
 * the following map structure would be created
 * </p>
 * <pre>
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
 * </pre>
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
            return Optional.empty();
        }
        return object;
    }

    private Optional<Object> getObject(String key)
    {
        if (map.containsKey(key)) {
            return Optional.of(map.get(key));
        }

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
