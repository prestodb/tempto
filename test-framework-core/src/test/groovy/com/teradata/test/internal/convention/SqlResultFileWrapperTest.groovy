/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention

import spock.lang.Specification

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

import static java.sql.JDBCType.*
import static org.apache.commons.io.IOUtils.toInputStream

class SqlResultFileWrapperTest
        extends Specification
{

  def 'sampleResultFile'()
  {
    setup:
    String fileContent = '-- delimiter: |; ignoreOrder: true; types: VARCHAR|BINARY|BIT|INTEGER|REAL|NUMERIC|DATE|TIME|TIMESTAMP\n' +
            'A|true|1|10|20.0|30.0|2015-11-01|10:55:25|2016-11-01 10:55:25|\n' +
            'B|true|1|10|20.0|30.0|2015-11-01|10:55:25|2016-11-01 10:55:25|'
    HeaderFileParser.ParsingResult parsingResult = new HeaderFileParser().parseFile(toInputStream(fileContent))
    SqlResultFileWrapper resultFileWrapper = new SqlResultFileWrapper(parsingResult)

    expect:
    resultFileWrapper.isIgnoreOrder()
    resultFileWrapper.getTypes() == [VARCHAR, BINARY, BIT, INTEGER, REAL, NUMERIC, DATE, TIME, TIMESTAMP]
    resultFileWrapper.getRows().size() == 2
    resultFileWrapper.getRows().get(0).getValues() == ['A', true, true, 10, Double.valueOf(20.0), new BigDecimal("30.0"), Date.valueOf('2015-11-01'),
                                                       Time.valueOf('10:55:25'), Timestamp.valueOf('2016-11-01 10:55:25')]
  }
}
