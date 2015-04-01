/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.tabledefinitions;

import java.io.File;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.getFilenameWithoutExtension;

class ConventionTableDefinitionDescriptor
{
    private final String name;
    private final File ddlFile;
    private final File dataFile;
    private final File revisionFile;

    public ConventionTableDefinitionDescriptor(File ddlFile, File dataFile, File revisionFile)
    {
        checkArgument(ddlFile.exists() && ddlFile.isFile(), "Invalid file: %s", ddlFile);
        checkArgument(dataFile.exists() && dataFile.isFile(), "Invalid file: %s", dataFile);
        checkArgument(revisionFile.exists() && revisionFile.isFile(), "Invalid file: %s", revisionFile);

        this.name = getFilenameWithoutExtension(ddlFile);
        this.ddlFile = ddlFile;
        this.dataFile = dataFile;
        this.revisionFile = revisionFile;
    }

    public String getName()
    {
        return name;
    }

    public File getDdlFile()
    {
        return ddlFile;
    }

    public File getDataFile()
    {
        return dataFile;
    }

    public File getRevisionFile()
    {
        return revisionFile;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("name", name)
                .add("ddlFile", ddlFile)
                .add("dataFile", dataFile)
                .add("revisionFile", revisionFile)
                .toString();
    }
}
