/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import org.slf4j.Logger;
import org.testng.ITest;
import org.testng.annotations.Test;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTest
        implements ITest
{

    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTest.class);

    private final String testName;
    private final File queryFile;
    private final File resultFile;

    public SqlQueryConventionBasedTest(String testName, File queryFile, File resultFile)
    {
        this.testName = testName;
        this.queryFile = queryFile;
        this.resultFile = resultFile;
    }

    @Test
    public void test()
    {
        // TODO: test
        LOGGER.info("Executing test: {}", getTestName());
    }

    @Override
    public String getTestName()
    {
        return testName + "." + queryFile.getName();
    }
}
