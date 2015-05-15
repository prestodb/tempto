/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.ssh;

import com.teradata.test.process.CliProcess;

import java.nio.file.Path;
import java.util.List;

/**
 * Simple SSH client.
 */
public interface SshClient
{
    /**
     * Executes command on a remote machine.
     */
    CliProcess execute(List<String> command);

    default void executeAndWait(List<String> command)
            throws InterruptedException
    {
        execute(command).waitForWithTimeoutAndKill();
    }

    /**
     * Uploads file to a remote machine. It works like SCP.
     */
    void upload(Path file, String remotePath);
}
