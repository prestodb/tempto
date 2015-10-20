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

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.teradata.tempto.process.CliProcess;
import com.teradata.tempto.ssh.SshClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.Iterables.transform;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * An {@link SshClient} based on JSch library.
 */
public class JSchSshClient
        implements SshClient
{
    private static final Logger LOGGER = getLogger(JSchSshClient.class);

    private final Supplier<Session> sessionSupplier;

    public JSchSshClient(Supplier<Session> sessionSupplier)
    {
        this.sessionSupplier = requireNonNull(sessionSupplier, "sessionSupplier is null");
    }

    @Override
    public CliProcess execute(List<String> command)
    {
        return execute(Joiner.on(' ').join(quote(command)));
    }

    @Override
    public CliProcess execute(String command)
    {
        try {
            Session session = createSession();
            LOGGER.info("Executing on {}@{}: {}", session.getUserName(), session.getHost(), command);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            JSchCliProcess process = new JSchCliProcess(session, channel);
            process.connect();
            return process;
        }
        catch (JSchException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Session createSession()
            throws JSchException
    {
        Session session = sessionSupplier.get();
        session.setDaemonThread(true);
        session.connect();
        return session;
    }

    @Override
    public void upload(Path file, String remotePath)
    {
        Session session = null;
        try {
            session = createSession();
            LOGGER.info("Uploading {} onto {}@{}:{}", file, session.getUserName(), session.getHost(), remotePath);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            String command = "scp -t " + remotePath;
            channel.setCommand(command);

            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            sendSCPFile(file, channel, in, out);
        }
        catch (JSchException | IOException exception) {
            Throwables.propagate(exception);
        }
        finally {
            if (session != null) session.disconnect();
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

    @Override
    public void close()
            throws IOException
    {
    }


    private Iterable<String> quote(List<String> command)
    {
        return transform(command, (String s) -> "\"" + s + "\"");
    }
}
