/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.initialization;

import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;
import org.testng.ITestResult;

@FunctionalInterface
public interface TestMethodModuleProvider
{
    Module getModule(Configuration configuration, ITestResult testResult);
}
