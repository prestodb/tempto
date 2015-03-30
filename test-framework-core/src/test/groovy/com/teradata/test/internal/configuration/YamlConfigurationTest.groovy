/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import com.teradata.test.internal.configuration.YamlConfiguration
import spock.lang.Specification

class YamlConfigurationTest
        extends Specification
{

  private static def YAML = """\
a:
   b:
      c: ala
      d: ela
x:
   y: 10
"""

  def test() {
    setup:
    def configuration = new YamlConfiguration(YAML)
    expect:
    configuration.listKeys() == ['a.b.c', 'a.b.d', 'x.y'] as Set
    configuration.getInt('x.y') == Optional.of(10)
    configuration.getString('a.b.c') == Optional.of('ala')
    configuration.getString('x.y') == Optional.of('10')
  }

}
