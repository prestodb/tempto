/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.internal.convention.sql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.RequirementsProvider;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.fulfillment.table.ImmutableTableRequirement;
import io.prestodb.tempto.fulfillment.table.MutableTableRequirement;
import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableDefinitionsRepository;
import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.internal.ReflectionHelper;
import io.prestodb.tempto.internal.convention.AnnotatedFileParser;
import io.prestodb.tempto.internal.convention.AnnotatedFileParser.SectionParsingResult;
import io.prestodb.tempto.internal.convention.ConventionBasedTest;
import io.prestodb.tempto.internal.convention.ConventionBasedTestFactory;
import io.prestodb.tempto.internal.convention.ConventionBasedTestProxyGenerator;
import io.prestodb.tempto.internal.convention.MutableTableDescriptor;
import io.prestodb.tempto.internal.convention.SqlQueryDescriptor;
import io.prestodb.tempto.internal.convention.SqlResultDescriptor;
import io.prestodb.tempto.internal.convention.SqlTestsFileUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static io.prestodb.tempto.Requirements.compose;
import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle;
import static io.prestodb.tempto.internal.convention.SqlResultDescriptor.sqlResultDescriptorFor;
import static io.prestodb.tempto.internal.convention.SqlTestsFileUtils.changeExtension;
import static io.prestodb.tempto.internal.convention.SqlTestsFileUtils.getExtension;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class SqlPathTestFactory
        implements ConventionBasedTestFactory.PathTestFactory
{
    private static final String TEST_FILE_EXTENSION = "sql";
    private static final String RESULT_FILE_EXTENSION = "result";
    private static final String BEFORE_SCRIPT_NAME = "before";
    private static final String AFTER_SCRIPT_NAME = "after";
    private static final SqlResultDescriptor EMPTY_SQL_RESULT_DESCRIPTOR = new SqlResultDescriptor(
            new SectionParsingResult(Optional.empty(), ImmutableList.of(), ImmutableMap.of(), ImmutableList.of()));

    private final TableDefinitionsRepository tableDefinitionsRepository;
    private final ConventionBasedTestProxyGenerator proxyGenerator;
    private final Configuration configuration;

    public SqlPathTestFactory(
            TableDefinitionsRepository tableDefinitionsRepository,
            ConventionBasedTestProxyGenerator proxyGenerator,
            Configuration configuration)
    {
        this.tableDefinitionsRepository = requireNonNull(tableDefinitionsRepository, "tableDefinitionsRepository is null");
        this.proxyGenerator = requireNonNull(proxyGenerator, "proxyGenerator is null");
        this.configuration = requireNonNull(configuration, "configuration is null");
    }

    @Override
    public boolean isSupportedPath(Path path)
    {
        return TEST_FILE_EXTENSION.equals(getExtension(path));
    }

    @Override
    public List<ConventionBasedTest> createTestsForPath(Path testFile, String testNamePrefix, ConventionBasedTestFactory factory)
    {
        checkState(exists(testFile) && isRegularFile(testFile), "Could not find file: %s", testFile.toAbsolutePath());

        Path beforeScriptFile = testFile.getParent().resolve(BEFORE_SCRIPT_NAME);
        Optional<Path> optionalBeforeScriptFile = isRegularFile(beforeScriptFile) ? Optional.of(beforeScriptFile) : Optional.<Path>empty();
        optionalBeforeScriptFile.ifPresent(SqlTestsFileUtils::makeExecutable);

        Path afterScripFile = testFile.getParent().resolve(AFTER_SCRIPT_NAME);
        Optional<Path> optionalAfterScriptFile = isRegularFile(afterScripFile) ? Optional.of(afterScripFile) : Optional.<Path>empty();
        optionalAfterScriptFile.ifPresent(SqlTestsFileUtils::makeExecutable);

        List<SectionParsingResult> sections = new AnnotatedFileParser().parseFile(testFile);
        if (sections.size() == 1) {
            return createTestsForSingleSectionTestFile(testFile, testNamePrefix, optionalBeforeScriptFile, optionalAfterScriptFile, getOnlyElement(sections));
        }
        else {
            return createTestsForMultiSectionTestFile(testFile, testNamePrefix, optionalBeforeScriptFile, optionalAfterScriptFile, sections);
        }
    }

    private List<ConventionBasedTest> createTestsForSingleSectionTestFile(
            Path testFile, String testNamePrefix, Optional<Path> optionalBeforeScriptFile, Optional<Path> optionalAfterScriptFile,
            SectionParsingResult querySection)
    {
        Path resultFile = changeExtension(testFile, RESULT_FILE_EXTENSION);

        SqlResultDescriptor sqlResultDescriptor = EMPTY_SQL_RESULT_DESCRIPTOR;
        if (exists(resultFile)) {
            checkState(isRegularFile(resultFile), "Expected result at: %s", resultFile.toAbsolutePath());
            sqlResultDescriptor = sqlResultDescriptorFor(resultFile);
        }
        return createTestsForSections(
                testFile, testNamePrefix, optionalBeforeScriptFile, optionalAfterScriptFile,
                newArrayList(new SqlQueryDescriptor(querySection)), newArrayList(sqlResultDescriptor));
    }

    private List<ConventionBasedTest> createTestsForMultiSectionTestFile(
            Path testFile, String testNamePrefix, Optional<Path> optionalBeforeScriptFile, Optional<Path> optionalAfterScriptFile,
            List<SectionParsingResult> sections)
    {
        checkState((sections.size() % 2) == 1, "First section should contain properties, next sections should represent query and results");

        SectionParsingResult baseSection = sections.get(0);
        List<SqlQueryDescriptor> queryDescriptors = newArrayList();
        List<SqlResultDescriptor> resultDescriptors = newArrayList();
        for (int i = 1; i < sections.size(); i += 2) {
            queryDescriptors.add(new SqlQueryDescriptor(sections.get(i), baseSection.getProperties()));
            resultDescriptors.add(new SqlResultDescriptor(sections.get(i + 1), baseSection.getProperties()));
        }

        return createTestsForSections(
                testFile, testNamePrefix, optionalBeforeScriptFile, optionalAfterScriptFile,
                queryDescriptors, resultDescriptors);
    }

    private List<ConventionBasedTest> createTestsForSections(
            Path testFile, String testNamePrefix, Optional<Path> optionalBeforeScriptFile, Optional<Path> optionalAfterScriptFile,
            List<SqlQueryDescriptor> queryDescriptors, List<SqlResultDescriptor> resultDescriptors)
    {
        checkState(queryDescriptors.size() == resultDescriptors.size());
        List<ConventionBasedTest> conventionBasedTests = newArrayList();
        for (int i = 0; i < queryDescriptors.size(); ++i) {
            SqlQueryDescriptor queryDescriptor = queryDescriptors.get(i);
            SqlResultDescriptor resultDescriptor = resultDescriptors.get(i);
            Requirement requirement = getRequirements(queryDescriptor);
            SqlQueryConventionBasedTest conventionTest = new SqlQueryConventionBasedTest(
                    optionalBeforeScriptFile,
                    optionalAfterScriptFile,
                    testFile,
                    testNamePrefix,
                    i,
                    queryDescriptors.size(),
                    queryDescriptor,
                    resultDescriptor,
                    requirement);
            ConventionBasedTest proxiedConventionTest = proxyGenerator.generateProxy(conventionTest);
            conventionBasedTests.add(proxiedConventionTest);
        }
        return conventionBasedTests;
    }

    private Requirement getRequirements(SqlQueryDescriptor queryDescriptor)
    {
        List<Requirement> requirements = newArrayList();
        for (TableHandle tableHandle : queryDescriptor.getTableDefinitionHandles()) {
            TableDefinition tableDefinition = tableDefinitionsRepository.get(tableHandle);
            tableHandle = resolveTableHandle(queryDescriptor, tableHandle);
            requirements.add(new ImmutableTableRequirement(tableDefinition, tableHandle));
        }
        for (MutableTableDescriptor descriptor : queryDescriptor.getMutableTableDescriptors()) {
            TableHandle tableHandle = resolveTableHandle(queryDescriptor, descriptor.tableHandle);
            TableDefinition tableDefinition = tableDefinitionsRepository.get(tableHandle(descriptor.tableDefinitionName));
            requirements.add(MutableTableRequirement.builder(tableDefinition)
                    .withTableHandle(tableHandle)
                    .withState(descriptor.state)
                    .build());
        }
        requirements.addAll(queryDescriptor.getRequirementClassNames()
                .stream()
                .map(this::getRequirementsFromClass)
                .collect(toList()));
        return compose(requirements);
    }

    private TableHandle resolveTableHandle(SqlQueryDescriptor queryDescriptor, TableHandle tableHandle)
    {
        if (!tableHandle.getDatabase().isPresent()) {
            tableHandle = tableHandle.inDatabase(queryDescriptor.getDatabaseName());
        }
        return tableHandle;
    }

    private Requirement getRequirementsFromClass(String requirementClassName)
    {
        RequirementsProvider requirementsProvider = ReflectionHelper.instantiate(requirementClassName);
        return requirementsProvider.getRequirements(configuration);
    }
}
