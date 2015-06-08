/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tempto.internal.listeners;

import com.google.common.base.Joiner;
import com.teradata.tempto.internal.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import static com.teradata.tempto.internal.initialization.RequirementsExpanderInterceptor.getMethodsCountFromContext;

public class ProgressLoggingListener
        implements ITestListener
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ProgressLoggingListener.class);

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
        testStartTime = System.currentTimeMillis();

        LOGGER.info("");
        started++;
        LOGGER.info("[{} of {}] {} (Groups: {})",
                started, getMethodsCountFromContext(testCase.getTestContext()), new TestInfo(testCase).getShortTestId(), Joiner.on(", ").join(testCase.getMethod().getGroups()));
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
        LOGGER.info("");
        LOGGER.info("Completed {} tests", started);
        LOGGER.info("{} SUCCEEDED      /      {} FAILED      /      {} SKIPPED", succeeded, failed, skipped);
    }
}
