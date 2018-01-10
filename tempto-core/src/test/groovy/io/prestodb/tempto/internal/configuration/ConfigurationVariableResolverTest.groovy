/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.internal.configuration

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
                variable: '${' + ENV_VARIABLE_KEY + '}'
        ])

        when:
        configuration = resolver.resolve(configuration)

        then:
        configuration.getStringMandatory('variable') == System.getenv(ENV_VARIABLE_KEY)
    }

    def resolveSystemEnvHasHigherPriorityThanFromConfiguration()
    {
        setup:
        def configuration = new MapConfiguration([
                variable          : '${' + ENV_VARIABLE_KEY + '}',
                (ENV_VARIABLE_KEY): 'value from configuration'
        ])

        when:
        configuration = resolver.resolve(configuration)

        then:
        configuration.getStringMandatory('variable') == System.getenv(ENV_VARIABLE_KEY)
    }

    def resolveSystemEnvHasHigherPriorityThanFromSystemProperties()
    {
        System.setProperty(ENV_VARIABLE_KEY, 'value from system properties')
        setup:
        def configuration = new MapConfiguration([
                variable          : '${' + ENV_VARIABLE_KEY + '}',
                (ENV_VARIABLE_KEY): 'value from configuration'
        ])

        when:
        configuration = resolver.resolve(configuration)

        then:
        configuration.getStringMandatory('variable') == System.getenv(ENV_VARIABLE_KEY)
    }

    def resolveSystemPropertiesHasHigherPriorityThanFromConfiguration()
    {
        def key = "SYSTEM_PROPERTY"
        def valueFromSystemProperties = 'value from system properties'
        System.setProperty(key, valueFromSystemProperties)

        setup:
        def configuration = new MapConfiguration([
                variable: '${' + key + '}',
                (key)   : 'value from configuration'
        ])

        when:
        configuration = resolver.resolve(configuration)

        then:
        configuration.getStringMandatory('variable') == valueFromSystemProperties
    }

    def resolveConfigurationVariables()
    {
        setup:
        def configuration = new MapConfiguration([
                items           : [
                        who       : 'ala',
                        verb      : 'ma',
                        what      : 'kota',
                        what_alias: '${items.what}'
                ],
                story           : '${items.who} ${items.verb} ${items.what}',
                story_with_alias: '${items.who} ${items.verb} ${items.what_alias}'
        ])

        when:
        configuration = resolver.resolve(configuration)

        then:
        configuration.getStringMandatory('story') == "ala ma kota"
        configuration.getStringMandatory('story_with_alias') == "ala ma kota"
    }

    def resolveConfigurationListVariables()
    {
        setup:
        def configuration = new MapConfiguration([
                items     : [
                        who : 'ala',
                        verb: 'ma',
                        what: 'kota',
                        int : 1
                ],
                list_alias: ['${items.who}', '${items.what}', '${items.int}']
        ])

        when:
        configuration = resolver.resolve(configuration)

        then:
        configuration.getStringList('list_alias') == ['ala', 'kota', '1']
    }

    def unableToResolveWhenCyclicReferences()
    {
        setup:
        def configuration = new MapConfiguration([
                first : '${second}',
                second: '${third}',
                third : '${first}'
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
                int          : 1,
                int_alias    : '${int}',
                boolean      : false,
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
