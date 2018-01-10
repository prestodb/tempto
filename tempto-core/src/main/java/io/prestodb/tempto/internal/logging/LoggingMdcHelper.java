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

package io.prestodb.tempto.internal.logging;

import io.prestodb.tempto.internal.listeners.TestMetadata;
import io.prestodb.tempto.internal.listeners.TestMetadataReader;
import org.testng.ITestResult;

public class LoggingMdcHelper
{
    private static final String MDC_TEST_ID_KEY = "test_id";
    private static final TestMetadataReader testMetadataReader = new TestMetadataReader();

    private LoggingMdcHelper() {}

    public static void setupLoggingMdcForTest(ITestResult testCase)
    {
        TestMetadata testMetadata = testMetadataReader.readTestMetadata(testCase);
        String testId = testMetadata.testName;
        org.slf4j.MDC.put("test_id", testId);
    }

    public static void cleanLoggingMdc()
    {
        org.slf4j.MDC.remove(MDC_TEST_ID_KEY);
    }
}
