/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.process;

import com.teradata.test.process.CliProcess;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class CliProcessBase
        implements CliProcess
{
    private static final Logger LOGGER = getLogger(CliProcessBase.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final Scanner output;
    private final PrintStream input;

    protected CliProcessBase(InputStream output, OutputStream input)
    {
        this.output = new Scanner(output);
        this.input = new PrintStream(input, true);
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
        String nextLine = output.nextLine();
        LOGGER.info("Next child process line: {}", nextLine);
        return nextLine;
    }

    @Override
    public String nextOutputToken()
    {
        String next = output.next();
        LOGGER.info("Next child process token: {}", next);
        return next;
    }

    @Override
    public boolean hasNextOutputLine()
    {
        return output.hasNextLine();
    }

    @Override
    public boolean hasNextOutput(Pattern pattern)
    {
        return output.hasNext(pattern);
    }

    @Override
    public boolean hasNextOutputToken()
    {
        return output.hasNext();
    }

    @Override
    public PrintStream getInput()
    {
        return input;
    }

    @Override
    public void waitForWithTimeoutAndKill()
            throws InterruptedException
    {
        waitForWithTimeoutAndKill(TIMEOUT);
    }
}
