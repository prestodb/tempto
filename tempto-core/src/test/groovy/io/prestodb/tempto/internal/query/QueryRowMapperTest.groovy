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
package io.prestodb.tempto.internal.query

import com.google.common.collect.ImmutableList
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.JDBCType

class QueryRowMapperTest
        extends Specification
{
    @Unroll
    def 'convert binary #value'()
    {
        setup:
        QueryRowMapper rowMapper = new QueryRowMapper(ImmutableList.of(JDBCType.BINARY))

        expect:
        rowMapper.mapToRow(ImmutableList.of(value)).getValues()[0] == expected

        where:
        value  | expected
        '0000' | bytes(0x00, 0x00)
        '0ab0' | bytes(0x0a, 0xb0)
    }

    def 'should fail when incorrect hex'()
    {
        setup:
        QueryRowMapper rowMapper = new QueryRowMapper(ImmutableList.of(JDBCType.BINARY))

        when:
        rowMapper.mapToRow(ImmutableList.of('1a0'))

        then:
        thrown(IllegalArgumentException)
    }

    private byte[] bytes(int ... bytes)
    {
        return bytes
    }
}
