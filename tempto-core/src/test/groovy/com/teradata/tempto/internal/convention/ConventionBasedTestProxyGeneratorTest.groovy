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

package com.teradata.tempto.internal.convention

import com.teradata.tempto.Requirement
import com.teradata.tempto.internal.convention.sql.SqlQueryConventionBasedTest
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

  private ConventionBasedTestProxyGenerator proxyGenerator = new ConventionBasedTestProxyGenerator("com.teradata.tempto");

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
