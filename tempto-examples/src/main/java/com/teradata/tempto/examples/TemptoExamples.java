/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tempto.examples;

import com.teradata.tempto.runner.TemptoRunner;
import com.teradata.tempto.runner.TemptoRunnerCommandLineParser;
import com.teradata.tempto.runner.TemptoRunnerOptions;
import sun.security.pkcs.ParsingException;

public class TemptoExamples
{

    public static void main(String[] args)
    {
        TemptoRunnerCommandLineParser parser = TemptoRunnerCommandLineParser
                .builder("tempto examples")
                .setTestsPackage("com.teradata.tempto.examples", false)
                .setConfigFile("classpath:/tempto-configuration.yaml", false)
                .build();
        TemptoRunnerOptions temptoRunnerOptions;
        try {
            temptoRunnerOptions = parser.parseCommandLine(args);
            TemptoRunner.runTempto(parser, temptoRunnerOptions);
        }
        catch (TemptoRunnerCommandLineParser.ParsingException e) {
            System.err.println("Could not parse command line. " + e.getMessage());
            System.err.println();
            parser.printHelpMessage();
            System.exit(1);
        }
    }
}
