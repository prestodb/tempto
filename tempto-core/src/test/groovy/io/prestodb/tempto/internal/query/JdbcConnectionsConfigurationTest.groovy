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

package io.prestodb.tempto.internal.query

import io.prestodb.tempto.internal.configuration.YamlConfiguration
import io.prestodb.tempto.query.JdbcConnectivityParamsState
import spock.lang.Specification

class JdbcConnectionsConfigurationTest
        extends Specification
{
    private static final def CONFIGURATION = new YamlConfiguration("""\
databases:
  a:
    jdbc_driver_class: com.acme.ADriver
    jdbc_url: jdbc:a://localhost:8080
    jdbc_user: auser
    jdbc_password: apassword

  b:
    jdbc_driver_class: com.acme.BDriver
    jdbc_url: jdbc:b://localhost:8080
    jdbc_user: buser
    jdbc_password: bpassword
    jdbc_pooling: false
    jdbc_jar: /path/to/jar.jar
    prepare_statement: USE schema
    kerberos_principal: HIVE@EXAMPLE.COM
    kerberos_keytab: example.keytab

  b_alias:
    alias: b

  non_jdbc_db:
    blah: true
""")

    private static final def BAD_CONFIGURATION = new YamlConfiguration("""\
databases:
  a:
    alias: b

  b:
    alias: c

  c:
    alias: a

  d:
    alias: blah
""")

    private static final def EXPECTED_A_JDBC_CONNECTIVITY_PARAMS =
            JdbcConnectivityParamsState.builder()
                    .setName('a')
                    .setDriverClass('com.acme.ADriver')
                    .setUrl('jdbc:a://localhost:8080')
                    .setUser('auser')
                    .setPassword('apassword')
                    .build()

    private static final def EXPECTED_B_JDBC_CONNECTIVITY_PARAMS =
            JdbcConnectivityParamsState.builder()
                    .setName('b')
                    .setDriverClass('com.acme.BDriver')
                    .setUrl('jdbc:b://localhost:8080')
                    .setUser('buser')
                    .setPassword('bpassword')
                    .setJar(Optional.of('/path/to/jar.jar'))
                    .setPrepareStatement(Optional.of('USE schema'))
                    .setKerberosPrincipal(Optional.of('HIVE@EXAMPLE.COM'))
                    .setKerberosKeytab(Optional.of('example.keytab'))
                    .build();

    private static final def EXPECTED_B_ALIAS_JDBC_CONNECTIVITY_PARAMS =
            JdbcConnectivityParamsState.builder()
                    .setName('b_alias')
                    .setDriverClass('com.acme.BDriver')
                    .setUrl('jdbc:b://localhost:8080')
                    .setUser('buser')
                    .setPassword('bpassword')
                    .setJar(Optional.of('/path/to/jar.jar'))
                    .setPrepareStatement(Optional.of('USE schema'))
                    .setKerberosPrincipal(Optional.of('HIVE@EXAMPLE.COM'))
                    .setKerberosKeytab(Optional.of('example.keytab'))
                    .build();

    def jdbcConnectionConfiguration = new JdbcConnectionsConfiguration(CONFIGURATION)
    def jdbcBadConnectionConfiguration = new JdbcConnectionsConfiguration(BAD_CONFIGURATION)

    def "list database connection configurations"()
    {
        expect:
        jdbcConnectionConfiguration.getDefinedJdcbConnectionNames() == ['a', 'b', 'b_alias'] as Set
    }

    def "get connection configuration"()
    {
        setup:
        def a = jdbcConnectionConfiguration.getConnectionConfiguration('a')
        def b = jdbcConnectionConfiguration.getConnectionConfiguration('b')

        expect:
        a == EXPECTED_A_JDBC_CONNECTIVITY_PARAMS
        b == EXPECTED_B_JDBC_CONNECTIVITY_PARAMS
    }

    def "get connection configuration for alias"()
    {
        setup:
        def bAlias = jdbcConnectionConfiguration.getConnectionConfiguration('b_alias')

        expect:
        bAlias == EXPECTED_B_ALIAS_JDBC_CONNECTIVITY_PARAMS
    }

    def "get connection configuration for unresolvable alias"()
    {
        when:
        jdbcBadConnectionConfiguration.getConnectionConfiguration('d')

        then:
        thrown(IllegalStateException)
    }

    def "get connection configuration for looping alias"()
    {
        when:
        jdbcBadConnectionConfiguration.getConnectionConfiguration('a')

        then:
        thrown(IllegalStateException)
    }
}
