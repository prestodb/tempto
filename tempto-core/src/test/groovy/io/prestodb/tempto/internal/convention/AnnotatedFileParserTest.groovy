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

package io.prestodb.tempto.internal.convention

import spock.lang.Specification

import static AnnotatedFileParser.SectionParsingResult
import static com.google.common.collect.Iterables.getOnlyElement
import static org.apache.commons.io.IOUtils.toInputStream

class AnnotatedFileParserTest
        extends Specification
{
    private AnnotatedFileParser fileParser = new AnnotatedFileParser()

    def 'parse file with comments, properties and whitespace lines'()
    {
        String fileContent = '-- property1: value1;\n' +
                '-- property2: value2\n' +
                'content line 1\n' +
                '--- comment line\n' +
                '  \n' +  // whitespace line
                '--- property3: value3\n' +
                'content line 2\n' +
                '\\--- contentproperty: x\n' +
                '\\# content comment'
        SectionParsingResult parsingResult = parseOnlySection(fileContent)

        expect:
        parsingResult.getProperty("property1").get() == "value1"
        parsingResult.getProperty("property2").get() == "value2"
        !parsingResult.getProperty("property3").isPresent()
        !parsingResult.getProperty("unknownProperty").isPresent()
        parsingResult.getContentLines().size() == 4
        parsingResult.getContentLines().get(0) == "content line 1"
        parsingResult.getContentLines().get(1) == "content line 2"
        parsingResult.getContentLines().get(2) == "--- contentproperty: x"
        parsingResult.getContentLines().get(3) == "# content comment"
        parsingResult.getContentAsSingleLine() == "content line 1 content line 2 --- contentproperty: x # content comment"
    }

    def 'parse file no comment properties'()
    {
        String fileContent = 'content line 1\n' +
                'content line 2'
        SectionParsingResult parsingResult = parseOnlySection(fileContent)

        expect:
        !parsingResult.getProperty("unknownProperty").isPresent()
        parsingResult.getContentLines().size() == 2
        parsingResult.getContentLines().get(0) == "content line 1"
        parsingResult.getContentLines().get(1) == "content line 2"
        parsingResult.getContentAsSingleLine() == "content line 1 content line 2"
    }

    def 'should fail redundant options'()
    {
        when:
        String fileContent = '-- property1: value\n' +
                '-- property1: value2'
        parseOnlySection(fileContent)

        then:
        def e = thrown(IllegalStateException.class)
        e.getMessage() == 'Different properties:  [{property1=(value, value2)}]'
    }

    def 'parse multi section file'()
    {
        String fileContent = '-- property1: value1; property2: value2; name: section1\n' +
                'content1\n' +
                '--! name: section2\n' +
                '-- property3: value3\n' +
                'content2'
        def sections = fileParser.parseFile(toInputStream(fileContent))

        expect:
        sections.size() == 2
        sections[0].getProperty('property1').get() == 'value1'
        sections[0].getProperty('property2').get() == 'value2'
        sections[0].sectionName.get() == 'section1'
        sections[0].contentLines.size() == 1
        sections[0].contentLines == ['content1']
    }

    def 'handles empty sections'()
    {
        String fileContent = '--! name: section1\n' +
                '--! name: section2'
        def sections = fileParser.parseFile(toInputStream(fileContent))

        expect:
        sections.size() == 2
        sections[0].sectionName.get() == 'section1'
        sections[1].sectionName.get() == 'section2'
    }

    def 'handles line end escapes'()
    {
        String fileContent = '--! name: section1\n' +
                'line\\n1\n' + 
                'line2\n' +
                '--! name: section2'
        def sections = fileParser.parseFile(toInputStream(fileContent))

        expect:
        sections.size() == 2
        sections[0].sectionName.get() == 'section1'
        sections[0].contentLines == ["line\n1", "line2"]
        sections[1].sectionName.get() == 'section2'
    }

    def parseOnlySection(String fileContent)
    {
        getOnlyElement(fileParser.parseFile(toInputStream(fileContent)))
    }
}
