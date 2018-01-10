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

package io.prestodb.tempto.fulfillment.command;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Commands which will be executed before the test suite.
 */
public class SuiteCommandRequirement
        extends CommandRequirement
{
    public static SuiteCommandRequirement suiteCommand(String command)
    {
        return new SuiteCommandRequirement(new Command(command));
    }

    public SuiteCommandRequirement(Command setupCommand)
    {
        this(singletonList(setupCommand));
    }

    public SuiteCommandRequirement(List<Command> setupCommands)
    {
        super(setupCommands);
    }

    @Override
    public String toString()
    {
        return "SuiteCommandRequirement{} " + super.toString();
    }
}
