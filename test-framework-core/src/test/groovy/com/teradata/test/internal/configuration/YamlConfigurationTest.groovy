/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import spock.lang.Specification

class YamlConfigurationTest
        extends Specification
{
  def static final ENV_VARIABLE_KEY = System.getenv().keySet().iterator().next()

  def getTestYaml()
  {
    """\
a:
   b:
      c: ala\${${ENV_VARIABLE_KEY}}
      d: ela\${foo}
x:
   y: 10
"""
  }

  def test()
  {
    setup:
    def configuration = new YamlConfiguration(getTestYaml())
    expect:
    configuration.listKeys() == ['a.b.c', 'a.b.d', 'x.y'] as Set
    configuration.getInt('x.y') == Optional.of(10)
    configuration.getString('a.b.c') == Optional.of('ala' + System.getenv().get(ENV_VARIABLE_KEY))
    configuration.getString('a.b.d') == Optional.of('ela${foo}')
    configuration.getString('x.y') == Optional.of('10')
  }
}
