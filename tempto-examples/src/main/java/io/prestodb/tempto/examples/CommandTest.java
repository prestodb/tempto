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

package io.prestodb.tempto.examples;

import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.Requirements;
import io.prestodb.tempto.RequirementsProvider;
import io.prestodb.tempto.configuration.Configuration;
import org.testng.annotations.Test;

import java.io.File;

import static io.prestodb.tempto.fulfillment.command.SuiteCommandRequirement.suiteCommand;
import static io.prestodb.tempto.fulfillment.command.TestCommandRequirement.testCommand;
import static org.assertj.core.api.Assertions.assertThat;

public class CommandTest
        implements RequirementsProvider
{
    @Override
    public Requirement getRequirements(Configuration configuration)
    {
        return Requirements.compose(
                testCommand("echo this is a test command output"),
                suiteCommand("echo this is a suite command output"),
                suiteCommand("touch commandTestFile"));
    }

    @Test(groups = "command")
    public void commandTest()
    {
        assertThat(new File("commandTestFile").exists()).isTrue();
    }

    @Test(groups = "command")
    public void configurationcommandTest()
    {
        assertThat(new File("configuratioCommandTestFile").exists()).isTrue();
    }
}
