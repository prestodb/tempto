/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.initialization.modules;

import com.google.inject.AbstractModule;
import com.teradata.test.configuration.Configuration;

public class TestConfigurationModule
        extends AbstractModule
{
    private final Configuration testConfiguration;

    public TestConfigurationModule(Configuration testConfiguration)
    {
        this.testConfiguration = testConfiguration;
    }

    @Override
    protected void configure()
    {
        bind(Configuration.class).toInstance(testConfiguration);
    }
}
