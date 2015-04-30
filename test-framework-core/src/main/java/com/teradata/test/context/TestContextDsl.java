/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.context;

import static com.teradata.test.context.ThreadLocalTestContextHolder.popTestContext;
import static com.teradata.test.context.ThreadLocalTestContextHolder.pushTestContext;

public final class TestContextDsl
{
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

    private TestContextDsl()
    {
    }
}
