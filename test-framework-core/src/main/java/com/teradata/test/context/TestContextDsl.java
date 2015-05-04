/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.context;

import com.teradata.test.threads.IndexedRunnable;

import static com.teradata.test.context.ThreadLocalTestContextHolder.popTestContext;
import static com.teradata.test.context.ThreadLocalTestContextHolder.pushTestContext;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;

public final class TestContextDsl
{
    public static IndexedRunnable withChildTestContext(IndexedRunnable runnable)
    {
        return (int threadIndex) -> {
            pushTestContext(testContext().createChildContext());
            try {
                runnable.run(threadIndex);
            }
            finally {
                popTestContext();
            }
        };
    }

    public static Runnable withChildTestContext(Runnable runnable)
    {
        return () -> runWithChildTestContext(runnable);
    }

    public static void runWithChildTestContext(Runnable runnable)
    {
        runWithTestContext(testContext().createChildContext(), runnable);
    }

    public static void runWithTestContext(TestContext testContext, Runnable runnable)
    {
        pushTestContext(testContext);
        try {
            runnable.run();
        }
        finally {
            popTestContext();
        }
    }

    private TestContextDsl()
    {
    }
}
