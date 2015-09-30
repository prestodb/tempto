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
import com.google.common.collect.ImmutableSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;

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

    private static final String CLASS_PATH_OPTION = "classpath";
    private static final String PACKAGE_OPTION = "package";

    private static final String CONFIG_FILE_OPTION = "config";
    private static final String CONFIG_FILE_DEFAULT = "classpath:/tempto-configuration.yaml";

    private static final String CONFIG_FILE_LOCAL_OPTION = "config-local";
    private static final String CONFIG_FILE_LOCAL_DEFAULT = "classpath:/tempto-configuration-local.yaml";

    private static final String REPORT_DIR_OPTION = "report-dir";
    private static final String REPORT_DIR_DEFAULT = "./test-reports";

    private static final String GROUPS_OPTION = "groups";
    private static final String EXCLUDED_GROUPS_OPTION = "excluded-groups";
    private static final String TESTS_OPTION = "tests";

    private static final String HELP_OPTION = "help";
    private static final Comparator<Option> ALL_EQUAL_OPTION_COMPARATOR = (a, b) -> 0;

    private final String appName;
    private final Map<String, DefaultValue> defaultsMap;

    private static final Function<String, Set<String>> SPLIT_ON_COMMA = s -> ImmutableSet.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(s));

    private TemptoRunnerCommandLineParser(String appName, Map<String, DefaultValue> defaultsMap)
    {
        this.appName = appName;
        this.defaultsMap = defaultsMap;
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
        helpFormatter.setOptionComparator(ALL_EQUAL_OPTION_COMPARATOR);
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
        if (isChangable(PACKAGE_OPTION)) {
            options.addOption(Option.builder("p")
                    .longOpt(PACKAGE_OPTION)
                    .hasArg()
                    .required()
                    .desc("Java package to be scanned for tests").build());
        }
        if (isChangable(CONFIG_FILE_OPTION)) {
            options.addOption(Option.builder()
                    .longOpt(CONFIG_FILE_OPTION)
                    .hasArg()
                    .desc("URI to Test main configuration YAML file. If lacks uri schema defaults to file:. If file is not found defaults to classpath:.")
                    .build());
        }
        if (isChangable(CONFIG_FILE_LOCAL_OPTION)) {
            options.addOption(Option.builder()
                    .longOpt(CONFIG_FILE_LOCAL_OPTION)
                    .hasArg()
                    .desc("URI to Test local configuration YAML file. If lacks uri schema defaults to file:. If file is not found defaults to classpath:.")
                    .build());
        }
        if (isChangable(REPORT_DIR_OPTION)) {
            options.addOption(Option.builder()
                    .longOpt(REPORT_DIR_OPTION)
                    .hasArg()
                    .desc("Test reports directory")
                    .build());
        }
        if (isChangable(GROUPS_OPTION)) {
            options.addOption(Option.builder()
                    .longOpt(GROUPS_OPTION)
                    .hasArg()
                    .desc("Test groups to be run")
                    .build());
        }
        if (isChangable(EXCLUDED_GROUPS_OPTION)) {
            options.addOption(Option.builder()
                    .longOpt(EXCLUDED_GROUPS_OPTION)
                    .hasArg()
                    .desc("Test groups to be excluded")
                    .build());
        }
        if (isChangable(TESTS_OPTION)) {
            options.addOption(Option.builder()
                    .longOpt(TESTS_OPTION)
                    .hasArg()
                    .desc("Test patterns to be included (not yet supported)")
                    .build());
        }
        options.addOption("h", HELP_OPTION, false, "Shows help message");
        return options;
    }

    private boolean isChangable(String option)
    {
        return !defaultsMap.containsKey(option) || defaultsMap.get(option).isChangeable();
    }

    private TemptoRunnerOptions commandLineToOptions(CommandLine commandLine)
    {
        String testsPackage = getOptionValue(commandLine, PACKAGE_OPTION).get();
        String configFile = getOptionValue(commandLine, CONFIG_FILE_OPTION).get();
        String configFileLocal = getOptionValue(commandLine, CONFIG_FILE_LOCAL_OPTION).get();
        String reportDir = getOptionValue(commandLine, REPORT_DIR_OPTION).get();

        Set<String> testGroups = getOptionValue(commandLine, GROUPS_OPTION).map(SPLIT_ON_COMMA).orElse(emptySet());
        Set<String> excludedGroups = getOptionValue(commandLine, EXCLUDED_GROUPS_OPTION).map(SPLIT_ON_COMMA).orElse(emptySet());
        Set<String> tests = getOptionValue(commandLine, TESTS_OPTION).map(SPLIT_ON_COMMA).orElse(emptySet());
        boolean helpRequested = commandLine.hasOption(HELP_OPTION);

        return TemptoRunnerOptions.builder()
                .setTestsPackage(testsPackage)
                .setConfigFile(configFile)
                .setConfigFileLocal(configFileLocal)
                .setReportDir(reportDir)
                .setTestGroups(testGroups)
                .setExcludeGroups(excludedGroups)
                .setTests(tests)
                .setHelpRequested(helpRequested)
                .build();
    }

    private Optional<String> getOptionValue(CommandLine commandLine, String option)
    {
        if (defaultsMap.containsKey(option)) {
            return Optional.of(commandLine.getOptionValue(option, defaultsMap.get(option).getValue()));
        }
        else {
            return Optional.ofNullable(commandLine.getOptionValue(option));
        }
    }

    public static Builder builder(String appName)
    {
        return new Builder(appName);
    }

    public static class Builder
    {
        private final String appName;
        private Map<String, DefaultValue> defaultsMap = createInitialDefaultsMap();

        private Builder(String appName)
        {
            this.appName = appName;
        }

        private static Map<String, DefaultValue> createInitialDefaultsMap()
        {
            Map<String, DefaultValue> defaultsMap = new HashMap<>();
            defaultsMap.put(REPORT_DIR_OPTION, new DefaultValue(REPORT_DIR_DEFAULT, true));
            defaultsMap.put(CONFIG_FILE_OPTION, new DefaultValue(CONFIG_FILE_DEFAULT, true));
            defaultsMap.put(CONFIG_FILE_LOCAL_OPTION, new DefaultValue(CONFIG_FILE_LOCAL_DEFAULT, true));
            return defaultsMap;
        }

        public Builder setClassPath(String classpath, boolean changable)
        {
            return setDefaultValue(CLASS_PATH_OPTION, classpath, changable);
        }

        public Builder setLocalClassPath(boolean changeable)
        {
            return setDefaultValue(CLASS_PATH_OPTION, null, changeable);
        }

        public Builder setTestsPackage(String testsPackage, boolean changeable)
        {
            return setDefaultValue(PACKAGE_OPTION, testsPackage, changeable);
        }

        public Builder setConfigFile(String configFile, boolean changeable)
        {
            return setDefaultValue(CONFIG_FILE_OPTION, configFile, changeable);
        }

        public Builder setReportDir(String reportDir, boolean changeable)
        {
            return setDefaultValue(REPORT_DIR_OPTION, reportDir, changeable);
        }

        private Builder setDefaultValue(String option, String value, boolean changable)
        {
            defaultsMap.put(option, new DefaultValue(value, changable));
            return this;
        }

        public TemptoRunnerCommandLineParser build()
        {
            return new TemptoRunnerCommandLineParser(appName, defaultsMap);
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
