/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.teradata.test.Requirement;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement;
import com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions;
import com.teradata.test.internal.convention.SqlTestsFileUtils.ExtensionFileCollectorVisitor;
import org.slf4j.Logger;
import org.testng.annotations.Factory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.internal.convention.ConventionRequirements.hiveTableRequirementFor;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.changeExtension;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.extensionFileCollectorVisitor;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.trimExtension;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTestFactory
{

    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTestFactory.class);
    private static final Object[] NO_TEST_CASES = new Object[0];

    public static final String TEST_FILE_EXTENSION = "sql";
    public static final String RESULT_FILE_EXTENSION = "result";

    public static final String BEFORE_SCRIPT_NAME = "before";
    public static final String AFTER_SCRIPT_NAME = "after";

    public static final String SQL_TESTS_PATH_PART = "sql-tests";
    public static final String DATASETS_PATH_PART = "datasets";
    public static final String TESTCASES_PATH_PART = "testcases";

    @Factory
    public Object[] createTestCases()
    {
        LOGGER.debug("Loading file based test cases");

        try {
            Optional<Path> productTestPath = getProductTestPath();
            if (!productTestPath.isPresent()) {
                return NO_TEST_CASES;
            }

            List<ConventionTableDefinition> conventionTableDefinitions = loadDataSets(productTestPath.get());
            return buildTestCases(productTestPath.get(), conventionTableDefinitions);
        }
        catch (Exception e) {
            LOGGER.error("Could not create file test", e);
            throw new RuntimeException("Could not create test cases", e);
        }
    }

    private Optional<Path> getProductTestPath()
            throws URISyntaxException
    {
        URL productTestURI = ClassLoader.getSystemResource(SQL_TESTS_PATH_PART);
        if (productTestURI == null) {
            return Optional.empty();
        }

        return Optional.of(Paths.get(productTestURI.toURI()));
    }

    private List<ConventionTableDefinition> loadDataSets(Path productTestPath)
            throws IOException
    {
        Path dataSetsPath = productTestPath.resolve(DATASETS_PATH_PART);

        if (dataSetsPath.toFile().exists()) {
            LOGGER.debug("Data sets configuration for path: {}", dataSetsPath);

            return StreamSupport.stream(newDirectoryStream(dataSetsPath, "*.ddl").spliterator(), false)
                    .map(ddlFilePath -> ddlFilePath.toFile())
                    .map(ddlFile -> new ConventionTableDefinition(ddlFile, changeExtension(ddlFile, "data"), changeExtension(ddlFile, "data-revision")))
                    .collect(Collectors.toList());
        }
        else {
            return emptyList();
        }
    }

    private Object[] buildTestCases(Path productTestPath, List<ConventionTableDefinition> conventionTableDefinitions)
            throws IOException
    {
        Path testCasesDirPath = productTestPath.resolve(TESTCASES_PATH_PART);

        if (testCasesDirPath.toFile().exists()) {
            ExtensionFileCollectorVisitor sqlFileCollector = extensionFileCollectorVisitor(TEST_FILE_EXTENSION);
            Files.walkFileTree(testCasesDirPath, sqlFileCollector);

            return sqlFileCollector.getResult().stream()
                    .map(testMethodPath -> createFileConventionTest(testMethodPath, conventionTableDefinitions))
                    .toArray();
        }
        else {
            return NO_TEST_CASES;
        }
    }

    private SqlQueryConventionBasedTest createFileConventionTest(Path testMethodPath, List<ConventionTableDefinition> conventionTableDefinitions)
    {
        File testMethodFile = testMethodPath.toFile();
        File testMethodResult = changeExtension(testMethodFile, RESULT_FILE_EXTENSION);

        checkState(testMethodFile.exists() && testMethodFile.isFile(), "Could not find file: %s", testMethodFile.getAbsolutePath());
        checkState(testMethodResult.exists() && testMethodResult.isFile(), "Could not find file: %s", testMethodResult.getAbsolutePath());
        checkState(testMethodPath.getParent().getParent().getFileName().toString().equals(TESTCASES_PATH_PART),
                "Invalid file structure for file, is: %s, should be: .../{}/{}/{}",
                testMethodResult.getAbsolutePath(), SQL_TESTS_PATH_PART, TESTCASES_PATH_PART, testMethodFile.getName());

        File beforeScriptFile = testMethodPath.getParent().resolve(BEFORE_SCRIPT_NAME).toFile();
        Optional<File> optionalBeforeScriptFile = beforeScriptFile.isFile() ? Optional.of(beforeScriptFile) : Optional.<File>empty();

        File afterScripFile = testMethodPath.getParent().resolve(AFTER_SCRIPT_NAME).toFile();
        Optional<File> optionalAfterScriptFile = afterScripFile.isFile() ? Optional.of(afterScripFile) : Optional.<File>empty();

        String testName = buildTestName(testMethodFile);
        Requirement requirement = getRequirements(conventionTableDefinitions);
        return new SqlQueryConventionBasedTest(testName, optionalBeforeScriptFile, optionalAfterScriptFile, testMethodFile, testMethodResult, requirement);
    }

    private String buildTestName(File testMethodFile)
    {
        return "sql_query_test." + testMethodFile.getParentFile().getName() + "." + trimExtension(testMethodFile.getName());
    }

    private Requirement getRequirements(List<ConventionTableDefinition> conventionTableDefinitions)
    {
        // TODO: requirements need to be based on actual convention based requirements
        List<Requirement> requirements = newArrayList();
        for (HiveTableDefinition tpchHiveTableDefinition : TpchTableDefinitions.TABLES) {
            requirements.add(new ImmutableHiveTableRequirement(tpchHiveTableDefinition));
        }
        for (ConventionTableDefinition conventionTableDefinition : conventionTableDefinitions) {
            Requirement dataSetRequirement = hiveTableRequirementFor(conventionTableDefinition);
            requirements.add(dataSetRequirement);
        }
        return compose(requirements);
    }
}
