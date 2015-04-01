/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.tabledefinitions;

import com.teradata.test.fulfillment.hive.DataSource;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.readFileToString;

public class FileBasedDataSource
        implements DataSource
{

    private final ConventionTableDefinitionDescriptor conventionTableDefinitionDescriptor;
    private String revisionMarker;

    public FileBasedDataSource(ConventionTableDefinitionDescriptor conventionTableDefinitionDescriptor)
    {
        this.conventionTableDefinitionDescriptor = conventionTableDefinitionDescriptor;
    }

    @Override
    public String getPath()
    {
        // {TESTS_PATH}/datasets/{dataSetName}
        String testsPath = testContext().getDependency(String.class, "tests.hdfs.path");
        return format("%s/datasets/%s", testsPath, conventionTableDefinitionDescriptor.getName());
    }

    @Override
    public InputStream data()
    {
        try {
            return new BufferedInputStream(new FileInputStream(conventionTableDefinitionDescriptor.getDataFile()));
        }
        catch (FileNotFoundException e) {
            throw new IllegalStateException("Data file " + conventionTableDefinitionDescriptor.getDdlFile() + " should exist");
        }
    }

    @Override
    public String revisionMarker()
    {
        try {
            if (revisionMarker == null) {
                revisionMarker = readFileToString(conventionTableDefinitionDescriptor.getRevisionFile());
            }
            return revisionMarker;
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read revision file: " + conventionTableDefinitionDescriptor.getRevisionFile());
        }
    }
}
