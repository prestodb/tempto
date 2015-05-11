/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import com.teradata.test.configuration.Configuration
import spock.lang.Specification

import static com.google.common.collect.Sets.newHashSet

class HierarchicalConfigurationTest
        extends Specification
{

  def testHierarchicalConfiguration()
  {
    setup:
    Configuration a = new MapConfiguration([
            a  : 'a',
            ab : 'a',
            ac : 'a',
            abc: 'a',
            sub: [ac: 'a']
    ])

    Configuration b = new MapConfiguration([
            b  : 'b',
            ab : 'b',
            bc : 'b',
            abc: 'b'
    ])

    Configuration c = new MapConfiguration([
            c  : 'c',
            ac : 'c',
            bc : 'c',
            abc: 'c',
            sub: [ac: 'c']

    ])

    when:
    HierarchicalConfiguration configuration = new HierarchicalConfiguration(a, b, c)

    then:
    configuration.getStringMandatory('a') == 'a'
    configuration.getStringMandatory('b') == 'b'
    configuration.getStringMandatory('c') == 'c'
    configuration.getStringMandatory('ab') == 'b'
    configuration.getStringMandatory('ac') == 'c'
    configuration.getStringMandatory('bc') == 'c'
    configuration.getStringMandatory('abc') == 'c'
    configuration.getStringMandatory('sub.ac') == 'c'

    configuration.getSubconfiguration('sub').getStringMandatory('ac') == 'c'

    configuration.listKeys() == newHashSet('a', 'b', 'c', 'ab', 'ac', 'bc', 'abc', 'sub.ac')

  }
}
