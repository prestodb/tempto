/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

import com.teradata.test.internal.context.TestContextStack;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Static helper for holding TestContext stack in a thread local variable.
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
    private final static ThreadLocal<TestContextStack<TestContext>> testContextStackThreadLocal = new InheritableThreadLocal<>();

    public static TestContext testContext()
    {
        assertTestContextSet();
        return testContextStackThreadLocal.get().peek();
    }

    public static Optional<TestContext> testContextIfSet()
    {
        if (testContextStackThreadLocal.get() == null) {
            return Optional.empty();
        }

        TestContextStack<TestContext> testContextStack = testContextStackThreadLocal.get();
        return !testContextStack.empty() ? Optional.of(testContextStack.peek()) : Optional.<TestContext>empty();
    }

    public static void runWithTextContext(TestContext testContext, Runnable runnable)
    {
        pushTestContext(testContext);
        try {
            runnable.run();
        }
        finally {
            popTestContext();
        }
    }

    public static void pushTestContext(TestContext testContext)
    {
        ensureTestContextStack();
        testContextStackThreadLocal.get().push(testContext);
    }

    public static TestContext popTestContext()
    {
        assertTestContextSet();

        TestContextStack<TestContext> testContextStack = testContextStackThreadLocal.get();
        TestContext testContext = testContextStack.pop();
        if (testContextStack.empty()) {
            testContextStackThreadLocal.remove();
        }
        return testContext;
    }

    public static void pushAllTestContexts(TestContextStack<? extends TestContext> testContextStack)
    {
        testContextStack.forEach(ThreadLocalTestContextHolder::pushTestContext);
    }

    public static TestContextStack<TestContext> popAllTestContexts()
    {
        TestContextStack<TestContext> testContextStack = testContextStackThreadLocal.get();
        testContextStackThreadLocal.remove();
        return testContextStack;
    }

    public static void assertTestContextNotSet()
    {
        checkState(testContextStackThreadLocal.get() == null, "test context should not be set for current thread");
    }

    public static void assertTestContextSet()
    {
        checkState(testContextStackThreadLocal.get() != null && !testContextStackThreadLocal.get().empty(), "test context not set for current thread");
    }

    private static void ensureTestContextStack()
    {
        if (testContextStackThreadLocal.get() == null) {
            testContextStackThreadLocal.set(new TestContextStack<>());
        }
    }

    private ThreadLocalTestContextHolder() {}
}
