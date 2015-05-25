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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;
import com.teradata.test.ssh.SshClient;

import static com.google.inject.name.Names.named;
import static com.teradata.test.internal.ssh.JSchSshClient.SSH_HOST_BINDING_NAME;
import static com.teradata.test.internal.ssh.JSchSshClient.SSH_PASSWORD_BINDING_NAME;
import static com.teradata.test.internal.ssh.JSchSshClient.SSH_PORT_BINDING_NAME;
import static com.teradata.test.internal.ssh.JSchSshClient.SSH_USER_BINDING_NAME;

@AutoModuleProvider
public class SshClientModuleProvider
        implements SuiteModuleProvider
{
    private static final String SSH_ROLES_CONFIGURATION_SECTION = "ssh_roles";
    private static final String HOST_KEY = "host";
    private static final String PORT_KEY = "port";
    private static final String USER_KEY = "user";
    private static final String PASSWORD_KEY = "password";

    public Module getModule(Configuration configuration)
    {
        Configuration rolesConfiguration = configuration.getSubconfiguration(SSH_ROLES_CONFIGURATION_SECTION);
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                for (String role : rolesConfiguration.listKeyPrefixes(1)) {
                    Configuration roleConfiguration = rolesConfiguration.getSubconfiguration(role);
                    String host = roleConfiguration.getStringMandatory(HOST_KEY);
                    int port = roleConfiguration.getIntMandatory(PORT_KEY);
                    String user = roleConfiguration.getStringMandatory(USER_KEY);
                    String password = roleConfiguration.getStringMandatory(PASSWORD_KEY);
                    bindSshClient(role, host, port, user, password);
                }
            }

            private void bindSshClient(String role, String host, int port, String user, String password)
            {
                PrivateModule privateModule = new PrivateModule()
                {
                    @Override
                    protected void configure()
                    {
                        Key<SshClient> sshClientKey = Key.get(SshClient.class, named(role));
                        bind(String.class).annotatedWith(named(SSH_HOST_BINDING_NAME)).toInstance(host);
                        bind(Integer.class).annotatedWith(named(SSH_PORT_BINDING_NAME)).toInstance(port);
                        bind(String.class).annotatedWith(named(SSH_USER_BINDING_NAME)).toInstance(user);
                        bind(String.class).annotatedWith(named(SSH_PASSWORD_BINDING_NAME)).toInstance(password);
                        bind(sshClientKey).to(JSchSshClient.class).in(Singleton.class);
                        expose(sshClientKey);
                    }
                };
                install(privateModule);
            }
        };
    }
}
