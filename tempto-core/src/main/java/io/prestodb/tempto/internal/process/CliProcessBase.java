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
package io.prestodb.tempto.internal.process;

import io.prestodb.tempto.process.CliProcess;
import org.slf4j.Logger;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.util.Closeables.closeQuietly;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class CliProcessBase
        implements CliProcess
{
    private static final Logger LOGGER = getLogger(CliProcessBase.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final Scanner processOutput;
    private final Scanner processError;
    private final PrintStream processInput;

    protected CliProcessBase(InputStream processOutput, InputStream processError, OutputStream processInput)
    {
        this.processOutput = new Scanner(processOutput);
        this.processError = new Scanner(processError);
        this.processInput = new PrintStream(processInput, true);
    }

    @Override
    public List<String> readRemainingOutputLines()
    {
        List<String> lines = newArrayList();
        while (hasNextOutputLine()) {
            lines.add(nextOutputLine());
        }
        return lines;
    }

    @Override
    public String nextOutputLine()
    {
        String nextLine = processOutput.nextLine();
        LOGGER.debug("processOutput: {}", nextLine);
        return nextLine;
    }

    @Override
    public String nextOutputToken()
    {
        String next = processOutput.next();
        LOGGER.debug("processOutput: {}", next);
        return next;
    }

    @Override
    public boolean hasNextOutputLine()
    {
        return processOutput.hasNextLine();
    }

    @Override
    public boolean hasNextOutput(Pattern pattern)
    {
        return processOutput.hasNext(pattern);
    }

    @Override
    public boolean hasNextOutputToken()
    {
        return processOutput.hasNext();
    }

    @Override
    public List<String> readRemainingErrorLines()
    {
        List<String> lines = newArrayList();
        while (hasNextErrorLine()) {
            lines.add(nextErrorLine());
        }
        return lines;
    }

    @Override
    public String nextErrorLine()
    {
        String nextLine = processError.nextLine();
        LOGGER.debug("processError: {}", nextLine);
        return nextLine;
    }

    @Override
    public String nextErrorToken()
    {
        String next = processError.next();
        LOGGER.debug("processError: {}", next);
        return next;
    }

    @Override
    public boolean hasNextErrorLine()
    {
        return processError.hasNextLine();
    }

    @Override
    public boolean hasNextError(Pattern pattern)
    {
        return processError.hasNext(pattern);
    }

    @Override
    public boolean hasNextErrorToken()
    {
        return processError.hasNext();
    }

    @Override
    public PrintStream getProcessInput()
    {
        return processInput;
    }

    @Override
    public void waitForWithTimeoutAndKill()
            throws InterruptedException
    {
        waitForWithTimeoutAndKill(TIMEOUT);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close()
    {
        closeQuietly(processOutput, processError, processInput);
    }
}
