/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.sql;

import com.google.common.collect.ImmutableList;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.fulfillment.table.ImmutableTableRequirement;
import com.teradata.test.fulfillment.table.TableDefinitionsRepository;
import com.teradata.test.internal.ReflectionHelper;
import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.ConventionBasedTestFactory;
import com.teradata.test.internal.convention.ConventionBasedTestProxyGenerator;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.internal.convention.SqlQueryFileWrapper.sqlQueryFileWrapperFor;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.changeExtension;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.getExtension;
import static java.util.stream.Collectors.toList;

public class SqlPathTestFactory
        implements ConventionBasedTestFactory.PathTestFactory
{
    private static final String TEST_FILE_EXTENSION = "sql";
    private static final String RESULT_FILE_EXTENSION = "result";
    private static final String BEFORE_SCRIPT_NAME = "before";
    private static final String AFTER_SCRIPT_NAME = "after";

    private final TableDefinitionsRepository tableDefinitionsRepository;
    private final ConventionBasedTestProxyGenerator proxyGenerator;

    public SqlPathTestFactory(TableDefinitionsRepository tableDefinitionsRepository,
            ConventionBasedTestProxyGenerator proxyGenerator)
    {
        this.tableDefinitionsRepository = tableDefinitionsRepository;
        this.proxyGenerator = proxyGenerator;
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

        Requirement requirement = getRequirements(testMethodFile);
        SqlQueryConventionBasedTest conventionTest = new SqlQueryConventionBasedTest(
                optionalBeforeScriptFile, optionalAfterScriptFile,
                testMethodFile, testMethodResult, requirement);
        ConventionBasedTest proxiedConventionTest = proxyGenerator.generateProxy(conventionTest);
        return ImmutableList.of(proxiedConventionTest);
    }

    private Requirement getRequirements(File testMethodFile)
    {
        List<Requirement> requirements = newArrayList();
        requirements.addAll(sqlQueryFileWrapperFor(testMethodFile).getTableDefinitionNames()
                .stream()
                .map(requiredTableName -> new ImmutableTableRequirement(tableDefinitionsRepository.getForName(requiredTableName)))
                .collect(toList()));
        requirements.addAll(sqlQueryFileWrapperFor(testMethodFile).getRequirementClassNames()
                .stream()
                .map(this::getRequirementsFromClass)
                .collect(toList()));
        return compose(requirements);
    }

    private Requirement getRequirementsFromClass(String requirementClassName)
    {
        RequirementsProvider requirementsProvider = ReflectionHelper.instantiate(requirementClassName);
        return requirementsProvider.getRequirements();
    }
}
