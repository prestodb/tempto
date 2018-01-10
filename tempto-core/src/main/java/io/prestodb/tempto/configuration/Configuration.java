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

import java.util.List;
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

    Optional<Double> getDouble(String key);

    double getDoubleMandatory(String key);

    double getDoubleMandatory(String key, String errorMessage);

    Optional<Boolean> getBoolean(String key);

    boolean getBooleanMandatory(String key);

    boolean getBooleanMandatory(String key, String errorMessage);

    List<String> getStringList(String key);

    List<String> getStringListMandatory(String key, String errorMessage);

    List<String> getStringListMandatory(String key);

    /**
     * Lists all keys in configuration
     *
     * @return List of keys
     */
    Set<String> listKeys();

    /**
     * Lists configuration key prefixes of at most given length
     * <p>
     * E.g. for configuration with keys:
     * a.b.c
     * a.d.e
     * b
     * <p>
     * listKeyPrefixes(1) would return ["a", "b"]
     * <p>
     * and
     * <p>
     * listKeyPrefixes(2) would return ["a.b", "a.d", "b"]
     *
     * @param prefixesLength Max size of prefix to list
     * @return List of prefixes
     */
    Set<String> listKeyPrefixes(int prefixesLength);

    /**
     * Returns configuration containing all keys starting with given prefix.
     * Keys for returned configuration are stripped of this prefix.
     *
     * @param keyPrefix Prefix to search for
     * @return Configuration containing all keys starting with given prefix.
     */
    Configuration getSubconfiguration(String keyPrefix);

    Map<String, Object> asMap();
}
