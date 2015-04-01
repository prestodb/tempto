/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.sql;

import com.google.common.collect.ImmutableList;
import com.teradata.test.Requirement;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement;
import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.ConventionBasedTestFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.changeExtension;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.getExtension;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.getFilenameWithoutExtension;

public class SqlPathTestFactory
        implements ConventionBasedTestFactory.PathTestFactory
{

    // this is temporary stuff. we need some datasources registry stuff
    private final List<HiveTableDefinition> availableTableDefinitions;

    private static final String TEST_FILE_EXTENSION = "sql";
    private static final String RESULT_FILE_EXTENSION = "result";
    private static final String BEFORE_SCRIPT_NAME = "before";
    private static final String AFTER_SCRIPT_NAME = "after";

    public SqlPathTestFactory(List<HiveTableDefinition> availableTableDefinitions)
    {
        this.availableTableDefinitions = availableTableDefinitions;
    }

    @Override
    public boolean isSupportedPath(Path path)
    {
        return TEST_FILE_EXTENSION.equals(getExtension(path));
    }

    @Override
    public List<ConventionBasedTest> createTestsForPath(Path path, String testNamePrefix, ConventionBasedTestFactory factory)
    {
        File testMethodFile = path.toFile();
        File testMethodResult = changeExtension(testMethodFile, RESULT_FILE_EXTENSION);

        checkState(testMethodFile.exists() && testMethodFile.isFile(), "Could not find file: %s", testMethodFile.getAbsolutePath());
        checkState(testMethodResult.exists() && testMethodResult.isFile(), "Could not find file: %s", testMethodResult.getAbsolutePath());

        File beforeScriptFile = path.getParent().resolve(BEFORE_SCRIPT_NAME).toFile();
        Optional<File> optionalBeforeScriptFile = beforeScriptFile.isFile() ? Optional.of(beforeScriptFile) : Optional.<File>empty();

        File afterScripFile = path.getParent().resolve(AFTER_SCRIPT_NAME).toFile();
        Optional<File> optionalAfterScriptFile = afterScripFile.isFile() ? Optional.of(afterScripFile) : Optional.<File>empty();

        String testName = buildTestName(testNamePrefix, path);
        Requirement requirement = getRequirements(availableTableDefinitions);
        return ImmutableList.of(new SqlQueryConventionBasedTest(
                testName, optionalBeforeScriptFile, optionalAfterScriptFile,
                testMethodFile, testMethodResult, requirement));
    }

    private String buildTestName(String namePrefix, Path testMethodPath)
    {
        return namePrefix + "." + getFilenameWithoutExtension(testMethodPath);
    }

    private Requirement getRequirements(List<HiveTableDefinition> tableDefinitions)
    {
        // TODO: requirements need to be based on actual convention based requirements
        List<Requirement> requirements = newArrayList();
        for (HiveTableDefinition tpchHiveTableDefinition : tableDefinitions) {
            requirements.add(new ImmutableHiveTableRequirement(tpchHiveTableDefinition));
        }
        return compose(requirements);
    }
}
