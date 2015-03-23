/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query

import com.teradata.test.configuration.YamlConfiguration
import spock.lang.Specification

class JdbcConnectionsConfigurationTest
        extends Specification
{

  def configuration = new YamlConfiguration("""\
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

  b_alias:
    alias: b
""")

  def jdbcConnectionConfiguration = new JdbcConnectionsConfiguration(configuration)

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
    a == new JdbcConnectivityParamsState('a', 'com.acme.ADriver', 'jdbc:a://localhost:8080', 'auser', 'apassword', true)
    b == new JdbcConnectivityParamsState('b', 'com.acme.BDriver', 'jdbc:b://localhost:8080', 'buser', 'bpassword', false)
  }

  def "get connection configuration for alias"()
  {
    setup:
    def bAlias = jdbcConnectionConfiguration.getConnectionConfiguration('b_alias')

    expect:
    bAlias == new JdbcConnectivityParamsState('b_alias', 'com.acme.BDriver', 'jdbc:b://localhost:8080', 'buser', 'bpassword', false)
  }
}
