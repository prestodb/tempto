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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.prestodb.tempto.internal.configuration.TestConfigurationFactory;
import io.prestodb.tempto.internal.convention.ConventionTestsUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.CONFIG_FILES;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.CONVENTION_TESTS_DIR;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.DUMP_CONVENTION_RESULTS;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.EXCLUDED_GROUPS;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.GROUPS;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.HELP;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.PACKAGE;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.REPORT_DIR;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.TESTS;
import static io.prestodb.tempto.runner.TemptoRunnerOptions.THREAD_COUNT;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public class TemptoRunnerCommandLineParser
{
    public static class ParsingException
            extends RuntimeException
    {
        public ParsingException(String message)
        {
            super(message);
        }

        public ParsingException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    private static final Comparator<Option> OPTION_BY_LONG_OPT_COMPARATOR = Comparator.comparing(Option::getLongOpt);

    private final String appName;
    private final List<Option> options;
    private final Map<String, DefaultValue> defaults;

    private TemptoRunnerCommandLineParser(String appName, List<Option> options, Map<String, DefaultValue> defaults)
    {
        requireNonNull(appName, "appName is null");
        requireNonNull(options, "options is null");
        requireNonNull(defaults, "defaults is null");

        this.appName = appName;
        this.options = ImmutableList.copyOf(options);
        this.defaults = ImmutableMap.copyOf(defaults);
    }

    public TemptoRunnerOptions parseCommandLine(String[] argv)
    {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, argv);
            return commandLineToOptions(commandLine);
        }
        catch (ParseException e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    public void printHelpMessage()
    {
        printHelpMessage(new PrintWriter(System.err));
    }

    public void printHelpMessage(PrintWriter printWriter)
    {
        Options options = buildOptions();
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(OPTION_BY_LONG_OPT_COMPARATOR);
        helpFormatter.printHelp(printWriter,
                120,
                appName,
                null,
                options,
                HelpFormatter.DEFAULT_LEFT_PAD,
                HelpFormatter.DEFAULT_DESC_PAD,
                null);
        printWriter.flush();
    }

    private Options buildOptions()
    {
        Options options = new Options();
        for (Option option : this.options) {
            if (isChangable(option.getLongOpt())) {
                options.addOption(option);
            }
        }
        return options;
    }

    private boolean isChangable(String option)
    {
        return !defaults.containsKey(option) || defaults.get(option).isChangeable();
    }

    private TemptoRunnerOptions commandLineToOptions(CommandLine commandLine)
    {
        ImmutableMap.Builder<String, String> values = ImmutableMap.<String, String>builder();
        if (!commandLine.getArgList().isEmpty()) {
            throw new ParsingException("Extra unsupported parameters: " + commandLine.getArgList());
        }
        for (Option option : options) {
            Optional<String> value = getOptionValue(commandLine, option);
            if (value.isPresent()) {
                values.put(option.getLongOpt(), value.get());
            }
        }
        return new TemptoRunnerOptions(values.build());
    }

    private Optional<String> getOptionValue(CommandLine commandLine, Option option)
    {
        String longOpt = option.getLongOpt();
        if (defaults.containsKey(longOpt)) {
            return Optional.of(commandLine.getOptionValue(longOpt, defaults.get(longOpt).getValue()));
        }
        Optional<String> value = Optional.ofNullable(commandLine.getOptionValue(longOpt));
        if (!value.isPresent() && commandLine.hasOption(longOpt)) {
            return Optional.of(TRUE.toString());
        }
        return value;
    }

    public static Builder builder(String appName)
    {
        return new Builder(appName);
    }

    public static class Builder
    {
        private final String appName;
        private final List<Option> options = new ArrayList<>();
        private final Map<String, DefaultValue> defaults = new HashMap<>();

        private Builder(String appName)
        {
            this.appName = appName;
            addOption(EXCLUDED_GROUPS);
            addOption(GROUPS);
            addOption(PACKAGE);
            addOption(REPORT_DIR);
            addOption(TESTS);
            addOption(HELP);
            addOption(DUMP_CONVENTION_RESULTS);
            addOption(THREAD_COUNT);
            setReportDir("./test-reports", true);
            setConfigFile(TestConfigurationFactory.DEFAULT_TEST_CONFIGURATION_LOCATION, true);
            setConventionTestDirectory(ConventionTestsUtils.DEFAULT_CONVENTION_TESTS_DIR, true);
        }

        public Builder addOption(Option option)
        {
            checkArgument(!contains(option), "Options %s is already added", option.getLongOpt());
            options.add(option);
            return this;
        }

        private boolean contains(Option option)
        {
            return options.stream().anyMatch(o -> o.getLongOpt().equals(option.getLongOpt()));
        }

        public Builder setTestsPackage(String testsPackage, boolean changeable)
        {
            return setDefaultValue(PACKAGE, testsPackage, changeable);
        }

        public Builder setConfigFile(String configFile, boolean changeable)
        {
            return setConfigFiles(singletonList(configFile), changeable);
        }

        public Builder setConfigFiles(List<String> configFiles, boolean changeable)
        {
            return setDefaultValue(CONFIG_FILES, Joiner.on(",").join(configFiles), changeable);
        }

        public Builder setReportDir(String reportDir, boolean changeable)
        {
            return setDefaultValue(REPORT_DIR, reportDir, changeable);
        }

        private Builder setConventionTestDirectory(String conventionTestsDir, boolean changeable)
        {
            return setDefaultValue(CONVENTION_TESTS_DIR, conventionTestsDir, changeable);
        }

        public Builder setExcludedGroups(String excludedGroups, boolean changeable)
        {
            return setDefaultValue(EXCLUDED_GROUPS, excludedGroups, changeable);
        }

        public Builder setGroups(String groups, boolean changeable)
        {
            return setDefaultValue(GROUPS, groups, changeable);
        }

        public Builder setDefaultValue(Option option, Object value)
        {
            return setDefaultValue(option, value.toString(), true);
        }

        private Builder setDefaultValue(Option option, String value, boolean changeable)
        {
            if (!contains(option)) {
                addOption(option);
            }
            defaults.put(option.getLongOpt(), new DefaultValue(value.toString(), changeable));
            return this;
        }

        public TemptoRunnerCommandLineParser build()
        {
            return new TemptoRunnerCommandLineParser(appName, options, defaults);
        }
    }

    private static class DefaultValue
    {
        private final String value;
        private final boolean changeable;

        private DefaultValue(String value, boolean changeable)
        {
            this.value = value;
            this.changeable = changeable;
        }

        public String getValue()
        {
            return value;
        }

        public boolean isChangeable()
        {
            return changeable;
        }
    }
}
