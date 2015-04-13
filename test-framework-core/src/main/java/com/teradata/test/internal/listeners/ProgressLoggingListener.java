/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.listeners;

import com.google.common.base.Joiner;
import com.teradata.test.internal.logging.TestFrameworkLoggingAppender;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ProgressLoggingListener
        implements ITestListener
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ProgressLoggingListener.class);
    public static final String MDC_TEST_ID_KEY = "test_id";

    private int total;
    private int started;
    private int succeeded;
    private int skipped;
    private int failed;
    private long testStartTime;

    private final TestMetadataReader testMetadataReader;

    public ProgressLoggingListener()
    {
        this.testMetadataReader = new TestMetadataReader();
    }

    @Override
    public void onStart(ITestContext context)
    {
        total = context.getAllTestMethods().length;
        LOGGER.info("Starting tests running");
    }

    @Override
    public void onTestStart(ITestResult testCase)
    {
        TestMetadataReader.TestMetadata testMetadata = testMetadataReader.getTestMetadata(testCase);
        org.slf4j.MDC.put("test_id", testMetadata.testName);
        testStartTime = System.currentTimeMillis();

        LOGGER.info("");
        started++;
        LOGGER.info("[{} of {}] {} (Groups: {})",
                started, total, testMetadata.testName, Joiner.on(", ").join(testMetadata.testGroups));
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
        LOGGER.info("For tests logs see: {}", TestFrameworkLoggingAppender.getSelectedLogsDirectory().orElse("N/A"));
    }
}
