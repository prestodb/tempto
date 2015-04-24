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

    public static final int DEFAULT_PRIORITY = 0;

    /**
     * Apply annotation to fulfillers which should be evaluated at suite level.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AutoSuiteLevelFulfiller
    {
        /**
         * With priority you can manage the order of fulfiller execution.
         * With higher priority fulfiller will {@link com.teradata.test.fulfillment.RequirementFulfiller#fulfill()} sooner
         * and {@link com.teradata.test.fulfillment.RequirementFulfiller#clenup()} later.
         */
        int priority() default DEFAULT_PRIORITY;
    }

    /**
     * Apply annotation to fulfillers which should be evaluated at testLevel.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AutoTestLevelFulfiller
    {
        /**
         * With priority you can manage the order of fulfiller execution.
         * With higher priority fulfiller will {@link com.teradata.test.fulfillment.RequirementFulfiller#fulfill()} sooner
         * and {@link com.teradata.test.fulfillment.RequirementFulfiller#clenup()} later.
         */
        int priority() default DEFAULT_PRIORITY;
    }

    Set<State> fulfill(Set<Requirement> requirements);

    void cleanup();
}
