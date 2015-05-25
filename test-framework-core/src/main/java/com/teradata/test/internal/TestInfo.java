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
