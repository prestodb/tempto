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

package com.teradata.tempto.fulfillment.ldap;

import java.util.Arrays;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.ImmutableMap;

public class OpenLdapObjectDefinitions
{
    public static final LdapObjectDefinition TEST_ORG =
            LdapObjectDefinition.builder("TestOrg")
                    .setDistinguishedName("ou=Test,dc=tempto,dc=com")
                    .setAttributes(newHashMap())
                    .addObjectClasses(Arrays.asList("organizationalUnit"))
                    .build();

    public static final LdapObjectDefinition TEST_GROUP =
            LdapObjectDefinition.builder("TestGroup")
                    .setDistinguishedName("cn=TestGroup,ou=Test,dc=tempto,dc=com")
                    .setAttributes(getTestGroupAttributes())
                    .addObjectClasses(Arrays.asList("groupOfNames"))
                    .build();

    public static final LdapObjectDefinition TEST_USER =
            LdapObjectDefinition.builder("TestUser")
                    .setDistinguishedName("uid=testuser,ou=Test,dc=tempto,dc=com")
                    .setAttributes(getTestUserAttributes())
                    .addObjectClasses(Arrays.asList("person", "inetOrgPerson"))
                    .build();

    private static Map<String, String> getTestGroupAttributes()
    {
        Map<String, String> attributes = newHashMap();
        attributes.put("cn", "Test Group");
        attributes.put("member", "uid=testuser,ou=Test,dc=tempto,dc=com");
        return attributes;
    }

    private static Map<String, String> getTestUserAttributes()
    {
        Map<String, String> attributes = newHashMap();
        attributes.put("cn", "Test User");
        attributes.put("sn", "User");
        attributes.put("password", "testp@ss");
        return attributes;
    }

    public static final LdapObjectDefinition AMERICA_ORG =
            LdapObjectDefinition.builder("AmericaOrg")
                    .setDistinguishedName("ou=America,dc=presto,dc=testldap,dc=com")
                    .setAttributes(newHashMap())
                    .addObjectClasses(Arrays.asList("organizationalUnit"))
                    .build();

    public static final LdapObjectDefinition ASIA_ORG =
            LdapObjectDefinition.builder("AsiaOrg")
                    .setDistinguishedName("ou=Asia,dc=presto,dc=testldap,dc=com")
                    .setAttributes(newHashMap())
                    .addObjectClasses(Arrays.asList("organizationalUnit"))
                    .build();

    public static final LdapObjectDefinition DEFAULT_GROUP =
            LdapObjectDefinition.builder("DefaultGroup")
                    .setDistinguishedName("cn=DefaultGroup,ou=America,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "DefaultGroup",
                            "member", "uid=DefaultGroupUser,ou=Asia,dc=presto,dc=testldap,dc=com"))
                    .addObjectClasses(Arrays.asList("groupOfNames"))
                    .build();

    public static final LdapObjectDefinition PARENT_GROUP =
            LdapObjectDefinition.builder("ParentGroup")
                    .setDistinguishedName("cn=ParentGroup,ou=America,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "ParentGroup",
                            "member", "uid=ParentGroupUser,ou=Asia,dc=presto,dc=testldap,dc=com",
                            "member", "uid=UserInMultipleGroups,ou=Asia,dc=presto,dc=testldap,dc=com"))
                    .addObjectClasses(Arrays.asList("groupOfNames"))
                    .build();

    public static final LdapObjectDefinition CHILD_GROUP =
            LdapObjectDefinition.builder("ChildGroup")
                    .setDistinguishedName("cn=ChildGroup,ou=America,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "ChildGroup",
                            "member", "uid=ChildGroupUser,ou=Asia,dc=presto,dc=testldap,dc=com"))
                    .addObjectClasses(Arrays.asList("groupOfNames"))
                    .build();

    public static final LdapObjectDefinition ANOTHER_GROUP =
            LdapObjectDefinition.builder("AnotherGroup")
                    .setDistinguishedName("cn=AnotherGroup,ou=America,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "AnotherGroup",
                            "member", "uid=UserInMultipleGroups,ou=Asia,dc=presto,dc=testldap,dc=com"))
                    .addObjectClasses(Arrays.asList("groupOfNames"))
                    .build();


    public static final LdapObjectDefinition DEFAULT_GROUP_USER =
            LdapObjectDefinition.builder("DefaultGroupUser")
                    .setDistinguishedName("uid=defaultgroupuser,ou=Asia,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "DefaultGroupUser",
                            "sn", "DefaultGroupUser",
                            "password", "password",
                            "memberOf", "DefaultGroup"
                    ))
                    .addObjectClasses(Arrays.asList("person", "inetOrgPerson"))
                    .build();

    public static final LdapObjectDefinition PARENT_GROUP_USER =
            LdapObjectDefinition.builder("ParentGroupUser")
                    .setDistinguishedName("uid=parentgroupuser,ou=Asia,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "ParentGroupUser",
                            "sn", "ParentGroupUser",
                            "password", "password",
                            "memberOf", "ParentGroup"
                    ))
                    .addObjectClasses(Arrays.asList("person", "inetOrgPerson"))
                    .build();

    public static final LdapObjectDefinition CHILD_GROUP_USER =
            LdapObjectDefinition.builder("ChildGroupUser")
                    .setDistinguishedName("uid=childgroupuser,ou=Asia,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "ChildGroupUser",
                            "sn", "ChildGroupUser",
                            "password", "password",
                            "memberOf", "ChildGroup"
                    ))
                    .addObjectClasses(Arrays.asList("person", "inetOrgPerson"))
                    .build();

    public static final LdapObjectDefinition USER_IN_MULTIPLE_GROUPS =
            LdapObjectDefinition.builder("UserInMultipleGroups")
                    .setDistinguishedName("uid=userinmultiplegroups,ou=Asia,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "UserInMultipleGroups",
                            "sn", "UserInMultipleGroups",
                            "password", "password",
                            "memberOf", "DefaultGroup",
                            "memberOf", "AnotherGroup"
                    ))
                    .addObjectClasses(Arrays.asList("person", "inetOrgPerson"))
                    .build();

    public static final LdapObjectDefinition ORPHAN_USER =
            LdapObjectDefinition.builder("OrphanUser")
                    .setDistinguishedName("uid=orphanuser,ou=Asia,dc=presto,dc=testldap,dc=com")
                    .setAttributes(ImmutableMap.of(
                            "cn", "OrphanUser",
                            "sn", "OrphanUser",
                            "password", "password"
                    ))
                    .addObjectClasses(Arrays.asList("person", "inetOrgPerson"))
                    .build();
}
