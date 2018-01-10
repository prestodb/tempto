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

package io.prestodb.tempto.internal.fulfillment.ldap;

import io.prestodb.tempto.fulfillment.ldap.LdapObjectDefinition;
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
import javax.naming.directory.ModificationItem;

import java.util.Hashtable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DefaultLdapObjectEntryManager
        implements LdapObjectEntryManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapObjectEntryManager.class);
    private final String ldapUrl;
    private final String ldapAdminDistinguishedName;
    private final String ldapAdminPassword;

    @Inject
    public DefaultLdapObjectEntryManager(
            @Named("ldap.url") String ldapUrl,
            @Named("ldap.admin.dn") String ldapAdminDistinguishedName,
            @Named("ldap.admin.password") String ldapAdminPassword)
    {
        this.ldapUrl = requireNonNull(ldapUrl, "ldapUrl is null");
        this.ldapAdminDistinguishedName = requireNonNull(ldapAdminDistinguishedName, "ldapAdminDistinguishedName is null");
        this.ldapAdminPassword = requireNonNull(ldapAdminPassword, "ldapAdminPassword is null");
    }

    private DirContext createContext(String ldapUrl, String ldapAdminDistinguishedName, String ldapAdminPassword)
    {
        Hashtable<String, String> environment = new Hashtable<>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapUrl);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, ldapAdminDistinguishedName);
        environment.put(Context.SECURITY_CREDENTIALS, ldapAdminPassword);
        try {
            return new InitialDirContext(environment);
        }
        catch (NamingException e) {
            throw new RuntimeException("Connection to LDAP server failed", e);
        }
    }

    private void closeQuietly(DirContext context)
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

    private String addLdapDefinition(LdapObjectDefinition ldapObjectDefinition, DirContext context)
    {
        checkNotNull(ldapObjectDefinition, "LDAP Object Definition is null");

        Attributes entries = new BasicAttributes();
        Attribute objectClass = new BasicAttribute("objectClass");

        ldapObjectDefinition.getAttributes()
                .forEach((k, v) -> entries.put(new BasicAttribute(k, v)));

        List<ModificationItem> modificationItems = ldapObjectDefinition.getModificationAttributes().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(attribute -> new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(entry.getKey(), attribute))))
                .collect(toList());

        ldapObjectDefinition.getObjectClasses()
                .forEach(objectClass::add);
        entries.put(objectClass);

        try {
            context.createSubcontext(ldapObjectDefinition.getDistinguishedName(), entries);
            context.modifyAttributes(ldapObjectDefinition.getDistinguishedName(), modificationItems.stream().toArray(ModificationItem[]::new));
            LOGGER.info("Successfully added entry " + ldapObjectDefinition.getId());
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
    public void addLdapDefinitions(List<LdapObjectDefinition> ldapObjectDefinitions)
    {
        DirContext context = createContext(ldapUrl, ldapAdminDistinguishedName, ldapAdminPassword);
        try {
            for (LdapObjectDefinition ldapObjectDefinition : ldapObjectDefinitions) {
                addLdapDefinition(ldapObjectDefinition, context);
            }
        }
        finally {
            closeQuietly(context);
        }
    }
}
