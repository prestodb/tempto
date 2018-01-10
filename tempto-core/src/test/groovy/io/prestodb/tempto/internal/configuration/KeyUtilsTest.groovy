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

import static io.prestodb.tempto.configuration.KeyUtils.getKeyPrefix
import static io.prestodb.tempto.configuration.KeyUtils.joinKey
import static io.prestodb.tempto.configuration.KeyUtils.splitKey

class KeyUtilsTest
        extends Specification
{
    def "test split key"()
    {
        expect:
        splitKey('abc') == ['abc']
        splitKey('a.b.c') == ['a', 'b', 'c']
    }

    def "join key"()
    {
        expect:
        joinKey(['a', 'b', 'c']) == 'a.b.c'
        joinKey(['a', null, 'c']) == 'a.c'
        joinKey([null, 'b', 'c']) == 'b.c'
        joinKey('a', 'b', 'c') == 'a.b.c'
    }

    def "get key prefix"()
    {
        expect:
        getKeyPrefix('a.b.c', 1) == 'a'
        getKeyPrefix('a.b.c', 2) == 'a.b'
        getKeyPrefix('a.b.c', 3) == 'a.b.c'
        getKeyPrefix('a.b.c', 4) == 'a.b.c'
    }
}
