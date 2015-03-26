package com.teradata.test.initialization;

import com.teradata.test.Requirement;

import java.lang.reflect.Method;

/**
 * Provides {@link Requirement} for tests methods.
 */
public interface TestMethodRequirementsProvider
{
    Requirement getRequirements(Method testMethod, Object[] parameters);

    Requirement getAllRequirements();
}
