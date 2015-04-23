/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.tabledefinitions;

import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient.RepeatableContentProducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.Collections.singleton;
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
    public String getPathSuffix()
    {
        // {TESTS_PATH}/datasets/{dataSetName}
        return format("datasets/%s", conventionTableDefinitionDescriptor.getName());
    }

    @Override
    public Collection<RepeatableContentProducer> data()
    {
        File dataFile = conventionTableDefinitionDescriptor.getDataFile();

        if (!dataFile.exists()) {
            throw new IllegalStateException("Data file " + conventionTableDefinitionDescriptor.getDataFile() + " should exist");
        }

        return singleton(() -> new FileInputStream(dataFile));
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
