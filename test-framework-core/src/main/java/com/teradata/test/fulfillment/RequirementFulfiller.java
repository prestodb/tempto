/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment;

import com.teradata.test.Requirement;
import com.teradata.test.context.State;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

public interface RequirementFulfiller
{
    /**
     * Apply annotation to fulfillers which should be evaluated at suite level.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AutoFulfillerSuiteLevel {}

    /**
     * Apply annotation to fulfillers which should be evaluated at testLevel.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AutoFulfillerTestLevel {}

    Set<State> fulfill(Set<Requirement> requirements);

    void cleanup();
}
