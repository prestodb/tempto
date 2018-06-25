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

package io.prestodb.tempto.convention

import io.prestodb.tempto.assertions.QueryAssert
import io.prestodb.tempto.internal.convention.AnnotatedFileParser
import io.prestodb.tempto.internal.convention.AnnotatedFileParser.SectionParsingResult
import io.prestodb.tempto.internal.convention.SqlResultDescriptor
import org.joda.time.DateTimeZone
import spock.lang.Specification

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

import static com.google.common.collect.Iterables.getOnlyElement
import static java.sql.JDBCType.BINARY
import static java.sql.JDBCType.BIT
import static java.sql.JDBCType.DATE
import static java.sql.JDBCType.INTEGER
import static java.sql.JDBCType.NUMERIC
import static java.sql.JDBCType.REAL
import static java.sql.JDBCType.TIME
import static java.sql.JDBCType.TIMESTAMP
import static java.sql.JDBCType.VARCHAR
import static org.apache.commons.io.IOUtils.toInputStream

class SqlResultDescriptorTest
        extends Specification
{
    def 'sampleResultFile'()
    {
        setup:
        String fileContent = '''-- delimiter: |; ignoreOrder: true; types: VARCHAR|BINARY|BIT|INTEGER|REAL|NUMERIC|DATE|TIME|TIMESTAMP
A|abcd|1|10|20.0|30.0|2015-11-01|10:55:25|2016-11-01 10:55:25|
B|abcd|1|10|20.0|30.0|2015-11-01|10:55:25|2016-11-01 10:55:25|'''
        SqlResultDescriptor resultDescriptor = parse(fileContent)

        expect:
        resultDescriptor.isIgnoreOrder()
        !resultDescriptor.isJoinAllRowsToOne()
        def expectedTypes = [VARCHAR, BINARY, BIT, INTEGER, REAL, NUMERIC, DATE, TIME, TIMESTAMP]
        resultDescriptor.getExpectedTypes() == Optional.of(expectedTypes)
        List<QueryAssert.Row> rows = resultDescriptor.getRows(expectedTypes)
        rows.size() == 2
        rows.get(0).getValues() == ['A', [0xab, 0xcd] as byte[], true, 10, Double.valueOf(20.0), new BigDecimal("30.0"), Date.valueOf('2015-11-01'),
                                    Time.valueOf('10:55:25'), Timestamp.valueOf('2016-11-01 10:55:25')]
    }

    def 'sampleResultFileWithoutExplicitExpectedTypes'()
    {
        setup:
        String fileContent = '''-- delimiter: |; ignoreOrder: false
A|
B|'''
        SqlResultDescriptor resultDescriptor = parse(fileContent)

        expect:
        !resultDescriptor.isIgnoreOrder()
        !resultDescriptor.isJoinAllRowsToOne()
        !resultDescriptor.getExpectedTypes().isPresent()
        List<QueryAssert.Row> rows = resultDescriptor.getRows([VARCHAR])
        rows.size() == 2
        rows.get(0).getValues() == ['A']
        rows.get(1).getValues() == ['B']
    }

    def 'joinAllRowsToOneFile'()
    {
        setup:
        String fileContent = '''-- delimiter: |; ignoreOrder: true; joinAllRowsToOne: true; types: VARCHAR
A|
B|'''
        SqlResultDescriptor resultDescriptor = parse(fileContent)

        expect:
        resultDescriptor.isIgnoreOrder()
        resultDescriptor.isJoinAllRowsToOne()

        def expectedTypes = [VARCHAR]
        resultDescriptor.getExpectedTypes() == Optional.of(expectedTypes)
        def rows = resultDescriptor.getRows(expectedTypes)
        rows.size() == 1
        rows.get(0).getValues() == ['A\nB']
    }

    def 'record with empty lines'()
    {
        setup:

        String fileContent = "-- delimiter: |; joinAllRowsToOne: true; types: VARCHAR\n" +
                'This\nrecord\n\\\nhas\n\\\n\\\nempty\n\\\n\\\n\\\nlines\n\\\n|'
        SqlResultDescriptor resultDescriptor = parse(fileContent)

        expect:
        resultDescriptor.isJoinAllRowsToOne()

        def expectedTypes = [VARCHAR]
        resultDescriptor.getExpectedTypes() == Optional.of(expectedTypes)
        def rows = resultDescriptor.getRows(expectedTypes)
        rows.size() == 1
        rows.get(0).getValues() == ['This\nrecord\n\nhas\n\n\nempty\n\n\n\nlines\n\n'] \

    }

    private SqlResultDescriptor parse(String fileContent)
    {
        SectionParsingResult parsingResult = getOnlyElement(new AnnotatedFileParser().parseFile(toInputStream(fileContent)))
        return new SqlResultDescriptor(parsingResult)
    }
}
