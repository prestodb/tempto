/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention

import com.teradata.test.assertions.QueryAssert
import com.teradata.test.internal.convention.AnnotatedFileParser
import spock.lang.Specification

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

import static com.google.common.collect.Iterables.getOnlyElement
import static AnnotatedFileParser.SectionParsingResult
import static java.sql.JDBCType.*
import static org.apache.commons.io.IOUtils.toInputStream

class SqlResultDescriptorTest
        extends Specification
{

  def 'sampleResultFile'()
  {
    setup:
    String fileContent = '''-- delimiter: |; ignoreOrder: true; types: VARCHAR|BINARY|BIT|INTEGER|REAL|NUMERIC|DATE|TIME|TIMESTAMP
A|true|1|10|20.0|30.0|2015-11-01|10:55:25|2016-11-01 10:55:25|
B|true|1|10|20.0|30.0|2015-11-01|10:55:25|2016-11-01 10:55:25|'''
    SectionParsingResult parsingResult = getOnlyElement(new AnnotatedFileParser().parseFile(toInputStream(fileContent)))
    SqlResultDescriptor resultDescriptor = new SqlResultDescriptor(parsingResult)

    expect:
    resultDescriptor.isIgnoreOrder()
    !resultDescriptor.isJoinAllRowsToOne()
    def expectedTypes = [VARCHAR, BINARY, BIT, INTEGER, REAL, NUMERIC, DATE, TIME, TIMESTAMP]
    resultDescriptor.getExpectedTypes() == Optional.of(expectedTypes)
    List<QueryAssert.Row> rows = resultDescriptor.getRows(expectedTypes)
    rows.size() == 2
    rows.get(0).getValues() == ['A', true, true, 10, Double.valueOf(20.0), new BigDecimal("30.0"), Date.valueOf('2015-11-01'),
                                Time.valueOf('10:55:25'), Timestamp.valueOf('2016-11-01 10:55:25')]
  }

  def 'sampleResultFileWithoutExplicitExpectedTypes'()
  {
    setup:
    String fileContent = '''-- delimiter: |; ignoreOrder: false
A|
B|'''
    SectionParsingResult parsingResult = getOnlyElement(new AnnotatedFileParser().parseFile(toInputStream(fileContent)))
    SqlResultDescriptor resultDescriptor = new SqlResultDescriptor(parsingResult)

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
    SectionParsingResult parsingResult = getOnlyElement(new AnnotatedFileParser().parseFile(toInputStream(fileContent)))
    SqlResultDescriptor resultDescriptor = new SqlResultDescriptor(parsingResult)

    expect:
    resultDescriptor.isIgnoreOrder()
    resultDescriptor.isJoinAllRowsToOne()

    def expectedTypes = [VARCHAR]
    resultDescriptor.getExpectedTypes() == Optional.of(expectedTypes)
    def rows = resultDescriptor.getRows(expectedTypes)
    rows.size() == 1
    rows.get(0).getValues() == ['A\nB']
  }
}
