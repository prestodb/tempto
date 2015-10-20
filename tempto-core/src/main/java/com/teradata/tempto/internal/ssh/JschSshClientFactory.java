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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.teradata.tempto.ssh.SshClient;
import com.teradata.tempto.ssh.SshClientFactory;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class JschSshClientFactory
        implements SshClientFactory
{
    private static final Logger LOGGER = getLogger(JschSshClientFactory.class);

    private final JSch jSch = new JSch();

    @Override
    public SshClient create(String host, int port, String user, Optional<String> password)
    {
        return new JSchSshClient(() -> {
            try {
                String passwordToString = password.isPresent() ? '/' + password.get() : "";
                LOGGER.debug("Allocating new SSH session to: {}{}@{}:{}", user, passwordToString, host, port);
                Session session = jSch.getSession(user, host, port);
                if (password.isPresent()) {
                    session.setPassword(password.get());
                }
                session.setConfig(getConfig());
                return session;
            }
            catch (JSchException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void addIdentity(String pathToPem)
    {
        try {
            LOGGER.debug("Adding SSH identity: {}", pathToPem);
            jSch.addIdentity(pathToPem);
        }
        catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties getConfig()
    {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        return config;
    }
}
