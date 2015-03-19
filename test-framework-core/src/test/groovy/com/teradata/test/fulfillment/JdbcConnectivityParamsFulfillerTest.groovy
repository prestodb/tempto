/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment

import com.teradata.test.configuration.YamlConfiguration
import com.teradata.test.fulfillment.jdbc.JdbcConnectivityParamsFulfiller
import com.teradata.test.fulfillment.jdbc.JdbcConnectivityParamsState
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager

class JdbcConnectivityParamsFulfillerTest
        extends Specification
{
  private static final String DRIVER_CLASSNAME_1 = JdbcConnectivityParamsFulfillerTest.class.name
  private static final String DRIVER_URL_1 = 'jdbc:foo'
  private static final String DRIVER_USER_1 = 'user'
  private static final String DRIVER_PASSWORD_1 = 'password'

  private static final String DRIVER_CLASSNAME_2 = JdbcConnectivityParamsFulfillerTest.class.name
  private static final String DRIVER_URL_2 = 'jdbc:foo'
  private static final String DRIVER_USER_2 = 'user'
  private static final String DRIVER_PASSWORD_2 = 'password'

  private static final String INVALID_DRIVER_CLASSNAME = 'foo'

  private static final String CONNECTION_NAME_1 = 'conn_1'
  private static final String CONNECTION_NAME_2 = 'conn_2'

  @Shared
  def Driver driver1
  @Shared
  def Driver driver2

  def setupSpec()
  {
    driver1 = Mock(Driver)
    driver1.acceptsURL(DRIVER_URL_1) >> true
    driver1.connect(_, _) >> Mock(Connection)
    driver2 = Mock(Driver)
    driver2.acceptsURL(DRIVER_URL_2) >> true
    driver2.connect(_, _) >> Mock(Connection)

    DriverManager.registerDriver(driver1)
    DriverManager.registerDriver(driver2)
  }

  def cleanupSpec()
  {
    DriverManager.deregisterDriver(driver1)
    DriverManager.deregisterDriver(driver2)
  }

  def "should connect using JDBC driver"()
  {
    setup:
    def fulfiller = new JdbcConnectivityParamsFulfiller(twoDatabasesConfiguration)

    when:
    fulfiller.fulfill()

    then:
    fulfiller.fulfill() == [
            new JdbcConnectivityParamsState(CONNECTION_NAME_1, DRIVER_CLASSNAME_1, DRIVER_URL_1, DRIVER_USER_1, DRIVER_PASSWORD_1),
            new JdbcConnectivityParamsState(CONNECTION_NAME_2, DRIVER_CLASSNAME_2, DRIVER_URL_2, DRIVER_USER_2, DRIVER_PASSWORD_2)
    ] as Set
  }

  def "should throw exception for unreachable JDBC server"()
  {
    setup:
    def fulfiller = new JdbcConnectivityParamsFulfiller(invalidConfiguration)

    when:
    fulfiller.fulfill()

    then:
    thrown(RuntimeException)
  }

  def getInvalidConfiguration()
  {
    return new YamlConfiguration("""\
databases:
  conn_1:
    jdbc_driver_class : ${INVALID_DRIVER_CLASSNAME}
    jdbc_url : ${DRIVER_URL_1}
    jdbc_user : ${DRIVER_USER_1}
    jdbc_password : ${DRIVER_PASSWORD_1}
""")
  }

  def getTwoDatabasesConfiguration()
  {
    return new YamlConfiguration("""\
databases:
  conn_1: 
    jdbc_driver_class : ${DRIVER_CLASSNAME_1}
    jdbc_url : ${DRIVER_URL_1}
    jdbc_user : ${DRIVER_USER_1}
    jdbc_password : ${DRIVER_PASSWORD_1}
  conn_2: 
    jdbc_driver_class : ${DRIVER_CLASSNAME_2}
    jdbc_url : ${DRIVER_URL_2}
    jdbc_user : ${DRIVER_USER_2}
    jdbc_password : ${DRIVER_PASSWORD_2}
""")
  }
}
