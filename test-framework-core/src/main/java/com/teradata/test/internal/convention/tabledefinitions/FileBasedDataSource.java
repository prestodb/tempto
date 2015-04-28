/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.tabledefinitions;

import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient.RepeatableContentProducer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.singleton;

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
        Path dataFile = conventionTableDefinitionDescriptor.getDataFile();

        if (!exists(dataFile)) {
            throw new IllegalStateException("Data file " + conventionTableDefinitionDescriptor.getDataFile() + " should exist");
        }

        return singleton(() -> newInputStream(dataFile));
    }

    @Override
    public String revisionMarker()
    {
        try {
            if (revisionMarker == null) {
                revisionMarker = new String(readAllBytes(conventionTableDefinitionDescriptor.getRevisionFile()));
            }
            return revisionMarker;
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read revision file: " + conventionTableDefinitionDescriptor.getRevisionFile());
        }
    }
}
