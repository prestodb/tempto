/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import com.teradata.test.configuration.Configuration
import spock.lang.Specification

import static com.teradata.test.internal.configuration.TestConfigurationFactory.CLASSPATH_PROTOCOL
import static com.teradata.test.internal.configuration.TestConfigurationFactory.LOCAL_TEST_CONFIGURATION_URI_KEY
import static com.teradata.test.internal.configuration.TestConfigurationFactory.TEST_CONFIGURATION_URI_KEY

class TestConfigurationFactoryTest
        extends Specification
{

  def 'read test configuration'() {
    setup:
    System.setProperty(TEST_CONFIGURATION_URI_KEY, CLASSPATH_PROTOCOL + "/configuration/global-configuration-test.yaml");
    System.setProperty(LOCAL_TEST_CONFIGURATION_URI_KEY, CLASSPATH_PROTOCOL + "/configuration/local-configuration-test.yaml");

    when:
    Configuration configuration = TestConfigurationFactory.createTestConfiguration()

    then:
    configuration.getStringMandatory('value.local') == 'local'
    configuration.getStringMandatory('value.both') == 'local'
    configuration.getStringMandatory('value.global') == 'global'
  }
}
