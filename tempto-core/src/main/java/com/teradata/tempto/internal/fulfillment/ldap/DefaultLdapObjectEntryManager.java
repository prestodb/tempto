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

package com.teradata.tempto.internal.fulfillment.ldap;

import com.teradata.tempto.fulfillment.ldap.LdapObjectDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.util.Hashtable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DefaultLdapObjectEntryManager
        implements LdapObjectEntryManager
{
    private final DirContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapObjectEntryManager.class);

    @Inject
    public DefaultLdapObjectEntryManager(
            @Named("ldap.url") String ldapUrl,
            @Named("ldap.admin.dn") String ldapAdminDistinguishedName,
            @Named("ldap.admin.password") String ldapAdminPassword)
    {
        this.context = createContext(
                requireNonNull(ldapUrl, "ldapUrl is null"),
                requireNonNull(ldapAdminDistinguishedName, "ldapAdminDistinguishedName is null"),
                requireNonNull(ldapAdminPassword, "ldapAdminPassword is null"));
    }

    private DirContext createContext(String ldapUrl, String ldapAdminDistinguishedName, String ldapAdminPassword)
    {
        Hashtable<String, String> environment = new Hashtable<>();

        DirContext dirContext = null;
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapUrl);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, ldapAdminDistinguishedName);
        environment.put(Context.SECURITY_CREDENTIALS, ldapAdminPassword);
        try {
            dirContext = new InitialDirContext(environment);
        }
        catch (NamingException e) {
            throw new RuntimeException("Connection to LDAP server failed", e);
        }
        finally {
            closeQuietly();
        }
        return dirContext;
    }

    private void closeQuietly()
    {
        try {
            if (context != null) {
                context.close();
            }
        }
        catch (NamingException e) {
            // ignore
        }
    }

    private String addLdapDefinition(LdapObjectDefinition ldapObjectDefinition)
    {
        checkNotNull(ldapObjectDefinition, "LDAP Object Definition is null");

        Attributes entries = new BasicAttributes();
        Attribute objectClass = new BasicAttribute("objectClass");

        ldapObjectDefinition.getAttributes()
                .forEach((k, v) -> entries.put(new BasicAttribute(k, v)));
        ldapObjectDefinition.getObjectClasses()
                .forEach(objectClass::add);
        entries.put(objectClass);

        try {
            context.createSubcontext(ldapObjectDefinition.getDistinguishedName(), entries);
        }
        catch (NameAlreadyBoundException e) {
            LOGGER.info(format("LDAP Entry %s already exists. Ignoring...", ldapObjectDefinition.getId()));
        }
        catch (NamingException e) {
            throw new RuntimeException("LDAP Entry addition failed", e);
        }

        return ldapObjectDefinition.getId();
    }

    @Override
    public List<String> addLdapDefinitions(List<LdapObjectDefinition> ldapObjectDefinitions)
    {
        try {
            return ldapObjectDefinitions.stream()
                    .map(this::addLdapDefinition)
                    .collect(toList());
        }
        finally {
            closeQuietly();
        }
    }
}
