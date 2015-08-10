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

package com.teradata.tempto.internal.convention.sql

import com.teradata.tempto.CompositeRequirement
import com.teradata.tempto.Requirement
import com.teradata.tempto.RequirementsProvider
import com.teradata.tempto.configuration.Configuration
import com.teradata.tempto.fulfillment.table.TableDefinitionsRepository
import com.teradata.tempto.internal.convention.ConventionBasedTest
import com.teradata.tempto.internal.convention.ConventionBasedTestProxyGenerator
import org.apache.commons.io.FilenameUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

import static com.teradata.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration

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
    List<ConventionBasedTest> conventionBasedTests = sqlPathTestFactory.createTestsForPath(testPath, '', null)

    then:
    conventionBasedTests.size() == 1
    containsRequirement(conventionBasedTests.get(0).getRequirements(), DummyRequirement1)
    conventionBasedTests.get(0).testCaseName() == FilenameUtils.getBaseName(testPath.getFileName().toString()) + '_1'
    conventionBasedTests.get(0).testGroups() == ['foo']
  }

  def shouldUseSectionNameAsTestName()
  {
    setup:
    Path testPath = getPathForConventionTest("-- name:foo_boo")

    when:
    List<ConventionBasedTest> conventionBasedTests = sqlPathTestFactory.createTestsForPath(testPath, '', null)

    then:
    conventionBasedTests.size() == 1
    conventionBasedTests.get(0).testCaseName() == 'foo_boo'
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
    List<ConventionBasedTest> conventionBasedTests = sqlPathTestFactory.createTestsForPath(testPath, '', null)

    then:
    conventionBasedTests.size() == 2

    conventionBasedTests.get(0).testCaseName() == 'query_1'
    containsRequirement(conventionBasedTests.get(0).getRequirements(), DummyRequirement1)
    containsRequirement(conventionBasedTests.get(0).getRequirements(), DummyRequirement2)

    conventionBasedTests.get(1).testCaseName() == 'query_2'
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

  def shouldFailInvalidQueryName()
  {
    setup:
    Path testPath = getPathForConventionTest("-- name: query^")

    when:
    sqlPathTestFactory.createTestsForPath(testPath, '', null)

    then:
    IllegalArgumentException e = thrown()
    e.message == 'Not a valid Java identifier: query^'
  }

  def shouldFailNoResultsFile()
  {
    setup:
    Path testPath = getPathForConventionTest("--", Optional.empty())

    when:
    sqlPathTestFactory.createTestsForPath(testPath, '', null)

    then:
    IllegalStateException e = thrown()
    e.message == 'Could not find file: ' + new File(testPath.toString().replace('.tmp', '.result'))
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

  public static class DummyRequirementsProvider1
          implements RequirementsProvider
  {
    @Override
    Requirement getRequirements(Configuration configuration)
    {
      return new DummyRequirement1()
    }
  }

  public static class DummyRequirementsProvider2
          implements RequirementsProvider
  {
    @Override
    Requirement getRequirements(Configuration configuration)
    {
      return new DummyRequirement2()
    }
  }

  public static class DummyRequirement1
          implements Requirement
  {
  }

  public static class DummyRequirement2
          implements Requirement
  {
  }
}
