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

package com.teradata.test.internal.convention.sql;

import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.convention.SqlResultDescriptor;
import com.teradata.test.fulfillment.table.ImmutableTableRequirement;
import com.teradata.test.fulfillment.table.MutableTableRequirement;
import com.teradata.test.fulfillment.table.TableDefinitionsRepository;
import com.teradata.test.internal.ReflectionHelper;
import com.teradata.test.internal.convention.AnnotatedFileParser;
import com.teradata.test.internal.convention.AnnotatedFileParser.SectionParsingResult;
import com.teradata.test.internal.convention.ConventionBasedTest;
import com.teradata.test.internal.convention.ConventionBasedTestFactory;
import com.teradata.test.internal.convention.ConventionBasedTestProxyGenerator;
import com.teradata.test.internal.convention.SqlQueryDescriptor;
import com.teradata.test.internal.convention.SqlTestsFileUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.convention.SqlResultDescriptor.sqlResultDescriptorFor;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.changeExtension;
import static com.teradata.test.internal.convention.SqlTestsFileUtils.getExtension;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
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
            return createTestsForSingleSectionTestFile(testFile, optionalBeforeScriptFile, optionalAfterScriptFile, getOnlyElement(sections));
        }
        else {
            return createTestsForMultiSectionTestFile(testFile, optionalBeforeScriptFile, optionalAfterScriptFile, sections);
        }
    }

    private List<ConventionBasedTest> createTestsForSingleSectionTestFile(
            Path testFile, Optional<Path> optionalBeforeScriptFile, Optional<Path> optionalAfterScriptFile,
            SectionParsingResult querySection)
    {
        Path resultFile = changeExtension(testFile, RESULT_FILE_EXTENSION);
        checkState(exists(resultFile) && isRegularFile(resultFile), "Could not find file: %s", resultFile.toAbsolutePath());

        return createTestsForSections(
                testFile, optionalBeforeScriptFile, optionalAfterScriptFile,
                newArrayList(new SqlQueryDescriptor(querySection)), newArrayList(sqlResultDescriptorFor(resultFile)));
    }

    private List<ConventionBasedTest> createTestsForMultiSectionTestFile(
            Path testFile, Optional<Path> optionalBeforeScriptFile, Optional<Path> optionalAfterScriptFile,
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
                testFile, optionalBeforeScriptFile, optionalAfterScriptFile,
                queryDescriptors, resultDescriptors);
    }

    private List<ConventionBasedTest> createTestsForSections(
            Path testFile, Optional<Path> optionalBeforeScriptFile, Optional<Path> optionalAfterScriptFile,
            List<SqlQueryDescriptor> queryDescriptors, List<SqlResultDescriptor> resultDescriptors)
    {
        checkState(queryDescriptors.size() == resultDescriptors.size());
        List<ConventionBasedTest> conventionBasedTests = newArrayList();
        for (int i = 0; i < queryDescriptors.size(); ++i) {
            SqlQueryDescriptor queryDescriptor = queryDescriptors.get(i);
            SqlResultDescriptor resultDescriptor = resultDescriptors.get(i);
            Requirement requirement = getRequirements(queryDescriptor);
            SqlQueryConventionBasedTest conventionTest = new SqlQueryConventionBasedTest(
                    optionalBeforeScriptFile, optionalAfterScriptFile,
                    testFile, i + 1, queryDescriptor, resultDescriptor, requirement);
            ConventionBasedTest proxiedConventionTest = proxyGenerator.generateProxy(conventionTest);
            conventionBasedTests.add(proxiedConventionTest);
        }
        return conventionBasedTests;
    }

    private Requirement getRequirements(SqlQueryDescriptor queryDescriptor)
    {
        List<Requirement> requirements = newArrayList();
        requirements.addAll(queryDescriptor.getTableDefinitionNames()
                .stream()
                .map(requiredTableName -> new ImmutableTableRequirement(tableDefinitionsRepository.getForName(requiredTableName)))
                .collect(toList()));
        requirements.addAll(queryDescriptor.getMutableTableDescriptors()
                .stream()
                .map(descriptor -> new MutableTableRequirement(
                        tableDefinitionsRepository.getForName(descriptor.tableDefinitionName),
                        descriptor.name,
                        descriptor.state))
                .collect(toList()));
        requirements.addAll(queryDescriptor.getRequirementClassNames()
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
