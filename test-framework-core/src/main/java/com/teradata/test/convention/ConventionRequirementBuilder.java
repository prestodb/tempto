/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import com.teradata.test.Requirement;
import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.teradata.test.fulfillment.hive.HiveTableDefinition.hiveTableDefinition;

public class ConventionRequirementBuilder
{

    public Requirement hiveTableRequirementFor(ConventionTableDefinition tableDefinition)
    {
        DataSource dataSource = new FileBasedDataSource(tableDefinition);
        HiveTableDefinition hiveTableDefinition = hiveTableDefinition(tableDefinition.getName(), createTableDDLTemplate(tableDefinition), dataSource);
        return new ImmutableHiveTableRequirement(hiveTableDefinition);
    }

    private String createTableDDLTemplate(ConventionTableDefinition conventionTableDefinition)
    {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(conventionTableDefinition.getDdlFile()))) {
            return new FileParser().parseFile(inputStream).getContent();
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not open ddl file: " + conventionTableDefinition.getDdlFile());
        }
    }
}
