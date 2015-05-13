/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import spock.lang.Shared
import spock.lang.Specification

class ConfigurationVariableResolverTest
        extends Specification
{
  def static final String ENV_VARIABLE_KEY = System.getenv().keySet().iterator().next()

  @Shared
  ConfigurationVariableResolver resolver = new ConfigurationVariableResolver()

  def resolveSystemEnv()
  {
    setup:
    def configuration = new MapConfiguration([
            variable: '${'  + ENV_VARIABLE_KEY + '}'
    ])

    when:
    configuration = resolver.resolve(configuration)

    then:
    configuration.getStringMandatory('variable') == System.getenv(ENV_VARIABLE_KEY)
  }

  def resolveSystemEnvHasHigherPrioryThanFromConfiguration()
  {
    setup:
    def configuration = new MapConfiguration([
            variable: '${'  + ENV_VARIABLE_KEY + '}',
            ENV_VARIABLE_KEY: 'value from configuration'
    ])

    when:
    configuration = resolver.resolve(configuration)

    then:
    configuration.getStringMandatory('variable') == System.getenv(ENV_VARIABLE_KEY)
  }

  def resolveConfigurationVariables()
  {
    setup:
    def configuration = new MapConfiguration([
            items: [
                    who: 'ala',
                    verb: 'ma',
                    what: 'kota',
                    what_alias: '${items.what}'
                    ],
            story: '${items.who} ${items.verb} ${items.what}',
            story_with_alias: '${items.who} ${items.verb} ${items.what_alias}'
    ])

    when:
    configuration = resolver.resolve(configuration)

    then:
    configuration.getStringMandatory('story') == "ala ma kota"
    configuration.getStringMandatory('story_with_alias') == "ala ma kota"
  }

  def unableToResolveWhenCyclicReferences()
  {
    setup:
    def configuration = new MapConfiguration([
            first: '${second}',
            second: '${third}',
            third: '${first}'
    ])

    when:
    resolver.resolve(configuration)

    then:
    def ex = thrown(RuntimeException)
    ex.message == 'Infinite loop in property interpolation of ${first}: first->second->third'
  }

  def unableToResolveUnknownVariables()
  {
    setup:
    def configuration = new MapConfiguration([
            first: '${second}'
    ])

    when:
    resolver.resolve(configuration)

    then:
    configuration.getStringMandatory('first') == '${second}'
  }

  def typesAreNotLost()
  {
    setup:
    def configuration = new MapConfiguration([
            int: 1,
            int_alias: '${int}',
            boolean: false,
            boolean_alias: '${boolean}'
    ])

    when:
    configuration = resolver.resolve(configuration)

    then:
    configuration.getIntMandatory('int') == 1
    configuration.getIntMandatory('int_alias') == 1
    configuration.getBooleanMandatory('boolean') == false
    configuration.getBooleanMandatory('boolean_alias') == false
  }
}
