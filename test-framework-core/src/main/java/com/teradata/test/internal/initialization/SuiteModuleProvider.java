/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.initialization;

import com.google.inject.Module;
import com.teradata.test.configuration.Configuration;

@FunctionalInterface
public interface SuiteModuleProvider
{
    Module getModule(Configuration configuration);
}
