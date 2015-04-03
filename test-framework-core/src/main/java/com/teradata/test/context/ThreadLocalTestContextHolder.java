/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

import com.teradata.test.internal.context.TestContextStack;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Static helper for holding TestContext stack in thread local variable.
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
    private final static InheritableThreadLocal<TestContextStack<TestContext>> testContextStackThreadLocal = new InheritableThreadLocal<TestContextStack<TestContext>>()
    {
        @Override
        protected TestContextStack<TestContext> initialValue()
        {
            return new TestContextStack<>();
        }
    };

    public static TestContext testContext()
    {
        assertTestContextSet();
        return testContextStackThreadLocal.get().peek();
    }

    public static Optional<TestContext> testContextIfSet()
    {
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
        testContextStackThreadLocal.get().push(testContext);
    }

    public static TestContext popTestContext()
    {
        assertTestContextSet();
        return testContextStackThreadLocal.get().pop();
    }

    public static void pushAllTestContexts(TestContextStack<? extends TestContext> testContextStack)
    {
        testContextStack.forEach(com.teradata.test.context.ThreadLocalTestContextHolder::pushTestContext);
    }

    public static TestContextStack<TestContext> popAllTestContexts()
    {
        TestContextStack<TestContext> testContextStack = testContextStackThreadLocal.get();
        testContextStackThreadLocal.remove();
        return testContextStack;
    }

    public static void assertTestContextNotSet()
    {
        checkState(testContextStackThreadLocal.get().empty(), "test context should not be set for current thread");
    }

    public static void assertTestContextSet()
    {
        checkState(!testContextStackThreadLocal.get().empty(), "test context not set for current thread");
    }

    private ThreadLocalTestContextHolder() {}
}
