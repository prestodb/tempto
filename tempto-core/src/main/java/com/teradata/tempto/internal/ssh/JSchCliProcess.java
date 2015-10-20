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
package com.teradata.tempto.internal.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.teradata.tempto.internal.process.CliProcessBase;
import com.teradata.tempto.process.CommandExecutionException;
import com.teradata.tempto.process.TimeoutRuntimeException;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;

import static java.lang.Thread.sleep;
import static org.slf4j.LoggerFactory.getLogger;

class JSchCliProcess
        extends CliProcessBase
{
    private static final Logger LOGGER = getLogger(JSchCliProcess.class);

    private final Session session;
    private final ChannelExec channel;

    JSchCliProcess(Session session, ChannelExec channel)
            throws IOException
    {
        super(channel.getInputStream(), channel.getErrStream() , channel.getOutputStream());
        this.session = session;
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
            throw new TimeoutRuntimeException("SSH channel did not finish within given timeout");
        }

        close();
        int exitStatus = channel.getExitStatus();
        if (channel.getExitStatus() != 0) {
            throw new CommandExecutionException("SSH command exited with status: " + exitStatus, exitStatus);
        }
    }

    /**
     * Terminates process and closes all related streams
     */
    @Override
    public void close()
    {
        try {
            channel.disconnect();
        } finally {
            try {
                session.disconnect();
            } finally {
                super.close();
            }
        }
    }
}
