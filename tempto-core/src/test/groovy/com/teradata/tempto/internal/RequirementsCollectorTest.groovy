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


package com.teradata.tempto.internal

import com.google.common.collect.ImmutableSet
import com.teradata.tempto.CompositeRequirement
import com.teradata.tempto.Requirement
import com.teradata.tempto.Requirements
import com.teradata.tempto.RequirementsProvider
import com.teradata.tempto.Requires
import com.teradata.tempto.configuration.Configuration
import spock.lang.Specification

import static com.teradata.tempto.Requirements.compose
import static com.teradata.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration
import static java.util.Arrays.asList

public class RequirementsCollectorTest
        extends Specification
{
  private static final def A = req('a')
  private static final def B = req('b')
  private static final def C = req('c')
  private static final def D = req('d')

  def requirementsCollector = new DefaultRequirementsCollector(emptyConfiguration())

  def "should list method requirements"()
  {
    expect:
    requirementsCollector.getAnnotationBasedRequirementsFor(method).requirementsSets == expectedRequirementSets

    where:
    method                                        | expectedRequirementSets
    MethodRequirement.getMethod('method')         | setOf(setOf(A))
    ClassRequirement.getMethod('method')          | setOf(setOf(B))
    MethodAndClassRequirement.getMethod('method') | setOf(setOf(A, B))
  }

  def "should compose requirements"()
  {
    expect:
    ((CompositeRequirement) requirement).requirementsSets == expectedRequirementSets

    where:
    requirement                         | expectedRequirementSets
    compose()                           | setOf(setOf())

    compose(A, B)                       | setOf(setOf(A, B))

    Requirements.allOf(A, B)                         | setOf(setOf(A), setOf(B))

    compose(C, Requirements.allOf(A, B))             | setOf(setOf(A, C), setOf(B, C))

    compose(Requirements.allOf(C, D), Requirements.allOf(A, B))   | setOf(setOf(A, C), setOf(B, C), setOf(D, A), setOf(D, B))

    compose(Requirements.allOf(C, Requirements.allOf(A, B)), D)   | setOf(setOf(C, D), setOf(A, D), setOf(B, D))

    compose(A, Requirements.allOf(compose(B, C), D)) | setOf(setOf(A, B, C), setOf(A, D))
  }

  private static Requirement req(String name)
  {
    return new DummyTestRequirement(name)
  }

  private static <E> Set<E> setOf(E e)
  {
    ImmutableSet.of(e)
  }

  private static <E> Set<E> setOf(E... elems)
  {
    ImmutableSet.builder().addAll(asList(elems)).build()
  }

  private static class MethodRequirement
  {
    @Requires(ProviderA)
    public void method()
    {
    }
  }

  @Requires(ProviderB)
  private static class ClassRequirement
  {
    public void method()
    {
    }
  }

  @Requires(ProviderA)
  private static class MethodAndClassRequirement
  {
    @Requires(ProviderB)
    public void method()
    {
    }
  }

  private static class ProviderA
          implements RequirementsProvider
  {
    Requirement getRequirements(Configuration configuration)
    {
      return A
    }
  }

  private static class ProviderB
          implements RequirementsProvider
  {
    @Override
    Requirement getRequirements(Configuration configuration)
    {
      return B
    }
  }

  private static final class DummyTestRequirement
          implements Requirement
  {
    private final String name;

    DummyTestRequirement(String name)
    {
      this.name = name;
    }

    public String toString()
    {
      return name;
    }
  }
}
