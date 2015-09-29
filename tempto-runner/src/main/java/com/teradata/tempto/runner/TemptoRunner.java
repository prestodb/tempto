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

package com.teradata.tempto.runner;

import com.google.common.base.Joiner;
import com.teradata.tempto.internal.configuration.TestConfigurationFactory;
import com.teradata.tempto.internal.listeners.TestMetadataReader;
import com.teradata.tempto.internal.listeners.TestNameGroupNameMethodSelector;
import jdk.nashorn.internal.runtime.options.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;

public class TemptoRunner
{
    private static final Logger LOG = LoggerFactory.getLogger(TemptoRunner.class);
    private final TemptoRunnerCommandLineParser parser;
    private final TemptoRunnerOptions options;

    private TemptoRunner(TemptoRunnerCommandLineParser parser, TemptoRunnerOptions options) {

        this.parser = parser;
        this.options = options;
    }

    public static void runTempto(TemptoRunnerCommandLineParser parser, TemptoRunnerOptions options)
    {
        new TemptoRunner(parser, options).run();
    }

    private void run()
    {
        LOG.debug("running tempto with options: {}", options);
        if (options.helpRequested()) {
            parser.printHelpMessage();
            return;
        }

        XmlSuite testSuite = getXmlSuite();
        setupTestsConfiguration();
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(singletonList(testSuite));
        setupTestsFiltering(testNG);
        testNG.run();
    }

    private void setupTestsConfiguration()
    {
        System.setProperty(TestConfigurationFactory.TEST_CONFIGURATION_URI_KEY, options.getConfigFile());
        System.setProperty(TestConfigurationFactory.LOCAL_TEST_CONFIGURATION_URI_KEY, options.getConfigFileLocal());
    }

    private void setupTestsFiltering(TestNG testNG)
    {
        if (!options.getTestGroups().isEmpty()) {
            System.setProperty(TestNameGroupNameMethodSelector.TEST_GROUPS_TO_RUN_PROPERTY, Joiner.on(',').join(options.getTestGroups()));
        }
        if (!options.getExcludeGroups().isEmpty()) {
            System.setProperty(TestNameGroupNameMethodSelector.TEST_GROUPS_TO_EXCLUDE_PROPERTY, Joiner.on(',').join(options.getExcludeGroups()));
        }
        if (!options.getTests().isEmpty()) {
            System.setProperty(TestNameGroupNameMethodSelector.TEST_NAMES_TO_RUN_PROPERTY, Joiner.on(',').join(options.getTests()));
        }
        testNG.addMethodSelector("com.teradata.tempto.internal.listeners.TestNameGroupNameMethodSelector", 20);
    }

    private XmlSuite getXmlSuite()
    {
        // we cannot use singletonLists here as testNG later
        // modifies lists stored in XmlSuite ... zonk
        XmlSuite testSuite = new XmlSuite();
        testSuite.setName("tempto-tests");
        testSuite.setFileName("tempto-tests");
        XmlTest test = new XmlTest(testSuite);
        test.setName("all");
        XmlPackage testPackage = new XmlPackage(options.getTestsPackage());
        List<XmlPackage> testPackages = new ArrayList<>();
        testPackages.add(testPackage);
        test.setPackages(testPackages);
        XmlClass conventionBasedTestsClass = new XmlClass("com.teradata.tempto.internal.convention.ConventionBasedTestFactory");
        List<XmlClass> classes = new ArrayList<>();
        classes.add(conventionBasedTestsClass);
        test.setClasses(classes);
        return testSuite;
    }
}
