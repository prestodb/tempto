/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

/**
 * An utility class for composing requirements.
 */
public class Requirements {

    public static RequirementsInfo compose(Requirement... requirements) {
        return compose(asList(requirements));
    }

    public static RequirementsInfo compose(List<Requirement> requirements) {
        return () -> newHashSet(transform(requirements, Requirements::wrapInSet));
    }

    private static Set<Requirement> wrapInSet(Requirement requirement) {
        return newHashSet(requirement);
    }
}
