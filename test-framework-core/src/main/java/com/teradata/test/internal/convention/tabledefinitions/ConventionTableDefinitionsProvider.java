/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.tabledefinitions;

import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.table.TableDefinitionsRepository;
import com.teradata.test.internal.convention.ConventionBasedTestFactory;
import com.teradata.test.internal.convention.ConventionTestsUtils;
import com.teradata.test.internal.convention.HeaderFileParser;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.teradata.test.fulfillment.hive.HiveTableDefinition.hiveTableDefinition;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.changeExtension;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class ConventionTableDefinitionsProvider
{
    private static final Logger LOGGER = getLogger(ConventionBasedTestFactory.class);
    public static final String DATASETS_PATH_PART = "datasets";

    public void registerConventionTableDefinitions(TableDefinitionsRepository tableDefinitionsRepository)
    {
        getAllConventionBasedTableDefinitions().stream().forEach(tableDefinitionsRepository::register);
    }

    private List<HiveTableDefinition> getAllConventionBasedTableDefinitions()
    {
        return getAllConventionTableDefinitionDescriptors().stream()
                .map(this::hiveTableDefinitionFor)
                .collect(toList());
    }

    private List<ConventionTableDefinitionDescriptor> getAllConventionTableDefinitionDescriptors()
    {
        Optional<Path> dataSetsPath = ConventionTestsUtils.getConventionsTestsPath(DATASETS_PATH_PART);
        if (!dataSetsPath.isPresent()) {
            return emptyList();
        }
        else {
            return getAllConventionTableDefinitionDescriptors(dataSetsPath.get());
        }
    }

    private List<ConventionTableDefinitionDescriptor> getAllConventionTableDefinitionDescriptors(Path dataSetsPath)
    {
        if (dataSetsPath.toFile().exists()) {
            LOGGER.debug("Data sets configuration for path: {}", dataSetsPath);

            try {
                return StreamSupport.stream(newDirectoryStream(dataSetsPath, "*.ddl").spliterator(), false)
                        .map(Path::toFile)
                        .map(ddlFile -> new ConventionTableDefinitionDescriptor(ddlFile, changeExtension(ddlFile, "data"), changeExtension(ddlFile, "data-revision")))
                        .collect(toList());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return emptyList();
        }
    }

    private HiveTableDefinition hiveTableDefinitionFor(ConventionTableDefinitionDescriptor tableDefinition)
    {
        DataSource dataSource = new FileBasedDataSource(tableDefinition);
        return hiveTableDefinition(tableDefinition.getName(), createTableDDLTemplate(tableDefinition), dataSource);
    }

    private String createTableDDLTemplate(ConventionTableDefinitionDescriptor conventionTableDefinitionDescriptor)
    {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(conventionTableDefinitionDescriptor.getDdlFile()))) {
            return new HeaderFileParser().parseFile(inputStream).getContent();
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not open ddl file: " + conventionTableDefinitionDescriptor.getDdlFile());
        }
    }
}
