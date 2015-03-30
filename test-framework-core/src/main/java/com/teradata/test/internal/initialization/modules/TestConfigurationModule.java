/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.initialization.modules;

import com.google.inject.AbstractModule;
import com.teradata.test.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.google.inject.name.Names.named;

public class TestConfigurationModule
        extends AbstractModule
{

    private static final Logger logger = LoggerFactory.getLogger(TestConfigurationModule.class);

    private final Configuration testConfiguration;

    public TestConfigurationModule(Configuration testConfiguration)
    {
        this.testConfiguration = testConfiguration;
    }

    @Override
    protected void configure()
    {
        bind(Configuration.class).toInstance(testConfiguration);
        bindConfigurationKeys();
    }

    private void bindConfigurationKeys()
    {
        for (String key : testConfiguration.listKeys()) {
            Optional<Object> value = testConfiguration.get(key);
            if (value.isPresent()) {
                @SuppressWarnings("unchecked") Class<Object> valueClass = (Class<Object>) value.get().getClass();

                logger.debug("Binding {} key: {} -> {}", valueClass.getName(), key, value.get());

                bind(valueClass)
                        .annotatedWith(named(key))
                        .toInstance(value.get());
            }
        }
    }
}
