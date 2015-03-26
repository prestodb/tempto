/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement;
import org.slf4j.Logger;
import org.testng.annotations.Factory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions.NATION;
import static org.slf4j.LoggerFactory.getLogger;

public class SqlQueryConventionBasedTestFactory
{

    private static final Logger LOGGER = getLogger(SqlQueryConventionBasedTestFactory.class);
    private static final Object[] NO_TEST_CASES = new Object[0];

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

            handleDataSets(productTestPath.get());
            return buildTestCases(productTestPath.get());
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

    private void handleDataSets(Path productTestPath)
    {
        Path dataSetsPath = productTestPath.resolve(DATASETS_PATH_PART);
        if (dataSetsPath.toFile().exists()) {
            LOGGER.debug("Datasets configuration");
            // TODO
        }
    }

    private Object[] buildTestCases(Path productTestPath)
            throws IOException
    {
        Path testCasesDirPath = productTestPath.resolve(TESTCASES_PATH_PART);

        if (testCasesDirPath.toFile().exists()) {
            ExtensionFileCollectorVisitor sqlFileCollector = new ExtensionFileCollectorVisitor("sql");
            Files.walkFileTree(testCasesDirPath, sqlFileCollector);

            return sqlFileCollector.getResult().stream()
                    .map(this::createFileConventionTest)
                    .toArray();
        }
        else {
            return NO_TEST_CASES;
        }
    }

    private SqlQueryConventionBasedTest createFileConventionTest(Path testMethodPath)
    {
        File testMethodFile = testMethodPath.toFile();
        File testMethodResult = changeExtension(testMethodPath, ".result").toFile();

        checkState(testMethodFile.exists() && testMethodFile.isFile(), "Could not find file: %s", testMethodFile.getAbsolutePath());
        checkState(testMethodResult.exists() && testMethodResult.isFile(), "Could not find file: %s", testMethodResult.getAbsolutePath());
        checkState(testMethodPath.getParent().getParent().getFileName().toString().equals(TESTCASES_PATH_PART),
                "Invalid file structure for file, is: %s, should be: .../{}/{}/{}",
                testMethodResult.getAbsolutePath(), SQL_TESTS_PATH_PART, TESTCASES_PATH_PART, testMethodFile.getName());

        String testName = testMethodPath.getParent().getFileName().toString();
        // TODO: requirements need to be based on actual convention based requirements
        List<SqlQueryConventionBasedTestCaseDefinition> testCases = newArrayList(new SqlQueryConventionBasedTestCaseDefinition(testName, testMethodFile, testMethodResult, new ImmutableHiveTableRequirement(NATION)));
        return new SqlQueryConventionBasedTest(testCases);
    }

    private Path changeExtension(Path source, String extension)
    {
        String fileName = source.getFileName().toString();
        String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + extension;
        return source.getParent().resolve(newFileName);
    }

    private static final class ExtensionFileCollectorVisitor
            extends SimpleFileVisitor<Path>
    {

        private final List<Path> result = newArrayList();
        private final String extension;

        public ExtensionFileCollectorVisitor(String extension)
        {
            this.extension = "." + extension;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException
        {
            if (file.getFileName().toString().endsWith(extension)) {
                result.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getResult()
        {
            return result;
        }
    }
}
