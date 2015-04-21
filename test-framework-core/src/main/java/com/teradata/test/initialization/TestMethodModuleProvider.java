/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.initialization;

import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;
import org.testng.ITestResult;

/**
 * Classes implementing this interface provide {@link Module}s
 * at test method level.
 */
@FunctionalInterface
public interface TestMethodModuleProvider
{
    Module getModule(Configuration configuration, ITestResult testResult);
}
