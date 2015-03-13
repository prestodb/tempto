/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.listeners;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

class ProgressLoggingListener implements ITestListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProgressLoggingListener.class);

    private int current;
    private int total;
    private int succeeded;
    private int skipped;
    private int failed;

    @Override
    public void onStart(ITestContext context) {
        current = 1;
        total = context.getAllTestMethods().length;

        succeeded = skipped = failed = 0;
    }

    @Override
    public void onTestStart(ITestResult testCase) {
        String qualifiedTestName = testCase.getInstanceName() + "." + testCase.getName();
        LOGGER.info("[{} of {}] {} (Groups: {})",
                    current++, total++, qualifiedTestName, Joiner.on(' ').join(testCase.getMethod().getGroups()));
    }

    @Override
    public void onTestSuccess(ITestResult testCase) {
        succeeded++;
    }

    @Override
    public void onTestFailure(ITestResult testCase) {
    }

    @Override
    public void onTestSkipped(ITestResult testCase) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testCase) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }
}
