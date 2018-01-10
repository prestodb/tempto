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

import io.prestodb.tempto.Requirement;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class CommandRequirement
        implements Requirement
{
    private final List<Command> setupCommands;

    public CommandRequirement(List<Command> setupCommands)
    {
        this.setupCommands = requireNonNull(setupCommands, "setupCommands is null");
    }

    public List<Command> getSetupCommands()
    {
        return setupCommands;
    }

    @Override
    public String toString()
    {
        return "CommandRequirement{" +
                "setupCommands=" + setupCommands +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        CommandRequirement that = (CommandRequirement) o;
        return Objects.equals(setupCommands, that.setupCommands);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(setupCommands);
    }
}
