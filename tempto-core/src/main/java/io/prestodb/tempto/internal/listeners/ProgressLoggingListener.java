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

package io.prestodb.tempto.internal.listeners;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.google.common.base.Preconditions.checkState;
import static io.prestodb.tempto.internal.initialization.RequirementsExpanderInterceptor.getMethodsCountFromContext;
import static java.lang.String.format;
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
        testStartTime = currentTimeMillis();
        started++;
        int total = getMethodsCountFromContext(testCase.getTestContext());
        LOGGER.info("[{} of {}] {}", started, total, formatTestName(testCase));
    }

    @Override
    public void onTestSuccess(ITestResult testCase)
    {
        succeeded++;
        logTestEnd(testCase, "SUCCESS");
    }

    @Override
    public void onTestFailure(ITestResult testCase)
    {
        failed++;
        logTestEnd(testCase, "FAILURE");
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

    private void logTestEnd(ITestResult testCase, String outcome)
    {
        long executionTime = currentTimeMillis() - testStartTime;
        if (executionTime < 1000) {
            LOGGER.info(outcome);
        }
        else {
            LOGGER.info("{}     /    {} took {}", outcome, formatTestName(testCase), formatDuration(executionTime));
        }
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
        LOGGER.info("Tests execution took {}", formatDuration(currentTimeMillis() - startTime));
    }

    private String formatTestName(ITestResult testCase)
    {
        TestMetadata testMetadata = testMetadataReader.readTestMetadata(testCase);
        String testGroups = Joiner.on(", ").join(testMetadata.testGroups);
        return format("%s (Groups: %s)", testMetadata.testName, testGroups);
    }

    private static String formatDuration(long durationInMillis)
    {
        BigDecimal durationSeconds = durationInSeconds(durationInMillis);
        if (durationSeconds.longValue() > 60) {
            long minutes = durationSeconds.longValue() / 60;
            long restSeconds = durationSeconds.longValue() % 60;
            return String.format("%d minutes and %d seconds", minutes, restSeconds);
        }
        else {
            return String.format("%s seconds", durationSeconds);
        }
    }

    private static BigDecimal durationInSeconds(long millis)
    {
        return new BigDecimal(millis).divide(new BigDecimal(1000), 1, RoundingMode.HALF_UP);
    }
}
