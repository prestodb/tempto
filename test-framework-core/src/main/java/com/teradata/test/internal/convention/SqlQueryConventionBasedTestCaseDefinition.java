package com.teradata.test.internal.convention;

import com.google.common.base.MoreObjects;
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

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("testCaseName", testCaseName)
                .add("queryFile", queryFile)
                .add("resultFile", resultFile)
                .add("requirement", requirement)
                .toString();
    }
}
