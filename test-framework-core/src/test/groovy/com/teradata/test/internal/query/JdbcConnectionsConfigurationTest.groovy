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

package com.teradata.test.internal.query

import com.teradata.test.internal.configuration.YamlConfiguration
import com.teradata.test.query.JdbcConnectivityParamsState
import spock.lang.Specification

import static java.util.Optional.empty

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

  b_alias:
    alias: b
""")

  private static final def EXPECTED_A_JDBC_CONNECTIVITY_PARAMS =
          new JdbcConnectivityParamsState('a', 'com.acme.ADriver', 'jdbc:a://localhost:8080',
                  'auser', 'apassword', true, empty())

  private static final def EXPECTED_B_JDBC_CONNECTIVITY_PARAMS =
          new JdbcConnectivityParamsState('b', 'com.acme.BDriver', 'jdbc:b://localhost:8080',
                  'buser', 'bpassword', false, Optional.of('/path/to/jar.jar'))

  private static final def EXPECTED_B_ALIAS_JDBC_CONNECTIVITY_PARAMS =
          new JdbcConnectivityParamsState('b_alias', 'com.acme.BDriver', 'jdbc:b://localhost:8080',
                  'buser', 'bpassword', false, Optional.of('/path/to/jar.jar'))

  def jdbcConnectionConfiguration = new JdbcConnectionsConfiguration(CONFIGURATION)

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
}
