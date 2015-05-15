/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.teradata.test.internal.process.CliProcessBase;

import java.io.IOException;
import java.time.Duration;

class JSchCliProcess
        extends CliProcessBase
{
    private final ChannelExec channel;

    JSchCliProcess(ChannelExec channel)
            throws IOException
    {
        super(channel.getInputStream(), channel.getOutputStream());
        this.channel = channel;

        channel.setErrStream(System.err);
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
        Thread thread = new Thread(this::readRemainingOutputLines);
        thread.start();
        thread.join(timeout.toMillis());

        if (!channel.isClosed()) {
            channel.disconnect();
            thread.join();
            throw new RuntimeException("SSH channel did not finish within given timeout");
        }

        int exitStatus = channel.getExitStatus();
        if (channel.getExitStatus() != 0) {
            throw new RuntimeException("SSH command exited with status: " + exitStatus);
        }
    }
}
