/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.teradata.test.Requirements.compose;
import static java.util.Arrays.asList;

/**
 * This class gathers requirements for a given test method.
 */
public final class RequirementsCollector
{
    public static Set<Set<Requirement>> collectRequirementsFor(Method method)
    {
        Requires methodRequiresAnnotation = method.getAnnotation(Requires.class);
        CompositeRequirement methodCompositeRequirement = getCompositeRequirement(methodRequiresAnnotation);

        Requires classRequiresAnnotation = method.getDeclaringClass().getAnnotation(Requires.class);
        CompositeRequirement classCompositeRequirement = getCompositeRequirement(classRequiresAnnotation);

        return compose(methodCompositeRequirement, classCompositeRequirement).getRequirementsSets();
    }

    private static CompositeRequirement getCompositeRequirement(Requires requires)
    {
        if (requires != null) {
            checkArgument(requires.value() != null);
            return compose(toRequirements(requires.value()));
        }
        else {
            return compose();
        }
    }

    private static List<Requirement> toRequirements(Class<? extends RequirementsProvider>[] providers)
    {
        return Lists.transform(asList(providers), (Class<? extends RequirementsProvider> providerClass) -> {
            try {
                Constructor<? extends RequirementsProvider> constructor = providerClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                RequirementsProvider provider = constructor.newInstance();
                return provider.getRequirements();
            }
            catch (InstantiationException  | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalArgumentException("Could not instantiate provider class", e);
            }
            catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No parameterless constructor for " + providerClass, e);
            }
        });
    }

    private RequirementsCollector()
    {
    }
}
