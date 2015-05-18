/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.process;

import com.teradata.test.internal.process.CliProcessBase;

import java.io.IOException;
import java.time.Duration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Wrapping class for {@link Process} that helps interacting with CLI processes.
 */
public class LocalCliProcess
        extends CliProcessBase
{
    private final Process process;

    public LocalCliProcess(Process process)
    {
        super(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
        this.process = process;
    }

    @Override
    public void waitForWithTimeoutAndKill(Duration timeout)
            throws InterruptedException
    {
        if (!process.waitFor(timeout.toMillis(), MILLISECONDS)) {
            close();
            throw new RuntimeException("Child process didn't finish within given timeout");
        }

        int exitValue = process.exitValue();
        if (exitValue != 0) {
            throw new RuntimeException("Child process exited with non-zero code: " + exitValue);
        }
    }

    /**
     * Terminates process and closes all related streams
     */
    @Override
    public void close()
    {
        // destroy process
        if (process.isAlive()) {
            process.destroy();
        }

        // then close all related streams
        super.close();
    }
}
