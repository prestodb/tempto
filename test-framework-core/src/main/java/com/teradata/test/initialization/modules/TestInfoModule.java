/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.initialization.modules;

import com.google.inject.AbstractModule;
import com.teradata.test.TestInfo;

import java.util.Date;

public class TestInfoModule
        extends AbstractModule
{

    private final TestInfo testInfo;

    public TestInfoModule(String testName)
    {
        this.testInfo = new TestInfo(testName, new Date());
    }

    @Override
    protected void configure()
    {
        bind(TestInfo.class).toInstance(testInfo);
    }
}
