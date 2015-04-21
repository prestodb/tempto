/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.initialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link SuiteModuleProvider}s and {@link TestMethodModuleProvider}s
 * annotated with this annotation will be automatically class scanned for.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AutoModuleProvider
{
}
