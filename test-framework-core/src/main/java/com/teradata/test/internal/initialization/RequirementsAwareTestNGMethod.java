/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.initialization;

import com.teradata.test.Requirement;
import org.testng.ITestNGMethod;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class RequirementsAwareTestNGMethod
        extends DelegateTestNGMethod
{
    private final Set<Requirement> requirements;

    public RequirementsAwareTestNGMethod(ITestNGMethod delegate, Set<Requirement> requirements)
    {
        super(delegate);
        this.requirements = requirements;
    }

    public Set<Requirement> getRequirements() {
        return requirements;
    }

    @Override
    public ITestNGMethod clone()
    {
        return new RequirementsAwareTestNGMethod(super.delegate.clone(), newHashSet(requirements));
    }
}
