/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Static helper for holding TestContext in thread local variable.
 * <p>
 * Justification for existence:
 * <p>
 * Using thread local for holding current TestContext is less explicit
 * and a bit hacky. But allows for having less verbose test building blocks.
 * <p>
 * We also do not require users to subclass a common context-aware test class
 * when they write tests.
 */
public final class ThreadLocalTestContextHolder
{
    private static InheritableThreadLocal<TestContext> testContextThreadLocal = new InheritableThreadLocal<>();

    public static TestContext testContext()
    {
        assertTestContextSet();
        return testContextThreadLocal.get();
    }

    public static Optional<TestContext> testContextIfSet()
    {
        TestContext testContext = testContextThreadLocal.get();
        return Optional.ofNullable(testContext);
    }

    public static void setTestContext(TestContext testContext)
    {
        assertTestContextNotSet();
        testContextThreadLocal.set(testContext);
    }

    public static void clearTestContext()
    {
        assertTestContextSet();
        testContextThreadLocal.set(null);
    }

    public static void assertTestContextNotSet()
    {
        checkState(testContextThreadLocal.get() == null, "test context should not be set for current thread");
    }

    public static void assertTestContextSet()
    {
        checkState(testContextThreadLocal.get() != null, "test context not set for current thread");
    }

    private ThreadLocalTestContextHolder() {}
}
