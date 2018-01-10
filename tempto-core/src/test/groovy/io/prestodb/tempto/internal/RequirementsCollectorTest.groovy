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

package io.prestodb.tempto.internal

import com.google.common.collect.ImmutableSet
import io.prestodb.tempto.CompositeRequirement
import io.prestodb.tempto.Requirement
import io.prestodb.tempto.Requirements
import io.prestodb.tempto.RequirementsProvider
import io.prestodb.tempto.Requires
import io.prestodb.tempto.configuration.Configuration
import io.prestodb.tempto.internal.configuration.YamlConfiguration
import spock.lang.Specification

import static io.prestodb.tempto.Requirements.compose
import static io.prestodb.tempto.fulfillment.command.SuiteCommandRequirement.suiteCommand
import static io.prestodb.tempto.fulfillment.command.TestCommandRequirement.testCommand
import static io.prestodb.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration
import static java.util.Arrays.asList

class RequirementsCollectorTest
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
        requirementsCollector.collect(method).requirementsSets == expectedRequirementSets

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
        requirement                                                 | expectedRequirementSets
        compose()                                                   | setOf(setOf())

        compose(A, B)                                               | setOf(setOf(A, B))

        Requirements.allOf(A, B)                                    | setOf(setOf(A), setOf(B))

        compose(C, Requirements.allOf(A, B))                        | setOf(setOf(A, C), setOf(B, C))

        compose(Requirements.allOf(C, D), Requirements.allOf(A, B)) | setOf(setOf(A, C), setOf(B, C), setOf(D, A), setOf(D, B))

        compose(Requirements.allOf(C, Requirements.allOf(A, B)), D) | setOf(setOf(C, D), setOf(A, D), setOf(B, D))

        compose(A, Requirements.allOf(compose(B, C), D))            | setOf(setOf(A, B, C), setOf(A, D))
    }

    def "should provide command requirements from configuration"()
    {
        expect:
        def requirementsCollector = new DefaultRequirementsCollector(new YamlConfiguration('''
command:
  test:
    - test command
  suite:
    - suite command
    '''))
        requirementsCollector.collect(method).requirementsSets == expectedRequirementSets

        where:
        method                                | expectedRequirementSets
        MethodRequirement.getMethod('method') | setOf(setOf(A, testCommand('test command'), suiteCommand('suite command')))
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
        void method()
        {
        }
    }

    @Requires(ProviderB)
    private static class ClassRequirement
    {
        void method()
        {
        }
    }

    @Requires(ProviderA)
    private static class MethodAndClassRequirement
    {
        @Requires(ProviderB)
        void method()
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
}
