/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.process;

import com.teradata.test.process.CliProcess;
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
        LOGGER.info("processOutput: {}", nextLine);
        return nextLine;
    }

    @Override
    public String nextOutputToken()
    {
        String next = processOutput.next();
        LOGGER.info("processOutput: {}", next);
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
        LOGGER.info("processError: {}", nextLine);
        return nextLine;
    }

    @Override
    public String nextErrorToken()
    {
        String next = processError.next();
        LOGGER.info("processError: {}", next);
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
