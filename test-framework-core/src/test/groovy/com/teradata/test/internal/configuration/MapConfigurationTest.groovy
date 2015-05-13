/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import com.teradata.test.internal.configuration.MapConfiguration
import spock.lang.Specification

import static com.teradata.test.internal.configuration.EmptyConfiguration.emptyConfiguration

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
                                  ],
                          'e': 'tola'
                  ],
                  'x': [
                          'y': 10
                  ]
          ]
  )

  def 'test list keys'()
  {
    expect:
    configuration.listKeys() == ['a.b.c', 'a.b.d', 'a.e', 'x.y'] as Set
  }

  def 'test get objects'()
  {
    expect:
    configuration.get('a.b.c') == Optional.of('ala')
    configuration.get('a.b.d') == Optional.of('ela')
    configuration.get('x.y') == Optional.of(10)
    configuration.get('x.y.x') == Optional.empty()
  }

  def 'test subconfiguration'()
  {
    setup:
    def subConfigurationA = configuration.getSubconfiguration('a')
    def subConfigurationAB = configuration.getSubconfiguration('a.b')
    def subConfigurationX = configuration.getSubconfiguration('x')
    def subConfigurationXY = configuration.getSubconfiguration('x.y')

    expect:
    subConfigurationA.listKeys() == ['b.c', 'b.d', 'e'] as Set
    subConfigurationAB.listKeys() == ['c', 'd'] as Set
    subConfigurationA.getString('b.c') == Optional.of('ala')
    subConfigurationA.getString('b.d') == Optional.of('ela')
    subConfigurationAB.getString('c') == Optional.of('ala')
    subConfigurationAB.getString('d') == Optional.of('ela')
    subConfigurationA.listKeyPrefixes(1) == ['b', 'e'] as Set
    subConfigurationX.listKeyPrefixes(1) == ['y'] as Set
    subConfigurationX.getSubconfiguration('y') == emptyConfiguration()
    subConfigurationXY.listKeyPrefixes(1) == [] as Set
  }

}
