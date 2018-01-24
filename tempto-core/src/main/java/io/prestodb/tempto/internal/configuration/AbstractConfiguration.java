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

import com.google.common.collect.ImmutableMap;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.configuration.KeyUtils;

import java.util.Collections;
import java.util.List;
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
    public Optional<Double> getDouble(String key)
    {
        Optional<Object> optionalValue = get(key);
        return checkValueOfTypeOrParseIfNeeded(key, optionalValue, Double.class, Double::parseDouble);
    }

    @Override
    public double getDoubleMandatory(String key)

    {
        return getDoubleMandatory(key, standardValueNotFoundMessage(key));
    }

    @Override
    public double getDoubleMandatory(String key, String errorMessage)
    {
        Optional<Double> value = getDouble(key);
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
    public List<String> getStringList(String key)
    {
        Optional<Object> object = get(key);
        if (object.isPresent() && object.get() instanceof List) {
            return (List<String>) object.get();
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getStringListMandatory(String key, String errorMessage)
    {
        List<String> stringList = getStringList(key);
        if (stringList.isEmpty()) {
            throw new IllegalStateException(errorMessage);
        }
        return stringList;
    }

    @Override
    public List<String> getStringListMandatory(String key)
    {
        return getStringListMandatory(key, standardValueNotFoundMessage(key));
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
