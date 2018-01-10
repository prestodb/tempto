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

package io.prestodb.tempto.internal.convention.sql

import io.prestodb.tempto.CompositeRequirement
import io.prestodb.tempto.Requirement
import io.prestodb.tempto.RequirementsProvider
import io.prestodb.tempto.configuration.Configuration
import io.prestodb.tempto.fulfillment.table.TableDefinitionsRepository
import io.prestodb.tempto.internal.convention.ConventionBasedTest
import io.prestodb.tempto.internal.convention.ConventionBasedTestProxyGenerator
import org.apache.commons.io.FilenameUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

import static io.prestodb.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration

class SqlPathTestFactoryTest
        extends Specification
{
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Shared
    SqlPathTestFactory sqlPathTestFactory

    def setup()
    {
        TableDefinitionsRepository tableDefinitionsRepositoryMock = Mock()
        ConventionBasedTestProxyGenerator conventionBasedTestProxyGeneratorMock = new ConventionBasedTestProxyGenerator('test')
        sqlPathTestFactory = new SqlPathTestFactory(tableDefinitionsRepositoryMock, conventionBasedTestProxyGeneratorMock, emptyConfiguration())
    }

    def shouldCreateConventionTestWithRequires()
    {
        setup:
        Path testPath = getPathForConventionTest("-- requires: ${DummyRequirementsProvider1.class.name}; groups:foo")

        when:
        List<ConventionBasedTest> conventionBasedTests = sqlPathTestFactory.createTestsForPath(testPath, 'tests.prefix', null)
        String baseTestFileName = FilenameUtils.getBaseName(testPath.getFileName().toString());

        then:
        conventionBasedTests.size() == 1
        containsRequirement(conventionBasedTests.get(0).getRequirements(), DummyRequirement1)
        conventionBasedTests.get(0).testName == "tests.prefix.${baseTestFileName}" as String
        conventionBasedTests.get(0).testGroups == ['foo'] as Set
    }

    def shouldUseSectionNameAsTestName()
    {
        setup:
        Path testPath = getPathForConventionTest("-- name:foo_boo")

        when:
        List<ConventionBasedTest> conventionBasedTests = sqlPathTestFactory.createTestsForPath(testPath, 'tests.prefix', null)
        String baseTestFileName = FilenameUtils.getBaseName(testPath.getFileName().toString());

        then:
        conventionBasedTests.size() == 1
        conventionBasedTests.get(0).testName == "tests.prefix.${baseTestFileName}.foo_boo" as String
    }

    def shouldCreateTestsWithMultipleSections()
    {
        setup:
        Path testPath = getPathForConventionTest(
                """
-- requires: ${DummyRequirementsProvider1.class.name}
--! name: query_1; requires: ${DummyRequirementsProvider2.class.name}
query 1 sql
--!
query 1 result
--! name: query_2
query 2 sql
--!
query 2 result
""", Optional.empty())

        when:
        List<ConventionBasedTest> conventionBasedTests = sqlPathTestFactory.createTestsForPath(testPath, 'tests.prefix', null)
        String testFileBaseName = FilenameUtils.getBaseName(testPath.getFileName().toString())

        then:
        conventionBasedTests.size() == 2

        conventionBasedTests.get(0).testName == "tests.prefix.${testFileBaseName}.query_1" as String
        containsRequirement(conventionBasedTests.get(0).getRequirements(), DummyRequirement1)
        containsRequirement(conventionBasedTests.get(0).getRequirements(), DummyRequirement2)

        conventionBasedTests.get(1).testName == "tests.prefix.${testFileBaseName}.query_2" as String
        containsRequirement(conventionBasedTests.get(1).getRequirements(), DummyRequirement1)
        !containsRequirement(conventionBasedTests.get(1).getRequirements(), DummyRequirement2)
    }

    def shouldCreateConventionTestWithWrongRequires()
    {
        setup:
        Path testPath = getPathForConventionTest("-- requires: not.existing.Requirement")

        when:
        sqlPathTestFactory.createTestsForPath(testPath, '', null)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Unable to find specified class: not.existing.Requirement'
    }

    def shouldCreateTestWhenNoResultsFile()
    {
        setup:
        Path testPath = getPathForConventionTest("--", Optional.empty())

        when:
        List<ConventionBasedTest> tests = sqlPathTestFactory.createTestsForPath(testPath, '', null)

        then:
        tests.size() == 1
    }

    def shouldFailInvalidNumberOfSections()
    {
        setup:
        Path testPath = getPathForConventionTest("--\n--!", Optional.empty())

        when:
        sqlPathTestFactory.createTestsForPath(testPath, '', null)

        then:
        IllegalStateException e = thrown()
        e.message == 'First section should contain properties, next sections should represent query and results'
    }

    private Path getPathForConventionTest(String conventionTestContent)
    {
        getPathForConventionTest(conventionTestContent, Optional.of(''))
    }

    private Path getPathForConventionTest(String conventionTestContent, Optional<String> resultFileContent)
    {
        def file = temporaryFolder.newFile()
        file.write conventionTestContent
        def testPath = Paths.get(file.path)

        if (resultFileContent.isPresent()) {
            File resultFile = new File(testPath.toString().replace('.tmp', '.result'))
            resultFile.write(resultFileContent.get())
        }

        return testPath
    }

    private boolean containsRequirement(Requirement requirement, Class<? extends Requirement> requirementClass)
    {
        if (requirement instanceof CompositeRequirement) {
            return (requirement as CompositeRequirement).requirementsSets.any {
                it.any {
                    containsRequirement(it, requirementClass)
                }
            }
        }
        else {
            return requirementClass.isInstance(requirement)
        }
    }

    static class DummyRequirementsProvider1
            implements RequirementsProvider
    {
        @Override
        Requirement getRequirements(Configuration configuration)
        {
            return new DummyRequirement1()
        }
    }

    static class DummyRequirementsProvider2
            implements RequirementsProvider
    {
        @Override
        Requirement getRequirements(Configuration configuration)
        {
            return new DummyRequirement2()
        }
    }

    static class DummyRequirement1
            implements Requirement
    {
    }

    static class DummyRequirement2
            implements Requirement
    {
    }
}
