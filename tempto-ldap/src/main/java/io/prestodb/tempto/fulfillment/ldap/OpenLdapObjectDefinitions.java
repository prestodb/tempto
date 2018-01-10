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

package io.prestodb.tempto.fulfillment.ldap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class OpenLdapObjectDefinitions
{
    public static final LdapObjectDefinition TEST_ORG =
            LdapObjectDefinition.builder("TestOrg")
                    .setDistinguishedName("ou=Test,dc=tempto,dc=com")
                    .setAttributes(newHashMap())
                    .setObjectClasses(Arrays.asList("organizationalUnit"))
                    .build();

    public static final LdapObjectDefinition TEST_GROUP =
            LdapObjectDefinition.builder("TestGroup")
                    .setDistinguishedName("cn=TestGroup,ou=Test,dc=tempto,dc=com")
                    .setAttributes(testGroupAttributes())
                    .setObjectClasses(Arrays.asList("groupOfNames"))
                    .build();

    private static Map<String, String> testGroupAttributes()
    {
        Map<String, String> attributes = newHashMap();
        attributes.put("cn", "Test Group");
        attributes.put("member", "uid=testuser,ou=Test,dc=tempto,dc=com");
        return attributes;
    }

    public static final LdapObjectDefinition TEST_USER =
            LdapObjectDefinition.builder("TestUser")
                    .setDistinguishedName("uid=testuser,ou=Test,dc=tempto,dc=com")
                    .setAttributes(testUserAttributes())
                    .setModificationAttributes(ImmutableMap.of("memberOf", ImmutableList.of("cn=TestGroup,ou=Test,dc=tempto,dc=com")))
                    .setObjectClasses(Arrays.asList("person", "inetOrgPerson"))
                    .build();

    private static Map<String, String> testUserAttributes()
    {
        Map<String, String> attributes = newHashMap();
        attributes.put("cn", "Test User");
        attributes.put("sn", "User");
        attributes.put("password", "testp@ss");
        return attributes;
    }
}
