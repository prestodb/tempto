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

package io.prestodb.tempto.context;

import io.prestodb.tempto.internal.context.TestContextStack;

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
    private final static ThreadLocal<TestContextStack<TestContext>> testContextStackThreadLocal = new InheritableThreadLocal<TestContextStack<TestContext>>()
    {
        protected TestContextStack<TestContext> childValue(TestContextStack<TestContext> parentTestContextStack)
        {
            if (parentTestContextStack != null) {
                checkState(!parentTestContextStack.empty());
                TestContextStack<TestContext> childTestContextStack = new TestContextStack<>();
                childTestContextStack.push(parentTestContextStack.peek());
                return childTestContextStack;
            }

            return null;
        }
    };

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

        return Optional.of(testContext());
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
