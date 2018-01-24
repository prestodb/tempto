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

import static io.prestodb.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration

class MapConfigurationTest
        extends Specification
{
    def configuration = new MapConfiguration(
            [
                    'a'    : [
                            'b'    :
                                    [
                                            c: 'ala',
                                            d: 'ela'
                                    ],
                            'e'    : 'tola',
                            'list1': ['element1', 'element2', 'element3']
                    ],
                    'x'    : [
                            'y': 10
                    ],
                    'list2': ['element1', 'element2', 'element3']
            ]
    )

    def 'test list keys'()
    {
        expect:
        configuration.listKeys() == ['a.b.c', 'a.b.d', 'a.e', 'x.y', 'list2', 'a.list1'] as Set
    }

    def 'test get objects'()
    {
        expect:
        configuration.get('a.b.c') == Optional.of('ala')
        configuration.get('a.b.d') == Optional.of('ela')
        configuration.get('x.y') == Optional.of(10)
        configuration.get('x.y.x') == Optional.empty()
        configuration.get('a.list1') == Optional.of(['element1', 'element2', 'element3'])
        configuration.get('list2') == Optional.of(['element1', 'element2', 'element3'])
    }

    def 'test subconfiguration'()
    {
        setup:
        def subConfigurationA = configuration.getSubconfiguration('a')
        def subConfigurationAB = configuration.getSubconfiguration('a.b')
        def subConfigurationX = configuration.getSubconfiguration('x')
        def subConfigurationXY = configuration.getSubconfiguration('x.y')

        expect:
        subConfigurationA.listKeys() == ['b.c', 'b.d', 'e', 'list1'] as Set
        subConfigurationAB.listKeys() == ['c', 'd'] as Set
        subConfigurationA.getString('b.c') == Optional.of('ala')
        subConfigurationA.getString('b.d') == Optional.of('ela')
        subConfigurationAB.getString('c') == Optional.of('ala')
        subConfigurationAB.getString('d') == Optional.of('ela')
        subConfigurationA.listKeyPrefixes(1) == ['b', 'e', 'list1'] as Set
        subConfigurationX.listKeyPrefixes(1) == ['y'] as Set
        subConfigurationX.getSubconfiguration('y') == emptyConfiguration()
        subConfigurationXY.listKeyPrefixes(1) == [] as Set
        subConfigurationA.getStringList('list1') == ['element1', 'element2', 'element3']
    }
}
