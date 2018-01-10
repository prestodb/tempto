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

package io.prestodb.tempto.configuration;

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
