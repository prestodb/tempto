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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.initialization.AutoModuleProvider;
import io.prestodb.tempto.initialization.SuiteModuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.inject.name.Names.named;

@AutoModuleProvider
public class TestConfigurationModuleProvider
        implements SuiteModuleProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfigurationModuleProvider.class);

    @Override
    public Module getModule(Configuration configuration)
    {
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(Configuration.class).toInstance(configuration);
                bindConfigurationKeys();
            }

            private void bindConfigurationKeys()
            {
                configuration.listKeys().forEach(key ->
                        configuration.get(key).ifPresent(value -> bind(getBindingKey(value, key)).toInstance(value)));
            }

            private Key getBindingKey(Object configValue, String configKey)
            {
                if (configValue instanceof List) {
                    // currently only list of strings is supported
                    return Key.get(new TypeLiteral<List<String>>() {}, named(configKey));
                }
                return Key.get(configValue.getClass(), named(configKey));
            }
        };
    }
}
