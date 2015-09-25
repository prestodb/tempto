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

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.copyOf;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class TemptoRunnerOptions
{
    private final String testsPackage;
    private final String configFile;
    private final String configFileLocal;
    private final String reportDir;
    private final Set<String> testGroups;
    private final Set<String> excludeGroups;
    private final Set<String> tests;
    private final boolean helpRequested;

    private TemptoRunnerOptions(
            String testsPackage,
            String configFile,
            String configFileLocal,
            String reportDir,
            Set<String> testGroups,
            Set<String> excludeGroups,
            Set<String> tests,
            boolean helpRequested)
    {
        this.testGroups = copyOf(checkNotNull(testGroups, "testGroups can not be null"));
        this.excludeGroups = copyOf(checkNotNull(excludeGroups, "excludeGroups can not be null"));
        this.tests = copyOf(checkNotNull(tests, "tests can not be null"));
        this.helpRequested = helpRequested;
        this.testsPackage = checkNotNull(testsPackage);
        this.configFile = checkNotNull(configFile, "configFile can not be null");
        this.configFileLocal = checkNotNull(configFileLocal, "configFileLocal can not be null");
        this.reportDir = reportDir;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public String getReportDir()
    {
        return reportDir;
    }

    public String getTestsPackage()
    {
        return testsPackage;
    }

    public String getConfigFile()
    {
        return configFile;
    }

    public String getConfigFileLocal()
    {
        return configFileLocal;
    }

    public Set<String> getTestGroups()
    {
        return testGroups;
    }

    public Set<String> getExcludeGroups()
    {
        return excludeGroups;
    }


    public Set<String> getTests()
    {
        return tests;
    }

    public boolean isHelpRequested()
    {
        return helpRequested;
    }

    public boolean helpRequested()
    {
        return helpRequested;
    }

    @Override
    public String toString()
    {
        return reflectionToString(this);
    }

    public static class Builder
    {
        private String testsPackage;
        private String configFile;
        private String configFileLocal;
        private String reportDir;
        private Set<String> testGroups;
        private Set<String> excludeGroups;
        private Set<String> tests;

        private boolean helpRequested;

        private Builder() {}

        public Builder setTestsPackage(String testsPackage)
        {
            this.testsPackage = testsPackage;
            return this;
        }

        public Builder setConfigFile(String configFile)
        {
            this.configFile = configFile;
            return this;
        }

        public Builder setConfigFileLocal(String configFileLocal)
        {
            this.configFileLocal = configFileLocal;
            return this;
        }

        public Builder setReportDir(String reportDir)
        {
            this.reportDir = reportDir;
            return this;
        }

        public Builder setTestGroups(Set<String> testGroups)
        {
            this.testGroups = testGroups;
            return this;
        }

        public Builder setExcludeGroups(Set<String> excludeGroups)
        {
            this.excludeGroups = excludeGroups;
            return this;
        }


        public Builder setTests(Set<String> tests)
        {
            this.tests = tests;
            return this;
        }


        public Builder setHelpRequested(boolean helpRequested)
        {
            this.helpRequested = helpRequested;
            return this;
        }

        public TemptoRunnerOptions build()
        {
            return new TemptoRunnerOptions(testsPackage, configFile, configFileLocal, reportDir, testGroups,
                    excludeGroups, tests, helpRequested);
        }
    }
}
