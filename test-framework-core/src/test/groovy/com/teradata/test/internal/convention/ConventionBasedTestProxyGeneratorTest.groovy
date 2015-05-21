/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention

import com.teradata.test.Requirement
import com.teradata.test.convention.SqlResultDescriptor
import com.teradata.test.internal.convention.sql.SqlQueryConventionBasedTest
import org.testng.annotations.Test
import spock.lang.Specification

import java.lang.reflect.Method
import java.nio.file.Path
import java.nio.file.Paths

import static com.google.common.collect.Iterables.getOnlyElement
import static org.assertj.core.api.Assertions.assertThat

class ConventionBasedTestProxyGeneratorTest
        extends Specification
{

  private ConventionBasedTestProxyGenerator proxyGenerator = new ConventionBasedTestProxyGenerator("com.teradata.test");

  def 'testGenerateProxy'()
  {
    when:
    Path testFile = file("convention/sample-test/query1.sql")
    SqlQueryDescriptor queryDescriptor = new SqlQueryDescriptor(section(testFile))
    SqlResultDescriptor resultDescriptor = new SqlResultDescriptor(section(testFile))
    Requirement requirement = Mock(Requirement)
    ConventionBasedTest testInstance = new SqlQueryConventionBasedTest(Optional.empty(), Optional.empty(), testFile, 1, queryDescriptor, resultDescriptor, requirement)

    ConventionBasedTest proxiedTest = proxyGenerator.generateProxy(testInstance)
    Class<ConventionBasedTest> proxiedClass = proxiedTest.getClass()
    Method testMethod = proxiedClass.getMethod("query1_1")
    Test testAnnotation = testMethod.getAnnotation(Test)

    then:
    assertThat(proxiedTest.getRequirements()).isSameAs(requirement)
    assertThat(testAnnotation).isNotNull()
    assertThat(testAnnotation.enabled()).isTrue()
    assertThat(testAnnotation.groups()).containsOnly("tpch", "quarantine")
  }

  private file(String path)
  {
    Paths.get(getClass().getClassLoader().getResource(path).getPath())
  }

  private section(Path file)
  {
    getOnlyElement(new AnnotatedFileParser().parseFile(file))
  }
}
