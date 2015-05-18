/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.teradata.test.internal.process.CliProcessBase;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;

import static java.lang.Thread.sleep;
import static org.slf4j.LoggerFactory.getLogger;

class JSchCliProcess
        extends CliProcessBase
{
    private static final Logger LOGGER = getLogger(JSchCliProcess.class);

    private final ChannelExec channel;

    JSchCliProcess(ChannelExec channel)
            throws IOException
    {
        super(channel.getInputStream(), channel.getErrStream() , channel.getOutputStream());
        this.channel = channel;
    }

    void connect()
            throws JSchException
    {
        channel.connect();
    }

    @Override
    public void waitForWithTimeoutAndKill(Duration timeout)
            throws InterruptedException
    {
        Thread thread = new Thread(() -> {
            readRemainingOutputLines();
            readRemainingErrorLines();
            // active waiting based on http://www.jcraft.com/jsch/examples/Exec.java.html example
            while (!channel.isClosed()) {
                try {
                    sleep(100);
                }
                catch (InterruptedException e) {
                    LOGGER.error("Interrupted exception", e);
                }
            }
        });
        thread.start();
        thread.join(timeout.toMillis());

        if (!channel.isClosed()) {
            close();
            thread.join();
            throw new RuntimeException("SSH channel did not finish within given timeout");
        }

        close();
        int exitStatus = channel.getExitStatus();
        if (channel.getExitStatus() != 0) {
            throw new RuntimeException("SSH command exited with status: " + exitStatus);
        }
    }

    /**
     * Terminates process and closes all related streams
     */
    @Override
    public void close()
    {
        // close channel first
        channel.disconnect();

        // close all related streams than
        super.close();
    }
}
