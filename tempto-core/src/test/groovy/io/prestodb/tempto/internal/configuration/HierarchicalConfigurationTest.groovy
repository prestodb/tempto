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

import io.prestodb.tempto.configuration.Configuration
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
