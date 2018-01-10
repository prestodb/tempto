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

package io.prestodb.tempto.runner;

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

    public static final Option CONFIG_FILES = Option.builder("f")
            .longOpt("config")
            .desc("Path to test configuration YAML files. If file does not exists in local file system then classpath is checked")
            .valueSeparator(',')
            .hasArg()
            .build();

    public static final Option REPORT_DIR = Option.builder("r")
            .longOpt("report-dir")
            .desc("Test reports directory")
            .hasArg()
            .build();

    public static final Option CONVENTION_TESTS_DIR = Option.builder("c")
            .longOpt("convention-test-dir")
            .desc("Convention test directory. If not found in local file system then classpath is checked.")
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

    public static final Option DUMP_CONVENTION_RESULTS = Option.builder()
            .longOpt("dump-convention-results")
            .desc("Dump results of convention based test queries to directory.")
            .hasArg()
            .build();

    public static final Option THREAD_COUNT = Option.builder()
            .longOpt("thread-count")
            .desc("Number of threads which will execute tests.")
            .hasArg()
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

    public String getConfigFiles()
    {
        return getValue(CONFIG_FILES.getLongOpt()).get();
    }

    public Set<String> getTestGroups()
    {
        return getValues(GROUPS.getLongOpt());
    }

    public String getConventionTestsDirectory()
    {
        return getValue(CONVENTION_TESTS_DIR.getLongOpt()).get();
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

    public Optional<String> getConventionResultsDumpPath()
    {
        return getValue(DUMP_CONVENTION_RESULTS.getLongOpt());
    }

    public int getThreadCount()
    {
        return Integer.parseInt(getValue(THREAD_COUNT.getLongOpt()).orElse("1"));
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
