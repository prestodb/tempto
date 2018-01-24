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

package io.prestodb.tempto.assertions

import com.google.common.collect.HashBiMap
import io.prestodb.tempto.internal.convention.AnnotatedFileParser
import io.prestodb.tempto.internal.convention.AnnotatedFileParser.SectionParsingResult
import io.prestodb.tempto.internal.convention.SqlResultDescriptor
import io.prestodb.tempto.query.QueryExecutionException
import io.prestodb.tempto.query.QueryResult
import org.assertj.core.api.AbstractListAssert
import spock.lang.Specification

import java.sql.ResultSet

import static com.google.common.collect.Iterables.getOnlyElement
import static io.prestodb.tempto.assertions.QueryAssert.Row.row
import static io.prestodb.tempto.assertions.QueryAssert.anyOf
import static io.prestodb.tempto.assertions.QueryAssert.assertThat
import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.TEST_CONFIGURATION_URIS_KEY
import static java.sql.JDBCType.BIGINT
import static java.sql.JDBCType.INTEGER
import static java.sql.JDBCType.VARCHAR

class QueryAssertTest
        extends Specification
{
    private final QueryResult NATION_JOIN_REGION_QUERY_RESULT = new QueryResult(
            [BIGINT, VARCHAR, VARCHAR],
            HashBiMap.create([
                    "n.nationkey": 1,
                    "n.name"     : 2,
                    "r.name"     : 3
            ]),
            [
                    [1, "ALGERIA", "AFRICA"],
                    [2, "ARGENTINA", "SOUTH AMERICA"]
            ], Optional.of(Mock(ResultSet)));

    private final ColumnValuesAssert EMPTY_COLUMN_VALUE_ASSERT = new ColumnValuesAssert<Object>() {
        @Override
        void assertColumnValues(AbstractListAssert columnAssert)
        {
            // DO NOTHING
        }
    }

    def setupSpec()
    {
        System.setProperty(TEST_CONFIGURATION_URIS_KEY, "/configuration/global-configuration-tempto.yaml");
    }

    def 'hasResultCount fails'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasRowsCount(3)

        then:
        def e = thrown(AssertionError)
        e.message.startsWith('Expected row count to be <3>, but was <2>; rows=')
    }

    def 'hasResultCount'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasRowsCount(2)

        then:
        noExceptionThrown()
    }

    def 'hasAnyRows - correct'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasAnyRows()

        then:
        noExceptionThrown()
    }

    def 'hasNoRows - fails'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasNoRows()

        then:
        def e = thrown(AssertionError)
        e.message.startsWith('Expected row count to be <0>, but was <2>; rows=')
    }

    def 'extractingColumn fails - no such column index'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .column(30, INTEGER, EMPTY_COLUMN_VALUE_ASSERT)

        then:
        def e = thrown(AssertionError)
        e.message == 'Result contains only <3> columns, extracting column <30>'
    }

    def 'extractingColumn fails - no such column name'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .column('unknown_column', INTEGER, EMPTY_COLUMN_VALUE_ASSERT)

        then:
        def e = thrown(AssertionError)
        e.message == 'No column with name: <unknown_column>'
    }

    def 'extractingColumn fails - invalid type'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .column('n.nationkey', VARCHAR, EMPTY_COLUMN_VALUE_ASSERT)

        then:
        def e = thrown(AssertionError)
        e.message == 'Expected <1> column, to be type: <VARCHAR>, but was: <BIGINT>'
    }

    def 'hasColumnCount with index'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .column(1, BIGINT, EMPTY_COLUMN_VALUE_ASSERT)

        then:
        noExceptionThrown()
    }

    def 'hasColumnCount with name'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .column('n.nationkey', BIGINT, EMPTY_COLUMN_VALUE_ASSERT)

        then:
        noExceptionThrown()
    }

    def 'hasColumns - wrong column count'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasColumns(VARCHAR)

        then:
        def e = thrown(AssertionError)
        e.message == 'Expected column count to be <1>, but was <3> - columns <[BIGINT, VARCHAR, VARCHAR]>'
    }

    def 'hasColumns - different column types'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasColumns(VARCHAR, VARCHAR, VARCHAR)

        then:
        def e = thrown(AssertionError)
        e.message == 'Expected <0> column of type <VARCHAR>, but was <BIGINT>, actual columns: [BIGINT, VARCHAR, VARCHAR]'
    }

    def 'hasColumns'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).hasColumns(BIGINT, VARCHAR, VARCHAR)

        then:
        noExceptionThrown()
    }

    def 'hasRows - different number of rows'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .contains(
                row(1, "ALGERIA", "AFRICA"),
                row(2, "ARGENTINA", "SOUTH AMERICA")
        )

        then:
        noExceptionThrown()
    }

    def 'hasRows - different value - no row'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .contains(
                row(2, "ARGENTINA", "SOUTH AMERICA"),
                row(1, "ALGERIA", "valid_value"),
        )

        then:
        def e = thrown(AssertionError)
        e.message == 'Could not find rows:\n' +
                '[1, ALGERIA, valid_value]\n' +
                '\n' +
                'actual rows:\n' +
                '[1, ALGERIA, AFRICA]\n' +
                '[2, ARGENTINA, SOUTH AMERICA]'
    }

    def 'hasRows'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .contains(
                row(2, "ARGENTINA", "SOUTH AMERICA"),
                row(1, "ALGERIA", "AFRICA"),
        )

        then:
        noExceptionThrown()
    }

    def 'hasRows - missing suffix column'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .contains(
                row(2, "ARGENTINA"),
                row(1, "ALGERIA"),
        )

        then:
        def e = thrown(AssertionError)
        e.message == 'Could not find rows:\n' +
                '[2, ARGENTINA]\n' +
                '[1, ALGERIA]\n' +
                '\n' +
                'actual rows:\n' +
                '[1, ALGERIA, AFRICA]\n' +
                '[2, ARGENTINA, SOUTH AMERICA]'
    }

    def 'hasRows - missing middle column'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .contains(
                row(2, "SOUTH AMERICA"),
                row(1, "AFRICA"),
        )

        then:
        def e = thrown(AssertionError)
        e.message == 'Could not find rows:\n' +
                '[2, SOUTH AMERICA]\n' +
                '[1, AFRICA]\n' +
                '\n' +
                'actual rows:\n' +
                '[1, ALGERIA, AFRICA]\n' +
                '[2, ARGENTINA, SOUTH AMERICA]'
    }

    def 'hasRows with multiple possible values'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .contains(
                row(2, "ARGENTINA", "SOUTH AMERICA"),
                row(1, "ALGERIA", anyOf("AFRICA", "MARS")),
        )

        then:
        noExceptionThrown()
    }

    def 'hasRows with multiple possible values - no row matching'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .contains(
                row(2, "ARGENTINA", "SOUTH AMERICA"),
                row(1, "ALGERIA", anyOf("SATURN", null)),
        )

        then:
        def e = thrown(AssertionError)
        e.message == 'Could not find rows:\n' +
                '[1, ALGERIA, anyOf(SATURN, null)]\n' +
                '\n' +
                'actual rows:\n' +
                '[1, ALGERIA, AFRICA]\n' +
                '[2, ARGENTINA, SOUTH AMERICA]'
    }

    def 'hasRowsInOrder - different number of rows'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .containsExactly(
                row(1, "ALGERIA", "AFRICA"),
                row(2, "ARGENTINA", "SOUTH AMERICA"),
                row(3, "AUSTRIA", "EUROPE")
        )

        then:
        def e = thrown(AssertionError)
        e.message.startsWith('Expected row count to be <3>, but was <2>; rows=')
    }

    def 'hasRowsInOrder - different value - no row'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .containsExactly(
                row(1, "ALGERIA", "AFRICA"),
                row(2, "ARGENTINA", "valid_value")
        )

        then:
        def e = thrown(AssertionError)
        e.message == 'Not equal rows:\n' +
                '1 - expected: 2|ARGENTINA|valid_value|\n1 - actual:   2|ARGENTINA|SOUTH AMERICA|'
    }

    def 'hasRowsInOrder - different order'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .containsExactly(
                row(2, "ARGENTINA", "SOUTH AMERICA"),
                row(1, "ALGERIA", "AFRICA")
        )

        then:
        def e = thrown(AssertionError)
        e.message == 'Not equal rows:\n' +
                '0 - expected: 2|ARGENTINA|SOUTH AMERICA|\n0 - actual:   1|ALGERIA|AFRICA|\n' +
                '1 - expected: 1|ALGERIA|AFRICA|\n1 - actual:   2|ARGENTINA|SOUTH AMERICA|'
    }

    def 'hasRowsInOrder'()
    {
        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT)
                .containsExactly(
                row(1, "ALGERIA", "AFRICA"),
                row(2, "ARGENTINA", "SOUTH AMERICA")
        )

        then:
        noExceptionThrown()
    }

    def 'Matches file - ok - with types'()
    {
        def parsingResult = parseResultFor('''\
-- delimiter: |; ignoreOrder: false; types: BIGINT|VARCHAR|VARCHAR
1|ALGERIA|AFRICA|
2|ARGENTINA|SOUTH AMERICA|
''')

        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).matches(new SqlResultDescriptor(parsingResult))

        then:
        noExceptionThrown()
    }

    def 'Matches file - failed - wrong explicit types in result file'()
    {
        def parsingResult = parseResultFor('''\
-- delimiter: |; ignoreOrder: false; types: BIGINT|BIGINT|BIGINT
1|ALGERIA|AFRICA|
2|ARGENTINA|SOUTH AMERICA|
''')

        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).matches(new SqlResultDescriptor(parsingResult))

        then:
        def e = thrown(AssertionError.class);
        e.getMessage() == "Expected <1> column of type <BIGINT>, but was <VARCHAR>, actual columns: [BIGINT, VARCHAR, VARCHAR]"
    }

    def 'Matches file - ok - no explicit types'()
    {
        def parsingResult = parseResultFor('''\
-- delimiter: |; ignoreOrder: false
1|ALGERIA|AFRICA|
2|ARGENTINA|SOUTH AMERICA|
''')

        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).matches(new SqlResultDescriptor(parsingResult))

        then:
        noExceptionThrown()
    }

    def 'Matches file - failed - wrong value'()
    {
        def parsingResult = parseResultFor('''\
-- delimiter: |; ignoreOrder: false
1|ALGERIA|AFRICA|
3|ARGENTINA|SOUTH AMERICA|
''')

        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).matches(new SqlResultDescriptor(parsingResult))

        then:
        def e = thrown(AssertionError.class)
        e.getMessage() == '''\
Not equal rows:
1 - expected: 3|ARGENTINA|SOUTH AMERICA|
1 - actual:   2|ARGENTINA|SOUTH AMERICA|'''
    }

    def 'Matches file - failed - cannot map expected result to types from db result'()
    {
        def parsingResult = parseResultFor('''\
-- delimiter: |; ignoreOrder: false
A|ALGERIA|AFRICA|
B|ARGENTINA|SOUTH AMERICA|
''')

        when:
        assertThat(NATION_JOIN_REGION_QUERY_RESULT).matches(new SqlResultDescriptor(parsingResult))

        then:
        def e = thrown(AssertionError.class)
        e.getMessage() == '''Could not map expected file content to query column types; types=[BIGINT, VARCHAR, VARCHAR]; content=<-- delimiter: |; ignoreOrder: false
A|ALGERIA|AFRICA|
B|ARGENTINA|SOUTH AMERICA|>; error=<For input string: "A">'''
    }

    private SectionParsingResult parseResultFor(String fileContent)
    {
        getOnlyElement(new AnnotatedFileParser().parseFile(new ByteArrayInputStream(fileContent.getBytes())));
    }

    def 'QueryExecutionAssert - not fail as expected'()
    {
        when:
        assertThat({ return null }).failsWithMessage("dummy")

        then:
        def e = thrown(AssertionError)
        e.message == "Query did not fail as expected."
    }

    def 'QueryExecutionAssert - wrong error message'()
    {
        when:
        assertThat({ throw new QueryExecutionException(new RuntimeException("foo bar")) }).failsWithMessage("dummy")

        then:
        def e = thrown(AssertionError)
        e.message == "Query failed with unexpected error message: 'java.lang.RuntimeException: foo bar' \n" +
                " Expected error message to contain 'dummy'"
    }

    def 'QueryExecutionAssert - right error message'()
    {
        when:
        assertThat({ throw new QueryExecutionException(new RuntimeException("dummy")) }).failsWithMessage("dummy")

        then:
        noExceptionThrown()
    }

    def 'QueryExecutionAssert - error message does not match'()
    {
        when:
        assertThat({ throw new QueryExecutionException(new RuntimeException("foo bar")) }).failsWithMessageMatching("foo")

        then:
        def e = thrown(AssertionError)
        e.message == "Query failed with unexpected error message: 'java.lang.RuntimeException: foo bar' \n" +
                " Expected error message to match 'foo'"
    }

    def 'QueryExecutionAssert - error message matches'()
    {
        when:
        assertThat({ throw new QueryExecutionException(new RuntimeException("dummy")) }).failsWithMessageMatching("^java.lang.RuntimeException: dug?(m){2,4}y\$")

        then:
        noExceptionThrown()
    }
}
