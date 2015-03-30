/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal;

import java.util.Date;

public class TestInfo
{
    private final String testName;
    private final Date executionStart;

    public TestInfo(String testName, Date executionStart)
    {
        this.testName = testName;
        this.executionStart = executionStart;
    }

    public String getTestName()
    {
        return testName;
    }

    public Date getExecutionStart()
    {
        return executionStart;
    }

    @Override
    public String toString()
    {
        return testName + "/" + executionStart;
    }
}
