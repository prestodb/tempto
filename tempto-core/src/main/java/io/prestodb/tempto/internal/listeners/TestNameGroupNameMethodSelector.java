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

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.indexOf;
import static com.google.common.collect.Sets.intersection;
import static java.lang.System.getProperty;
import static java.util.Optional.ofNullable;

/**
 * We want to be able to select test methods to be run on per testName/testGroup basis.
 * We cannot use standard testNG methods to select based on group/methodName for that though.
 * We generate some tests from factories and we want those generated tests to
 * belong to different test groups. But we cannot assign testNG groups to them as those can only be defined
 * in static way through annotations.
 * <p>
 * So we introduce our own mechanism of tests selection.
 * This is governed by following java system properties:
 * <ul>
 * <li>io.prestodb.tempto.groups - should contain comma separated list of groups from which tests should be run
 * <li>io.prestodb.tempto.exclude_groups - should contain comma separated list of groups from which tests should be excluded
 * <li>io.prestodb.tempto.tests - should contain comma separated list of test names to be run
 * </ul>
 * <p>
 * <p>
 * TestName matching is done by verifying if value from system property is suffix of actual test name in question
 */
public class TestNameGroupNameMethodSelector
        implements IMethodSelector
{
    public static final String TEST_NAMES_TO_RUN_PROPERTY = "io.prestodb.tempto.tests";
    public static final String TEST_GROUPS_TO_RUN_PROPERTY = "io.prestodb.tempto.groups";
    public static final String TEST_GROUPS_TO_EXCLUDE_PROPERTY = "io.prestodb.tempto.exclude_groups";

    private final Optional<Set<String>> testNamesToRun;
    private final Optional<Set<String>> testGroupsToRun;
    private final Optional<Set<String>> testGroupsToExclude;

    private static final Splitter LIST_SYSTEM_PROPERTY_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private final TestMetadataReader testMetadataReader;

    public TestNameGroupNameMethodSelector()
    {
        this(getListSystemProperty(TEST_NAMES_TO_RUN_PROPERTY),
                getListSystemProperty(TEST_GROUPS_TO_RUN_PROPERTY),
                getListSystemProperty(TEST_GROUPS_TO_EXCLUDE_PROPERTY),
                new TestMetadataReader());
    }

    public TestNameGroupNameMethodSelector(Optional<Set<String>> testNamesToRun, Optional<Set<String>> testGroupsToRun, Optional<Set<String>> testGroupsToExclude, TestMetadataReader testMetadataReader)
    {
        this.testNamesToRun = testNamesToRun;
        this.testGroupsToRun = testGroupsToRun;
        this.testGroupsToExclude = testGroupsToExclude;
        this.testMetadataReader = testMetadataReader;
    }

    @Override
    public boolean includeMethod(IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod)
    {
        TestMetadata testMetadata = testMetadataReader.readTestMetadata(method);
        return includeBasedOnTestName(testMetadata) &&
                includeBasedOnGroups(testMetadata) &&
                !excludeBasedOnGroups(testMetadata);
    }

    private boolean includeBasedOnTestName(TestMetadata testMetadata)
    {
        if (!testNamesToRun.isPresent()) {
            return true;
        }

        return indexOf(testNamesToRun.get(), testName -> testMetadata.testName.contains(testName)) != -1;
    }

    private boolean includeBasedOnGroups(TestMetadata testMetadata)
    {
        if (!testGroupsToRun.isPresent()) {
            return true;
        }
        return !intersection(testMetadata.testGroups, testGroupsToRun.get()).isEmpty();
    }

    private boolean excludeBasedOnGroups(TestMetadata testMetadata)
    {
        if (!testGroupsToExclude.isPresent()) {
            return false;
        }
        return !intersection(testMetadata.testGroups, testGroupsToExclude.get()).isEmpty();
    }

    @Override
    public void setTestMethods(List<ITestNGMethod> testMethods)
    {
    }

    private static Optional<Set<String>> getListSystemProperty(String testNamesToRunProperty)
    {
        String property = getProperty(testNamesToRunProperty);
        return ofNullable(property)
                .map(LIST_SYSTEM_PROPERTY_SPLITTER::split)
                .map(Sets::newHashSet);
    }
}
