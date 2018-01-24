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

    def 'get string list'()
    {
        when:
        setupGetObject(KEY, ['a', 'b'])

        then:
        configuration.getStringList(KEY) == ['a', 'b']
    }

    def 'mandatory get string list with no value with message'()
    {
        when:
        setupGetObject(KEY, null)
        configuration.getStringListMandatory(KEY, 'damn, no key')

        then:
        def e = thrown(IllegalStateException)
        e.message == 'damn, no key'
    }

    def 'mandatory get string list with no value'()
    {
        when:
        setupGetObject(KEY, null)
        configuration.getStringListMandatory(KEY)

        then:
        def e = thrown(IllegalStateException)
        e.message == 'could not find value for key a.b.c'
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
