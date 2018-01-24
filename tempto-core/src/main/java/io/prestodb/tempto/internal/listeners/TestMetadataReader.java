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

import io.prestodb.tempto.testmarkers.WithName;
import io.prestodb.tempto.testmarkers.WithTestGroups;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

public class TestMetadataReader
{
    public TestMetadata readTestMetadata(ITestResult testResult)
    {
        return readTestMetadata(testResult.getMethod());
    }

    public TestMetadata readTestMetadata(ITestNGMethod testMethod)
    {
        return new TestMetadata(
                readTestGroups(testMethod),
                readTestName(testMethod));
    }

    private Set<String> readTestGroups(ITestNGMethod method)
    {
        if (method.isTest() && method.getInstance() instanceof WithTestGroups) {
            return (((WithTestGroups) method.getInstance()).getTestGroups());
        }
        return newHashSet(asList(method.getGroups()));
    }

    private String readTestName(ITestNGMethod method)
    {
        if (method.isTest() && method.getInstance() instanceof WithName) {
            return ((WithName) method.getInstance()).getTestName();
        }
        return method.getTestClass().getName() + "." + method.getMethodName();
    }
}
