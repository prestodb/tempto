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

package io.prestodb.tempto.internal.fulfillment.command;

import com.google.common.base.Splitter;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.context.State;
import io.prestodb.tempto.fulfillment.RequirementFulfiller;
import io.prestodb.tempto.fulfillment.TestStatus;
import io.prestodb.tempto.fulfillment.command.Command;
import io.prestodb.tempto.fulfillment.command.CommandRequirement;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class CommandFulfiller<T extends CommandRequirement>
        implements RequirementFulfiller
{
    private static final Logger LOGGER = getLogger(CommandFulfiller.class);

    private final Class<T> requirementClass;

    CommandFulfiller(Class<T> requirementClass)
    {
        this.requirementClass = requirementClass;
    }

    @Override
    public final Set<State> fulfill(Set<Requirement> requirements)
    {
        requirements.stream()
                .filter(requirement -> requirement.getClass().isAssignableFrom(requirementClass))
                .map(requirementClass::cast)
                .distinct()
                .forEach(this::fulfill);

        return emptySet();
    }

    private void fulfill(T requirement)
    {
        requirement.getSetupCommands().stream()
                .map(Command::getCommand)
                .forEach(CommandFulfiller::execute);
    }

    private static void execute(String command)
    {
        try {
            LOGGER.info("Executing command: " + command);
            File devNull = new File("/dev/null");
            int exitStatus = new ProcessBuilder(Splitter.on(" ").splitToList(command))
                    .redirectError(devNull)
                    .redirectOutput(devNull)
                    .redirectInput(devNull)
                    .start()
                    .waitFor();
            if (exitStatus != 0) {
                throw new RuntimeException(String.format("Command '%s' exited with no zero status '%d'", command, exitStatus));
            }
        }
        catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void cleanup(TestStatus status)
    {
        // nothing to do here
    }
}
