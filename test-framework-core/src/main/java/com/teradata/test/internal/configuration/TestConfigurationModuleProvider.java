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

package com.teradata.test.internal.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
                for (String key : configuration.listKeys()) {
                    Optional<Object> value = configuration.get(key);
                    if (value.isPresent()) {
                        @SuppressWarnings("unchecked") Class<Object> valueClass = (Class<Object>) value.get().getClass();

                        LOGGER.debug("Binding {} key: {} -> {}", valueClass.getName(), key, value.get());

                        bind(valueClass)
                                .annotatedWith(named(key))
                                .toInstance(value.get());
                    }
                }
            }
        };
    }
}
