/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention

import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement
import static AnnotatedFileParser.SectionParsingResult
import static org.apache.commons.io.IOUtils.toInputStream

class AnnotatedFileParserTest
        extends Specification
{
  private AnnotatedFileParser fileParser = new AnnotatedFileParser()

  def 'parse file with comments and properties'()
  {
    String fileContent = '-- property1: value1;\n' +
            '-- property2: value2\n' +
            'content line 1\n' +
            '--- comment line\n' +
            '--- property3: value3\n' +
            'content line 2'
    SectionParsingResult parsingResult = parseOnlySection(fileContent)

    expect:
    parsingResult.getProperty("property1").get() == "value1"
    parsingResult.getProperty("property2").get() == "value2"
    !parsingResult.getProperty("property3").isPresent()
    !parsingResult.getProperty("unknownProperty").isPresent()
    parsingResult.getContentLines().size() == 2
    parsingResult.getContentLines().get(0) == "content line 1"
    parsingResult.getContentLines().get(1) == "content line 2"
    parsingResult.getContentAsSingleLine() == "content line 1 content line 2"
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

  def parseOnlySection(String fileContent)
  {
    getOnlyElement(fileParser.parseFile(toInputStream(fileContent)))
  }
}
