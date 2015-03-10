/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class MultiCompositeRequirement implements CompositeRequirement {

    private final Set<Set<CompositeRequirement>> requirementsSetsBranches;

    public MultiCompositeRequirement(Set<Set<CompositeRequirement>> requirementsSetsBranches) {
        this.requirementsSetsBranches = requirementsSetsBranches;
    }

    @Override
    public Set<Set<Requirement>> getRequirementsSets() {
        Set<Set<Requirement>> expandedRequirementsSets = newHashSet();
        for (Set<CompositeRequirement> requirementsSetsBranch : requirementsSetsBranches) {
            expandedRequirementsSets.addAll(expandRequirements(requirementsSetsBranch));
        }
        return expandedRequirementsSets;
    }

    private Set<Set<Requirement>> expandRequirements(Collection<CompositeRequirement> requirementsSetsBranch) {
        Set<Set<Requirement>> expandedRequirementsSets = ImmutableSet.of(ImmutableSet.of());
        for (CompositeRequirement requirementsSets : requirementsSetsBranch) {
            expandedRequirementsSets = expandRequirements(expandedRequirementsSets, requirementsSets);
        }
        return expandedRequirementsSets;
    }

    private Set<Set<Requirement>> expandRequirements(Set<Set<Requirement>> expandedRequirementsSets, CompositeRequirement requirementsSets) {
        Set<Set<Requirement>> newExpandedRequirementsSets = newHashSet();
        for (Set<Requirement> expandedRequirementSet : expandedRequirementsSets) {
            for (Set<Requirement> requirementSet : requirementsSets.getRequirementsSets()) {
                Set<Requirement> newExpandedRequirementSet = ImmutableSet.<Requirement>builder()
                        .addAll(expandedRequirementSet)
                        .addAll(requirementSet)
                        .build();
                newExpandedRequirementsSets.add(newExpandedRequirementSet);
            }
        }
        return newExpandedRequirementsSets;
    }
}
