/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.initialization;

import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;

/**
 * Classes implementing this interface provide {@link Module}s
 * at suite level.
 */
@FunctionalInterface
public interface SuiteModuleProvider
{
    Module getModule(Configuration configuration);
}
