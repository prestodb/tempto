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
package com.teradata.tempto.configuration;

import java.util.Map;
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

    Map<String, Object> asMap();
}
