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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.cli.Option;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class TemptoRunnerOptions
{
    public static final Option PACKAGE = Option.builder("p")
            .longOpt("package")
            .desc("Java package to be scanned for tests")
            .hasArg()
            .required()
            .build();

    public static final Option CONFIG_FILE = Option.builder()
            .longOpt("config")
            .desc("Path to main test configuration YAML file. If file does not exists in local file system then classpath is checked")
            .hasArg()
            .build();

    public static final Option CONFIG_FILE_LOCAL = Option.builder()
            .longOpt("config-local")
            .desc("Path to local test configuration YAML file. If file does not exists in local file system then classpath is checked")
            .hasArg()
            .build();

    public static final Option REPORT_DIR = Option.builder("r")
            .longOpt("report-dir")
            .desc("Test reports directory")
            .hasArg()
            .build();

    public static final Option GROUPS = Option.builder("g")
            .longOpt("groups")
            .desc("Test groups to be run")
            .valueSeparator(',')
            .hasArg()
            .build();

    public static final Option EXCLUDED_GROUPS = Option.builder("x")
            .longOpt("excluded-groups")
            .desc("Test groups to be excluded")
            .valueSeparator(',')
            .hasArg()
            .build();

    public static final Option TESTS = Option.builder("t")
            .longOpt("tests")
            .desc("Test name suffix to be included")
            .valueSeparator(',')
            .hasArg()
            .build();

    public static final Option HELP = Option.builder("h")
            .longOpt("help")
            .build();

    private final Map<String, String> values;

    public TemptoRunnerOptions(Map<String, String> values)
    {
        requireNonNull(values, "values is null");
        this.values = ImmutableMap.copyOf(values);
    }

    public String getReportDir()
    {
        return getValue(REPORT_DIR.getLongOpt()).get();
    }

    public String getTestsPackage()
    {
        return getValue(PACKAGE.getLongOpt()).get();
    }

    public String getConfigFile()
    {
        return getValue(CONFIG_FILE.getLongOpt()).get();
    }

    public String getConfigFileLocal()
    {
        return getValue(CONFIG_FILE_LOCAL.getLongOpt()).get();
    }

    public Set<String> getTestGroups()
    {
        return getValues(GROUPS.getLongOpt());
    }

    public Set<String> getExcludeGroups()
    {
        return getValues(EXCLUDED_GROUPS.getLongOpt());
    }

    public Set<String> getTests()
    {
        return getValues(TESTS.getLongOpt());
    }

    public boolean isHelpRequested()
    {
        return isSet(HELP);
    }

    public Set<String> getValues(String option)
    {
        return getValue(option).map(v -> ImmutableSet.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(v))).orElse(ImmutableSet.<String>of());
    }

    public Optional<String> getValue(String option)
    {
        return Optional.ofNullable(values.get(option));
    }

    public boolean isSet(Option option)
    {
        return getValue(option.getLongOpt()).map(v -> v.equals(TRUE.toString())).orElse(false);
    }

    @Override
    public String toString()
    {
        return reflectionToString(this);
    }
}
