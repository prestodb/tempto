/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.initialization.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.internal.TestInfo;
import com.teradata.test.initialization.TestMethodModuleProvider;
import org.testng.ITestResult;

public class TestMethodInfoModuleProvider
        implements TestMethodModuleProvider
{
    public Module getModule(Configuration configuration, ITestResult testResult)
    {
        TestInfo testInfo = new TestInfo(testResult.getTestClass().getRealClass().getName() + "." + testResult.getMethod().getMethodName());
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
