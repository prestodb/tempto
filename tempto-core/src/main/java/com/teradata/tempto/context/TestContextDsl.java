/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teradata.tempto.context;

import com.teradata.tempto.threads.IndexedRunnable;

import static com.teradata.tempto.context.ThreadLocalTestContextHolder.popTestContext;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.pushTestContext;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.testContext;

public final class TestContextDsl
{
    private TestContextDsl()
    {
    }

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
}
