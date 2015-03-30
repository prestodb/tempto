/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.listeners;

import com.teradata.test.testmarkers.WithName;
import com.teradata.test.testmarkers.WithTestGroups;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

/**
 * Helper class for obtaining test method metadata (name, groups etc) which
 * is usable in multiple places in test framework.
 */
public class TestMetadataReader
{
    public static class TestMetadata {
        public final Set<String> testGroups;
        public final String testName;

        public TestMetadata(Set<String> testGroups, String testName) {
            this.testGroups = testGroups;
            this.testName = testName;
        }
    }

    public TestMetadata getTestMetadata(ITestResult testResult) {
        return getTestMetadata(testResult.getMethod());
    }

    public TestMetadata getTestMetadata(ITestNGMethod testMethod) {
        return new TestMetadata(
                getTestGroups(testMethod),
                getTestName(testMethod));
    }

    private Set<String> getTestGroups(ITestNGMethod method)
    {
        if (method.isTest() && method.getInstance() instanceof WithTestGroups) {
            return (((WithTestGroups) method.getInstance()).getTestGroups());
        }
        return newHashSet(asList(method.getGroups()));
    }

    private String getTestName(ITestNGMethod method)
    {
        if (method.isTest() && method.getInstance() instanceof WithName) {
            return ((WithName) method.getInstance()).getTestName();
        }
        return method.getTestClass().getName() + "." + method.getMethodName();
    }

}
