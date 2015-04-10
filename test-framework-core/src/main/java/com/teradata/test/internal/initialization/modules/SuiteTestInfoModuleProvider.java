/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.initialization.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.internal.TestInfo;
import com.teradata.test.internal.initialization.SuiteModuleProvider;

import java.util.Date;

public class SuiteTestInfoModuleProvider
        implements SuiteModuleProvider
{
    @Override
    public Module getModule(Configuration configuration)
    {
        TestInfo testInfo = new TestInfo("SUITE", new Date());
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(TestInfo.class).toInstance(testInfo);
            }
        };
    }
}
