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

import io.prestodb.tempto.configuration.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class DelegateConfiguration
        implements Configuration
{
    protected abstract Configuration getDelegate();

    @Override
    public Optional<Object> get(String key)
    {
        return getDelegate().get(key);
    }

    @Override
    public Optional<String> getString(String key)
    {
        return getDelegate().getString(key);
    }

    @Override
    public String getStringMandatory(String key)
    {
        return getDelegate().getStringMandatory(key);
    }

    @Override
    public String getStringMandatory(String key, String errorMessage)
    {
        return getDelegate().getStringMandatory(key, errorMessage);
    }

    @Override
    public Optional<Integer> getInt(String key)
    {
        return getDelegate().getInt(key);
    }

    @Override
    public int getIntMandatory(String key)
    {
        return getDelegate().getIntMandatory(key);
    }

    @Override
    public int getIntMandatory(String key, String errorMessage)
    {
        return getDelegate().getIntMandatory(key, errorMessage);
    }

    @Override
    public Optional<Double> getDouble(String key)
    {
        return getDelegate().getDouble(key);
    }

    @Override
    public double getDoubleMandatory(String key)
    {
        return getDelegate().getDoubleMandatory(key);
    }

    @Override
    public double getDoubleMandatory(String key, String errorMessage)
    {
        return getDelegate().getDoubleMandatory(key, errorMessage);
    }

    @Override
    public Optional<Boolean> getBoolean(String key)
    {
        return getDelegate().getBoolean(key);
    }

    @Override
    public boolean getBooleanMandatory(String key)
    {
        return getDelegate().getBooleanMandatory(key);
    }

    @Override
    public boolean getBooleanMandatory(String key, String errorMessage)
    {
        return getDelegate().getBooleanMandatory(key, errorMessage);
    }

    @Override
    public List<String> getStringList(String key)
    {
        return getDelegate().getStringList(key);
    }

    @Override
    public List<String> getStringListMandatory(String key, String errorMessage)
    {
        return getDelegate().getStringListMandatory(key, errorMessage);
    }

    @Override
    public List<String> getStringListMandatory(String key)
    {
        return getDelegate().getStringListMandatory(key);
    }

    @Override
    public Set<String> listKeys()
    {
        return getDelegate().listKeys();
    }

    @Override
    public Set<String> listKeyPrefixes(int prefixesLength)
    {
        return getDelegate().listKeyPrefixes(prefixesLength);
    }

    @Override
    public Configuration getSubconfiguration(String keyPrefix)
    {
        return getDelegate().getSubconfiguration(keyPrefix);
    }

    @Override
    public Map<String, Object> asMap()
    {
        return getDelegate().asMap();
    }

    @Override
    public int hashCode()
    {
        return getDelegate().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return getDelegate().equals(o);
    }
}
