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
package io.prestodb.tempto.internal.ssh;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.initialization.AutoModuleProvider;
import io.prestodb.tempto.initialization.SuiteModuleProvider;
import io.prestodb.tempto.ssh.SshClient;
import io.prestodb.tempto.ssh.SshClientFactory;

import java.util.Optional;

import static com.google.inject.name.Names.named;

@AutoModuleProvider
public class SshClientModuleProvider
        implements SuiteModuleProvider
{
    private static final String SSH_KEY = "ssh";
    private static final String ROLES_KEY = "roles";
    private static final String IDENTITY_KEY = "identity";

    private static final String HOST_KEY = "host";
    private static final String PORT_KEY = "port";
    private static final String USER_KEY = "user";
    private static final String PASSWORD_KEY = "password";

    public Module getModule(Configuration configuration)
    {
        Configuration sshConfiguration = configuration.getSubconfiguration(SSH_KEY);
        Optional<String> identity = sshConfiguration.getString(IDENTITY_KEY);

        Configuration rolesConfiguration = sshConfiguration.getSubconfiguration(ROLES_KEY);
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                JschSshClientFactory sshClientFactory = new JschSshClientFactory();
                identity.ifPresent(identityValue -> sshClientFactory.addIdentity(identityValue));
                bind(SshClientFactory.class).toInstance(sshClientFactory);

                for (String role : rolesConfiguration.listKeyPrefixes(1)) {
                    Configuration roleConfiguration = rolesConfiguration.getSubconfiguration(role);
                    String host = roleConfiguration.getStringMandatory(HOST_KEY);
                    int port = roleConfiguration.getInt(PORT_KEY).orElse(22);
                    String user = roleConfiguration.getString(USER_KEY).orElse("root");
                    Optional<String> password = roleConfiguration.getString(PASSWORD_KEY);

                    Key<SshClient> sshClientKey = Key.get(SshClient.class, named(role));
                    bind(sshClientKey).toInstance(sshClientFactory.create(host, port, user, password));
                }
            }
        };
    }
}
