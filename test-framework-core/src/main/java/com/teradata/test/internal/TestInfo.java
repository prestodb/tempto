/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal;

public class TestInfo
{
    private final String testName;

    public TestInfo(String testName)
    {
        this.testName = testName;
    }

    public String getTestName()
    {
        return testName;
    }

    @Override
    public String toString()
    {
        return testName;
    }
}
