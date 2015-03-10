/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class SingletonCompositeRequirement implements CompositeRequirement {

    private final Set<Set<Requirement>> requirementsSets;

    public SingletonCompositeRequirement(Requirement requirement) {
        this.requirementsSets = ImmutableSet.of(ImmutableSet.of(requirement));
    }

    public Set<Set<Requirement>> getRequirementsSets() {
        return requirementsSets;
    }
}
