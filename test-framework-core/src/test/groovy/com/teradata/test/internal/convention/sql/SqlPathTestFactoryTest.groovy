/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention.sql

import com.teradata.test.CompositeRequirement
import com.teradata.test.Requirement
import com.teradata.test.RequirementsProvider
import com.teradata.test.fulfillment.table.TableDefinitionsRepository
import com.teradata.test.internal.RequirementsCollector
import com.teradata.test.internal.convention.ConventionBasedTest
import com.teradata.test.internal.convention.ConventionBasedTestProxyGenerator
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

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
    conventionBasedTestProxyGeneratorMock.generateProxy(_) >> Mock(ConventionBasedTest)
    sqlPathTestFactory = new SqlPathTestFactory(tableDefinitionsRepositoryMock, conventionBasedTestProxyGeneratorMock)
  }

  def createConventionTestWithRequires()
  {
    setup:
    Path testPath = getPathForConventionTest("-- requires: ${DummyRequirementsProvider.class.name}")

    when:
    List<ConventionBasedTest> conventionBasedTests = sqlPathTestFactory.createTestsForPath(testPath, '', null)

    then:
    conventionBasedTests.size() == 1
    containsRequirement(conventionBasedTests.get(0).getRequirements(), DummyRequirement)
  }

  def createConventionTestWithWrongRequires()
  {
    setup:
    Path testPath = getPathForConventionTest("-- requires: not.existing.Requirement")

    when:
    sqlPathTestFactory.createTestsForPath(testPath, '', null)

    then:
    RuntimeException ex = thrown()
    ex.message == 'Unable to find specified class: not.existing.Requirement'
  }

  private Path getPathForConventionTest(String conventionTestContent)
  {
    def file = temporaryFolder.newFile()
    file.write conventionTestContent
    def testPath = Paths.get(file.path)

    // mock result file
    File resultFile = new File(testPath.toString().replace('.tmp', '.result'))
    resultFile.createNewFile()

    return testPath
  }

  private boolean containsRequirement(Requirement requirement, Class<? extends Requirement> requirementClass)
  {
    RequirementsCollector requirementsCollector = new RequirementsCollector()
    if (requirement instanceof CompositeRequirement) {
      return (requirement as CompositeRequirement).requirementsSets.any {
        it.any {
          containsRequirement(it, requirementClass)
        }
      }
    } else {
      return requirementClass.isInstance(requirement)
    }
  }

  public static class DummyRequirementsProvider
          implements RequirementsProvider
  {
    @Override
    Requirement getRequirements()
    {
      return new DummyRequirement()
    }
  }

  public static class DummyRequirement
          implements Requirement
  {
  }
}
