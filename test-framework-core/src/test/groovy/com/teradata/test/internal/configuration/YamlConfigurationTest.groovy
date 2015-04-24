/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import spock.lang.Specification

class YamlConfigurationTest
        extends Specification
{
  def static final ENV_VARIABLE_KEY = System.getenv().keySet().iterator().next()


  def replaceEnvVariables()
  {
    setup:
    def configuration = new YamlConfiguration("""\
a:
   b:
      c: ala\${${ENV_VARIABLE_KEY}}
      d: ela\${foo}
x:
   y: 10
""")
    expect:
    configuration.listKeys() == ['a.b.c', 'a.b.d', 'x.y'] as Set
    configuration.getInt('x.y') == Optional.of(10)
    configuration.getString('a.b.c') == Optional.of('ala' + System.getenv().get(ENV_VARIABLE_KEY))
    configuration.getString('a.b.d') == Optional.of('ela${foo}')
    configuration.getString('x.y') == Optional.of('10')
  }

  def replaceSimpleVariables()
  {
    setup:
    def configuration = new YamlConfiguration('''
items:
   who: ala
   verb: ma
   what: kota
   what_alias: ${items.what}
story:
   ${items.who} ${items.verb} ${items.what}
story_with_alias:
   ${items.who} ${items.verb} ${items.what_alias}
''')
    expect:
    configuration.getStringMandatory('story') == "ala ma kota"
    configuration.getStringMandatory('story_with_alias') == "ala ma kota"
  }
}
