/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import com.google.common.collect.ImmutableSet;

import java.util.List;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

/**
 * An utility class for composing requirements.
 */
public final class Requirements {

    public static Requirement compose(Requirement... requirements) {
        return compose(asList(requirements));
    }

    public static Requirement compose(List<Requirement> requirements) {
        return new MultiCompositeRequirement(ImmutableSet.of(copyOf(wrapAsCompositeRequirement(requirements))));
    }

    public static Requirement allOf(Requirement... requirements) {
        return allOf(asList(requirements));
    }

    public static Requirement allOf(List<Requirement> requirements) {
        return new MultiCompositeRequirement(copyOf(transform(wrapAsCompositeRequirement(requirements), ImmutableSet::<CompositeRequirement>of)));
    }

    private static List<CompositeRequirement> wrapAsCompositeRequirement(List<Requirement> requirements) {
        return transform(requirements, (Requirement requirement) -> {
            if (requirement instanceof CompositeRequirement) {
                return (CompositeRequirement) requirement;
            } else {
                return new SingletonCompositeRequirement(requirement);
            }
        });
    }

    private Requirements() {
    }
}
