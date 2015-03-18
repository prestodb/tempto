/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment

import com.teradata.test.configuration.Configuration
import com.teradata.test.fulfillment.jdbc.JdbcConnectivityFulfiller
import com.teradata.test.fulfillment.jdbc.JdbcConnectivityRequirement
import com.teradata.test.fulfillment.jdbc.JdbcConnectivityState
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager

class JdbcConnectivityFulfillerTest
        extends Specification
{
  private static final def String DRIVER_CLASSNAME = JdbcConnectivityFulfillerTest.class.name
  private static final def String DRIVER_URL = 'jdbc:foo'
  private static final def String DRIVER_USER = 'user'
  private static final def String DRIVER_PASSWORD = 'password'

  @Shared
  def Driver driver

  def setupSpec()
  {
    driver = Mock(Driver)
    driver.acceptsURL(DRIVER_URL) >> true
    driver.connect(_, _) >> Mock(Connection)
    DriverManager.registerDriver(driver)
  }

  def cleanupSpec()
  {
    DriverManager.deregisterDriver(driver)
  }

  def "should connect using JDBC driver"()
  {
    setup:
    def fulfiller = new JdbcConnectivityFulfiller(mockedConfiguration)

    when:
    fulfiller.fulfill([new JdbcConnectivityRequirement()] as Set)

    then:
    fulfiller.fulfill([new JdbcConnectivityRequirement()] as Set) == [new JdbcConnectivityState(DRIVER_CLASSNAME, DRIVER_URL, DRIVER_USER, DRIVER_PASSWORD)] as Set
  }

  def "should throw exception for unreachable JDBC server"()
  {
    setup:
    def fulfiller = new JdbcConnectivityFulfiller(invalidConfiguration)

    when:
    fulfiller.fulfill([new JdbcConnectivityRequirement()] as Set)

    then:
    thrown(RuntimeException)
  }

  def "should return empty states list"()
  {
    setup:
    def fulfiller = new JdbcConnectivityFulfiller(invalidConfiguration)

    expect:
    fulfiller.fulfill([] as Set) isEmpty()
  }

  def getInvalidConfiguration()
  {
    def testConfiguration = Mock(Configuration)
    testConfiguration.getStringMandatory('jdbc_driver_class') >> 'foo'
    testConfiguration.getStringMandatory('jdbc_url') >> 'url'
    testConfiguration.getStringMandatory('jdbc_user') >> 'user'
    testConfiguration.getStringMandatory('jdbc_password') >> 'password'
    testConfiguration
  }

  def getMockedConfiguration()
  {
    def testConfiguration = Mock(Configuration)
    testConfiguration.getStringMandatory('jdbc_driver_class') >> DRIVER_CLASSNAME
    testConfiguration.getStringMandatory('jdbc_url') >> DRIVER_URL
    testConfiguration.getStringMandatory('jdbc_user') >> DRIVER_USER
    testConfiguration.getStringMandatory('jdbc_password') >> DRIVER_PASSWORD
    testConfiguration
  }
}
