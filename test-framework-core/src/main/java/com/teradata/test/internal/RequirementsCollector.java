/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal;

import com.teradata.test.CompositeRequirement;

import java.lang.reflect.Method;

public interface RequirementsCollector
{
    CompositeRequirement getAnnotationBasedRequirementsFor(Method method);
}
