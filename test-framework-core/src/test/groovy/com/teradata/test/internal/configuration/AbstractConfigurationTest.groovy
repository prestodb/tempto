/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import spock.lang.Specification

class AbstractConfigurationTest
        extends Specification
{

  public static final String KEY = 'a.b.c'
  def configuration = Spy(AbstractConfiguration)

  def 'get present integer value'()
  {
    when:
    setupGetObject(KEY, 10)

    then:
    configuration.getInt(KEY) == Optional.of(10)
    configuration.getIntMandatory(KEY) == 10
  }

  def 'get present integer value from String'()
  {
    when:
    setupGetObject(KEY, '10')

    then:
    configuration.getInt(KEY) == Optional.of(10)
    configuration.getIntMandatory(KEY) == 10
  }

  def 'non-mandatory get integer not present value'()
  {
    when:
    setupGetObject(KEY, null)

    then:
    configuration.getInt(KEY) == Optional.empty()
  }

  def 'mandatory get integer not present value'()
  {
    when:
    setupGetObject(KEY, null)
    configuration.getIntMandatory(KEY)

    then:
    def e = thrown(IllegalStateException)
    e.message == 'could not find value for key a.b.c'
  }

  def 'mandatory get integer not present value with message'()
  {
    when:
    setupGetObject(KEY, null)
    configuration.getIntMandatory(KEY, 'damn, no key')

    then:
    def e = thrown(IllegalStateException)
    e.message == 'damn, no key'
  }

  def 'get integer for non matching type'()
  {
    when:
    setupGetObject(KEY, [])
    configuration.getInt(KEY)

    then:
    def e = thrown(IllegalStateException)
    e.message == 'expected java.lang.Integer value for key a.b.c but got java.util.ArrayList'
  }

  def 'get present string value'()
  {
    when:
    setupGetObject(KEY, 'ala')

    then:
    configuration.getString(KEY) == Optional.of('ala')
    configuration.getStringMandatory(KEY) == 'ala'
  }

  def 'get present string value for integer object'()
  {
    when:
    setupGetObject(KEY, 10)

    then:
    configuration.getString(KEY) == Optional.of('10')
    configuration.getStringMandatory(KEY) == '10'
  }

  def 'non-mandatory get string not present value'()
  {
    when:
    setupGetObject(KEY, null)

    then:
    configuration.getString(KEY) == Optional.empty()
  }

  def 'mandatory get string not present value'()
  {
    when:
    setupGetObject(KEY, null)
    configuration.getStringMandatory(KEY)

    then:
    def e = thrown(IllegalStateException)
    e.message == 'could not find value for key a.b.c'
  }

  def 'mandatory get string not present value with message'()
  {
    when:
    setupGetObject(KEY, null)
    configuration.getStringMandatory(KEY, 'damn, no key')

    then:
    def e = thrown(IllegalStateException)
    e.message == 'damn, no key'
  }

  def 'test list key prefixes'()
  {
    when:
    configuration.listKeys() >> [
            'a.b.c',
            'a.b.d',
            'b',
            'b.a.c.d',
    ]

    then:
    configuration.listKeyPrefixes(2) == ['a.b', 'b', 'b.a'] as Set
  }

  private void setupGetObject(String key, Object value)
  {
    configuration.get(key) >> Optional.ofNullable(value)
  }
}
