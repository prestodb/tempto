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

package io.prestodb.tempto;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

/**
 * An utility class for composing requirements.
 */
public final class Requirements
{
    public static CompositeRequirement compose(Requirement... requirements)
    {
        return compose(asList(requirements));
    }

    public static CompositeRequirement compose(List<Requirement> requirements)
    {
        return new MultiCompositeRequirement(ImmutableSet.of(copyOf(wrapAsCompositeRequirements(requirements))));
    }

    public static CompositeRequirement allOf(Requirement... requirements)
    {
        return allOf(asList(requirements));
    }

    public static CompositeRequirement allOf(List<Requirement> requirements)
    {
        return new MultiCompositeRequirement(wrapAsCompositeRequirements(requirements).stream()
                .map(ImmutableSet::of)
                .collect(toImmutableSet()));
    }

    private static List<CompositeRequirement> wrapAsCompositeRequirements(List<Requirement> requirements)
    {
        return requirements.stream()
                .map((Requirement requirement) -> {
                    if (requirement instanceof CompositeRequirement) {
                        return (CompositeRequirement) requirement;
                    } else {
                        return new SingletonCompositeRequirement(requirement);
                    }
                }).collect(toImmutableList());
    }

    private static class SingletonCompositeRequirement
            implements CompositeRequirement
    {
        private final Set<Set<Requirement>> requirementsSets;

        public SingletonCompositeRequirement(Requirement requirement)
        {
            this.requirementsSets = ImmutableSet.of(ImmutableSet.of(requirement));
        }

        public Set<Set<Requirement>> getRequirementsSets()
        {
            return requirementsSets;
        }
    }

    private static class MultiCompositeRequirement
            implements CompositeRequirement
    {
        private final Set<Set<CompositeRequirement>> requirementsSetsBranches;

        public MultiCompositeRequirement(Set<Set<CompositeRequirement>> requirementsSetsBranches)
        {
            this.requirementsSetsBranches = requirementsSetsBranches;
        }

        @Override
        public Set<Set<Requirement>> getRequirementsSets()
        {
            Set<Set<Requirement>> expandedRequirementsSets = newHashSet();
            for (Set<CompositeRequirement> requirementsSetsBranch : requirementsSetsBranches) {
                expandedRequirementsSets.addAll(expandRequirements(requirementsSetsBranch));
            }
            return expandedRequirementsSets;
        }

        private Set<Set<Requirement>> expandRequirements(Collection<CompositeRequirement> requirementsSetsBranch)
        {
            Set<Set<Requirement>> expandedRequirementsSets = ImmutableSet.of(ImmutableSet.of());
            for (CompositeRequirement requirementsSets : requirementsSetsBranch) {
                expandedRequirementsSets = expandRequirements(expandedRequirementsSets, requirementsSets);
            }
            return expandedRequirementsSets;
        }

        private Set<Set<Requirement>> expandRequirements(Set<Set<Requirement>> expandedRequirementsSets, CompositeRequirement requirementsSets)
        {
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

    private Requirements()
    {
    }
}
