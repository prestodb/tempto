/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.listeners;

import com.google.common.base.Joiner;
import com.teradata.test.internal.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import static com.teradata.test.internal.initialization.RequirementsExpander.getMethodsCountFromContext;

public class ProgressLoggingListener
        implements ITestListener
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ProgressLoggingListener.class);
    public static final String MDC_TEST_ID_KEY = "test_id";

    private int started;
    private int succeeded;
    private int skipped;
    private int failed;
    private long testStartTime;

    @Override
    public void onStart(ITestContext context)
    {
        LOGGER.info("Starting tests running");
    }

    @Override
    public void onTestStart(ITestResult testCase)
    {
        String testId = getMdcTestId(testCase);
        org.slf4j.MDC.put("test_id", testId);
        testStartTime = System.currentTimeMillis();

        LOGGER.info("");
        started++;
        LOGGER.info("[{} of {}] {} (Groups: {})",
                started, getMethodsCountFromContext(testCase.getTestContext()), testId, Joiner.on(", ").join(testCase.getMethod().getGroups()));
    }

    private String getMdcTestId(ITestResult testCase)
    {
        return new TestInfo(testCase).getShortTestId();
    }

    @Override
    public void onTestSuccess(ITestResult testCase)
    {
        succeeded++;
        logTestEnd("SUCCESS");
    }

    @Override
    public void onTestFailure(ITestResult testCase)
    {
        failed++;
        if (testCase.getThrowable() != null) {
            LOGGER.error("Exception: ", testCase.getThrowable());
        }
        logTestEnd("FAILURE");
    }

    @Override
    public void onTestSkipped(ITestResult testCase)
    {
        skipped++;
        logTestEnd("SKIPPED");
    }

    private void logTestEnd(String outcome)
    {
        long executionTime = System.currentTimeMillis() - testStartTime;
        LOGGER.info("{}     /    took: {}ms", outcome, executionTime);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testCase)
    {
    }

    @Override
    public void onFinish(ITestContext context)
    {
        org.slf4j.MDC.remove(MDC_TEST_ID_KEY);
        LOGGER.info("");
        LOGGER.info("Completed {} tests", started);
        LOGGER.info("{} SUCCEEDED      /      {} FAILED      /      {} SKIPPED", succeeded, failed, skipped);
    }
}
