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

import com.teradata.tempto.internal.configuration.TestConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;

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
        System.setProperty(TestConfigurationFactory.TEST_CONFIGURATION_URI_KEY, options.getConfigFile());
        System.setProperty(TestConfigurationFactory.LOCAL_TEST_CONFIGURATION_URI_KEY, options.getConfigFileLocal());
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(singletonList(testSuite));
        testNG.run();
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
        if (!options.getTestGroups().isEmpty()) {
            test.setIncludedGroups(new ArrayList<>(options.getTestGroups()));
        }
        if (!options.getExcludeGroups().isEmpty()) {
            test.setExcludedGroups(new ArrayList<>(options.getExcludeGroups()));
        }

        XmlClass conventionBasedTestsClass = new XmlClass("com.teradata.tempto.internal.convention.ConventionBasedTestFactory");
        List<XmlClass> classes = new ArrayList<>();
        classes.add(conventionBasedTestsClass);
        test.setClasses(classes);
        return testSuite;
    }
}
