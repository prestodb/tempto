/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention

import spock.lang.Specification

import static org.apache.commons.io.IOUtils.toInputStream

class HeaderFileParserTest
        extends Specification
{
  private HeaderFileParser fileParser = new HeaderFileParser()

  def 'parse file with comment properties'()
  {
    String fileContent = '-- property1: value1; property2: value2\n' +
            'content line 1\n' +
            'content line 2'
    HeaderFileParser.ParsingResult parsingResult = fileParser.parseFile(toInputStream(fileContent))

    expect:
    parsingResult.getProperty("property1").get() == "value1"
    parsingResult.getProperty("property2").get() == "value2"
    !parsingResult.getProperty("unknownProperty").isPresent()
    parsingResult.getContentLines().size() == 2
    parsingResult.getContentLines().get(0) == "content line 1"
    parsingResult.getContentLines().get(1) == "content line 2"
    parsingResult.getContent() == "content line 1 content line 2"
  }

  def 'parse file no comment properties'()
  {
    String fileContent = 'content line 1\n' +
            'content line 2'
    HeaderFileParser.ParsingResult parsingResult = fileParser.parseFile(toInputStream(fileContent))

    expect:
    !parsingResult.getProperty("unknownProperty").isPresent()
    parsingResult.getContentLines().size() == 2
    parsingResult.getContentLines().get(0) == "content line 1"
    parsingResult.getContentLines().get(1) == "content line 2"
    parsingResult.getContent() == "content line 1 content line 2"
  }
}
