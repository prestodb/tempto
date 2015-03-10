/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test

import com.google.common.collect.ImmutableSet
import spock.lang.Specification

import static com.teradata.test.Requirements.allOf
import static com.teradata.test.Requirements.compose
import static java.util.Arrays.asList

public class RequirementsTest extends Specification {

    private static final def A = req('a')
    private static final def B = req('b')
    private static final def C = req('c')
    private static final def D = req('d')

    def "test"() {
        expect:
        ((CompositeRequirement) requirement).requirementsSets == expectedRequirementSets

        where:
        requirement                         | expectedRequirementSets
        compose(A, B)                       | setOf(setOf(A, B))

        allOf(A, B)                         | setOf(setOf(A), setOf(B))

        compose(C, allOf(A, B))             | setOf(setOf(A, C), setOf(B, C))

        compose(allOf(C, D), allOf(A, B))   | setOf(setOf(A, C), setOf(B, C), setOf(D, A), setOf(D, B))

        compose(allOf(C, allOf(A, B)), D)   | setOf(setOf(C, D), setOf(A, D), setOf(B, D))

        compose(A, allOf(compose(B, C), D)) | setOf(setOf(A, B, C), setOf(A, D))

    }

    private static <E> Set<E> setOf(E e) {
        ImmutableSet.of(e)
    }

    private static <E> Set<E> setOf(E... elems) {
        ImmutableSet.builder().addAll(asList(elems)).build()
    }

    private static Requirement req(String name) {
        return new DummyTestRequirement(name)
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
