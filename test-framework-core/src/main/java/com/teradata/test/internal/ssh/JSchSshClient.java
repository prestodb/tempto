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
package com.teradata.test.internal.ssh;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.teradata.test.process.CliProcess;
import com.teradata.test.ssh.SshClient;
import org.slf4j.Logger;

import javax.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static com.google.common.collect.Iterables.transform;
import static java.nio.file.Files.newInputStream;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * An {@link SshClient} based on JSch library.
 */
public class JSchSshClient
        implements SshClient
{
    static final String SSH_HOST_BINDING_NAME = "ssh.host";
    static final String SSH_PORT_BINDING_NAME = "ssh.port";
    static final String SSH_USER_BINDING_NAME = "ssh.user";
    static final String SSH_PASSWORD_BINDING_NAME = "ssh.password";

    private static final Logger LOGGER = getLogger(JSchSshClient.class);

    private final String sshHost;
    private final int sshPort;
    private final String sshUser;
    private final String sshPassword;
    private final JSch jSch = new JSch();

    private Session session;

    @Inject
    public JSchSshClient(
            @Named(SSH_HOST_BINDING_NAME) String sshHost,
            @Named(SSH_PORT_BINDING_NAME) int sshPort,
            @Named(SSH_USER_BINDING_NAME) String sshUser,
            @Named(SSH_PASSWORD_BINDING_NAME) String sshPassword)
    {
        this.sshHost = sshHost;
        this.sshPort = sshPort;
        this.sshUser = sshUser;
        this.sshPassword = sshPassword;
    }

    @Override
    public CliProcess execute(String command)
    {
        try {
            ChannelExec channel = (ChannelExec) getActiveSession().openChannel("exec");
            channel.setCommand(command);
            JSchCliProcess process = new JSchCliProcess(channel);
            process.connect();
            return process;
        }
        catch (JSchException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public CliProcess execute(List<String> command)
    {
        return execute(Joiner.on(' ').join(quote(command)));
    }

    @Override
    public void upload(Path file, String remotePath)
    {
        try {
            ChannelExec channel = (ChannelExec) getActiveSession().openChannel("exec");
            String command = "scp -t " + remotePath;
            channel.setCommand(command);

            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            sendSCPFile(file, channel, in, out);
        }
        catch (JSchException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void sendSCPFile(Path file, ChannelExec channel, InputStream in, OutputStream out)
            throws IOException, JSchException
    {
        channel.connect();
        try {
            checkAck(channel, in);

            sendSCPHandshake(file, out);
            checkAck(channel, in);

            try (InputStream fileStream = newInputStream(file)) {
                ByteStreams.copy(fileStream, out);
            }
            out.write(0);

            out.flush();
            checkAck(channel, in);
        }
        finally {
            channel.disconnect();
        }
    }

    private void sendSCPHandshake(Path file, OutputStream out)
            throws IOException
    {
        long fileSize = Files.size(file);
        String command = "C0644 " + fileSize + " " + file.getFileName().toString() + "\n";
        out.write(command.getBytes());
        out.flush();
    }

    private void checkAck(ChannelExec channel, InputStream in)
            throws IOException
    {
        int returnCode = in.read();
        if (returnCode != 0) {
            channel.disconnect();
            throw new RuntimeException("SCP failed, error code: " + returnCode);
        }
    }

    private Session getActiveSession()
            throws JSchException
    {
        try {
            ChannelExec testChannel = (ChannelExec) session.openChannel("exec");
            testChannel.setCommand("true");
            testChannel.connect();
            testChannel.disconnect();
        }
        catch (Throwable t) {
            LOGGER.info("Allocating new SSH session to: {}", sshHost);
            session = jSch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);
            session.setConfig(getConfig());
            session.connect();
        }
        return session;
    }

    private Properties getConfig()
    {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        return config;
    }

    private Iterable<String> quote(List<String> command)
    {
        return transform(command, (String s) -> "\"" + s + "\"");
    }
}
