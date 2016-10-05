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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.google.common.base.Preconditions.checkState;
import static com.teradata.tempto.internal.initialization.RequirementsExpanderInterceptor.getMethodsCountFromContext;
import static java.lang.System.currentTimeMillis;

public class ProgressLoggingListener
        implements ITestListener
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ProgressLoggingListener.class);

    private int started;
    private int succeeded;
    private int skipped;
    private int failed;
    private long startTime;
    private long testStartTime;

    private final TestMetadataReader testMetadataReader;

    public ProgressLoggingListener()
    {
        this.testMetadataReader = new TestMetadataReader();
    }

    @Override
    public void onStart(ITestContext context)
    {
        startTime = currentTimeMillis();
        LOGGER.info("Starting tests running");
    }

    @Override
    public void onTestStart(ITestResult testCase)
    {
        TestMetadata testMetadata = testMetadataReader.readTestMetadata(testCase);
        testStartTime = currentTimeMillis();
        started++;
        int total = getMethodsCountFromContext(testCase.getTestContext());
        String testGroups = Joiner.on(", ").join(testMetadata.testGroups);
        LOGGER.info("[{} of {}] {} (Groups: {})", started, total, testMetadata.testName, testGroups);
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
        logTestEnd("FAILURE");
        if (testCase.getThrowable() != null) {
            LOGGER.error("Failure cause:", testCase.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult testCase)
    {
        skipped++;
        LOGGER.info("SKIPPED");
    }

    private void logTestEnd(String outcome)
    {
        long executionTime = currentTimeMillis() - testStartTime;
        if (executionTime < 1000) {
            LOGGER.info(outcome);
        }
        else {
            BigDecimal durationSeconds = durationInSeconds(executionTime);
            LOGGER.info("{}     /    took {} seconds", outcome, durationSeconds);
        }
    }

    private static BigDecimal durationInSeconds(long millis)
    {
        return new BigDecimal(millis).divide(new BigDecimal(1000), 1, RoundingMode.HALF_UP);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testCase)
    {
    }

    @Override
    public void onFinish(ITestContext context)
    {
        checkState(succeeded + failed + skipped > 0, "No tests executed");
        LOGGER.info("");
        LOGGER.info("Completed {} tests", started);
        LOGGER.info("{} SUCCEEDED      /      {} FAILED      /      {} SKIPPED", succeeded, failed, skipped);
        BigDecimal durationSeconds = durationInSeconds(currentTimeMillis() - startTime);
        long minutes = durationSeconds.longValue() / 60;
        long restSeconds = durationSeconds.longValue() % 60;
        LOGGER.info("Tests execution took {} minutes and {} seconds", minutes, restSeconds);
    }
}
