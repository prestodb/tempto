/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Requires
{
    Class<? extends RequirementsProvider>[] value();
}
