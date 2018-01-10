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
package io.prestodb.tempto.process;

import java.io.Closeable;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * Interface for interacting with CLI processes.
 */
public interface CliProcess
        extends Closeable
{
    public static List<String> trimLines(List<String> lines)
    {
        return lines.stream().map(String::trim).collect(toList());
    }

    List<String> readRemainingOutputLines();

    String nextOutputLine();

    String nextOutputToken();

    boolean hasNextOutputLine();

    boolean hasNextOutput(Pattern pattern);

    boolean hasNextOutputToken();

    List<String> readRemainingErrorLines();

    String nextErrorLine();

    String nextErrorToken();

    boolean hasNextErrorLine();

    boolean hasNextError(Pattern pattern);

    boolean hasNextErrorToken();

    PrintStream getProcessInput();

    /**
     * Waits for a process to finish and ensures it returns with 0 status. If the process
     * fails to finish within given timeout it is killed and {@link TimeoutRuntimeException} is thrown.
     *
     * @throws InterruptedException if the thread is interrupted
     * @throws CommandExecutionException if command finishes with non zero status
     * @throws TimeoutRuntimeException
     */
    void waitForWithTimeoutAndKill()
            throws InterruptedException, TimeoutRuntimeException, CommandExecutionException;

    void waitForWithTimeoutAndKill(Duration timeout)
            throws InterruptedException, TimeoutRuntimeException, CommandExecutionException;
}
