/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention

import com.teradata.test.Requirement
import com.teradata.test.internal.convention.sql.SqlQueryConventionBasedTest
import org.testng.annotations.Test
import spock.lang.Specification

import java.lang.reflect.Method
import java.nio.file.Path
import java.nio.file.Paths

import static org.assertj.core.api.Assertions.assertThat

class ConventionBasedTestProxyGeneratorTest
        extends Specification
{

  private ConventionBasedTestProxyGenerator proxyGenerator = new ConventionBasedTestProxyGenerator("com.teradata.test");

  def 'testGenerateProxy'()
  {
    when:
    Path queryFile = file("convention/sample-test/query1.sql")
    Path resultFile = file("convention/sample-test/query1.sql")
    Requirement requirement = Mock(Requirement)
    ConventionBasedTest testInstance = new SqlQueryConventionBasedTest(Optional.empty(), Optional.empty(), queryFile, resultFile, requirement)

    ConventionBasedTest proxiedTest = proxyGenerator.generateProxy(testInstance)
    Class<ConventionBasedTest> proxiedClass = proxiedTest.getClass()
    Method testMethod = proxiedClass.getMethod("query1")
    Test testAnnotation = testMethod.getAnnotation(Test)

    then:
    assertThat(proxiedTest.getRequirements()).isSameAs(requirement)
    assertThat(testAnnotation).isNotNull()
    assertThat(testAnnotation.enabled()).isTrue()
    assertThat(testAnnotation.groups()).containsOnly("tpch", "quarantine")
  }

  private Path file(String path)
  {
    Paths.get(getClass().getClassLoader().getResource(path).getPath())
  }
}
