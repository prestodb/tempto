/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.listeners;

import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Responsible for transforming the {@link org.testng.annotations.Test} annotations in the following ways:
 *
 * <ol>
 *   <li>Ensure that the annotation is only on a test method and not a class or constructor</li>
 * </ol>
 */
public class ProductTestAnnotationTransformer implements IAnnotationTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductTestAnnotationTransformer.class);

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        ensureAnnotationIsOnlyOnTestMethod(testClass, testConstructor, testMethod);
    }

    private static void ensureAnnotationIsOnlyOnTestMethod(Class testClass, Constructor testConstructor, Method testMethod) {
        if (testMethod == null) {
            String annotatedObjectName = (testClass != null) ? testClass.getName() : testConstructor.getName();
            LOGGER.error("Cannot annotate '{}' with @Test in the product tests, only a method can be a product test", annotatedObjectName);
            throw new IllegalStateException("Illegal @Test annotation on " + annotatedObjectName);
        }
    }
}
