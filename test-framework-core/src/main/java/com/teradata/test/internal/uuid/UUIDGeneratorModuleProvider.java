/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.uuid;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;

@AutoModuleProvider
public class UUIDGeneratorModuleProvider implements SuiteModuleProvider
{
    @Override
    public Module getModule(Configuration configuration)
    {
        return new AbstractModule() {
            @Override
            protected void configure()
            {
                bind(UUIDGenerator.class).to(DefaultUUIDGenerator.class).in(Singleton.class);
            }
        };
    }
}
