/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test

import com.google.common.collect.ImmutableSet
import spock.lang.Specification

import static com.teradata.test.Requirements.allOf
import static com.teradata.test.Requirements.compose
import static com.teradata.test.RequirementsCollector.collectRequirementsFor
import static java.util.Arrays.asList

public class RequirementsCollectorTest extends Specification {

    private static final def A = req('a')
    private static final def B = req('b')
    private static final def C = req('c')
    private static final def D = req('d')

    def "should list method requirements"() {
        expect:
        collectRequirementsFor(method) == expectedRequirementSets

        where:
        method                                        | expectedRequirementSets
        MethodRequirement.getMethod('method')         | setOf(setOf(A))
        ClassRequirement.getMethod('method')          | setOf(setOf(B))
        MethodAndClassRequirement.getMethod('method') | setOf(setOf(A, B))
    }

    def "should compose requirements"() {
        expect:
        ((CompositeRequirement) requirement).requirementsSets == expectedRequirementSets

        where:
        requirement                         | expectedRequirementSets
        compose()                           | setOf(setOf())

        compose(A, B)                       | setOf(setOf(A, B))

        allOf(A, B)                         | setOf(setOf(A), setOf(B))

        compose(C, allOf(A, B))             | setOf(setOf(A, C), setOf(B, C))

        compose(allOf(C, D), allOf(A, B))   | setOf(setOf(A, C), setOf(B, C), setOf(D, A), setOf(D, B))

        compose(allOf(C, allOf(A, B)), D)   | setOf(setOf(C, D), setOf(A, D), setOf(B, D))

        compose(A, allOf(compose(B, C), D)) | setOf(setOf(A, B, C), setOf(A, D))

    }

    private static Requirement req(String name) {
        return new DummyTestRequirement(name)
    }

    private static <E> Set<E> setOf(E e) {
        ImmutableSet.of(e)
    }

    private static <E> Set<E> setOf(E... elems) {
        ImmutableSet.builder().addAll(asList(elems)).build()
    }

    private static class MethodRequirement {
        @Requires(ProviderA)
        public void method() {
        }
    }

    @Requires(ProviderB)
    private static class ClassRequirement {
        public void method() {
        }
    }

    @Requires(ProviderA)
    private static class MethodAndClassRequirement {
        @Requires(ProviderB)
        public void method() {
        }
    }

    private static class ProviderA implements RequirementsProvider {
        Requirement getRequirements() {
            return A
        }
    }

    private static class ProviderB implements RequirementsProvider {
        @Override
        Requirement getRequirements() {
            return B
        }
    }

    private static final class DummyTestRequirement implements Requirement {
        private final String name;

        DummyTestRequirement(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
}
