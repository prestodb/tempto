/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

import static com.google.common.base.Preconditions.checkState;

/**
 * Static helper for holding TestContext in thread local variable.
 *
 * Justification for existence:
 *
 * Using thread local for holding current TestContext is less explicit
 * and a bit hacky. But allows for having less verbose test building blocks.
 *
 * We also do not require users to subclass a common context-aware test class
 * when they write tests.
 */
public class ThreadLocalTestContextHolder
{
    private static ThreadLocal<TestContext> testContextThreadLocal = new ThreadLocal<>();

    public static void assertTestContextNotSet() {
        checkState(testContextThreadLocal.get() == null);
    }

    public static void clearTestContext() {
        testContextThreadLocal.set(null);
    }

    public static void setTestContext(TestContext testContext) {
        testContextThreadLocal.set(testContext);
    }

    public static TestContext testContext() {
        TestContext testContext = testContextThreadLocal.get();
        checkState(testContext != null, "test context not set for current thread");
        return testContext;
    }
}
