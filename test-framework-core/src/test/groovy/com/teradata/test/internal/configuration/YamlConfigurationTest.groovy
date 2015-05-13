/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import spock.lang.Specification

class YamlConfigurationTest
        extends Specification
{

  def create()
  {
    setup:
    def configuration = new YamlConfiguration("""\
a:
   b:
      d: ela\${foo}
x:
   y: 10
""")
    expect:
    configuration.listKeys() == ['a.b.d', 'x.y'] as Set
    configuration.getInt('x.y') == Optional.of(10)
    configuration.getString('a.b.d') == Optional.of('ela${foo}')
    configuration.getString('x.y') == Optional.of('10')
  }
}
