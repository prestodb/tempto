package com.teradata.test.convention;

import com.teradata.test.Requirement;

import java.io.File;

public class SqlQueryConventionBasedTestCaseDefinition
{
    public final String testCaseName;
    public final File queryFile;
    public final File resultFile;
    public final Requirement requirement;

    public SqlQueryConventionBasedTestCaseDefinition(String testCaseName, File queryFile, File resultFile, Requirement requirement)
    {
        this.testCaseName = testCaseName;
        this.queryFile = queryFile;
        this.resultFile = resultFile;
        this.requirement = requirement;
    }
}
