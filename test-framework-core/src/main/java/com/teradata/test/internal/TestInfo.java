/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal;

import org.testng.ITestResult;

public class TestInfo
{
    private final ITestResult testResult;

    public TestInfo(ITestResult testResult)
    {
        this.testResult = testResult;
    }

    public String getLongTestId()
    {
        return testResult.getTestClass().getRealClass().getName() + "." + testResult.getMethod().getMethodName() + "_" + testResult.getStartMillis();
    }

    public String getShortTestId()
    {
        return testResult.getTestClass().getRealClass().getSimpleName() + "." + testResult.getName() + "_" + testResult.getStartMillis();
    }
}
