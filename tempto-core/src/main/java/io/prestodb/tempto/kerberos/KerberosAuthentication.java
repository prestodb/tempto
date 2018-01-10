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

package io.prestodb.tempto.kerberos;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

public class KerberosAuthentication
{
    private final Set<Principal> principalsSet;
    private final Configuration kerberosConfiguration;

    public KerberosAuthentication(String principal, String keytab)
    {
        requireNonNull(principal, "principal is null");
        requireNonNull(keytab, "keytab is null");
        this.principalsSet = ImmutableSet.of(new KerberosPrincipal(principal));
        this.kerberosConfiguration = createKerberosConfiguration(principal, keytab);
    }

    private static Configuration createKerberosConfiguration(String principal, String keytab)
    {
        Map<String, String> loginOptions = createLoginOptions(principal, keytab);
        return new Configuration()
        {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name)
            {
                return new AppConfigurationEntry[] {
                        new AppConfigurationEntry(
                                "com.sun.security.auth.module.Krb5LoginModule",
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                loginOptions
                        )
                };
            }
        };
    }

    private static Map<String, String> createLoginOptions(String principal, String keytab)
    {
        ImmutableMap.Builder<String, String> options = ImmutableMap.builder();
        options.put("useKeyTab", "true");
        options.put("principal", principal);
        options.put("keyTab", keytab);
        options.put("storeKey", "true");
        options.put("doNotPrompt", "true");
        options.put("isInitiator", "true");
        return options.build();
    }

    public Subject authenticate()
    {
        Subject subject = new Subject(false, principalsSet, emptySet(), emptySet());
        try {
            LoginContext loginContext = new LoginContext("", subject, null, kerberosConfiguration);
            loginContext.login();
            return loginContext.getSubject();
        }
        catch (LoginException e) {
            throw Throwables.propagate(e);
        }
    }
}
