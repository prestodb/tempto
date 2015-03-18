/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.configuration

import spock.lang.Specification

class MapConfigurationTest
        extends Specification
{

  def configuration = new MapConfiguration(
          [
                  'a': [
                          'b':
                                  [
                                          c: 'ala',
                                          d: 'ela'
                                  ]
                  ],
                  'x': [
                          'y': 10
                  ]
          ]
  )

  def 'test list keys'()
  {
    expect:
    configuration.listKeys() == ['a.b.c', 'a.b.d', 'x.y'] as Set
  }

  def 'test get objects'()
  {
    expect:
    configuration.getObject('a.b.c') == Optional.of('ala')
    configuration.getObject('a.b.d') == Optional.of('ela')
    configuration.getObject('x.y') == Optional.of(10)
    configuration.getObject('x.y.x') == Optional.empty()
  }

  def 'test subconfiguration'()
  {
    setup:
    def subConfigurationA = configuration.getSubconfiguration('a')
    def subConfigurationAB = configuration.getSubconfiguration('a.b')

    expect:
    subConfigurationA.listKeys() == ['b.c', 'b.d'] as Set
    subConfigurationAB.listKeys() == ['c', 'd'] as Set
    subConfigurationA.getString('b.c') == Optional.of('ala')
    subConfigurationA.getString('b.d') == Optional.of('ela')
    subConfigurationAB.getString('c') == Optional.of('ala')
    subConfigurationAB.getString('d') == Optional.of('ela')
    subConfigurationA.listKeyPrefixes(1) == ['b'] as Set
  }

}
