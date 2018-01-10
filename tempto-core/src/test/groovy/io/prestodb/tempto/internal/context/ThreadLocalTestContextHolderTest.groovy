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

package io.prestodb.tempto.internal.context

import io.prestodb.tempto.context.TestContext
import org.apache.commons.lang3.tuple.Pair
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static io.prestodb.tempto.context.TestContextDsl.withChildTestContext
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.assertTestContextNotSet
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.popAllTestContexts
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.popTestContext
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.pushTestContext
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.testContext
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.testContextIfSet

class ThreadLocalTestContextHolderTest
        extends Specification
{
    void cleanup()
    {
        if (testContextIfSet().isPresent()) {
            popAllTestContexts();
        }
    }

    def "assertNotSet does not throw if unset"()
    {
        setup:
        assertTestContextNotSet()
        // works fine
    }

    def "assertNotSet throws if set"()
    {
        setup:
        pushTestContext(Mock(TestContext))

        when:
        assertTestContextNotSet()

        then:
        thrown(IllegalStateException)
    }

    def "getting testContext throws if not set"()
    {
        when:
        testContext()

        then:
        thrown(IllegalStateException)
    }

    def "getting testContext returns what was set"()
    {
        setup:
        TestContext mockTestContext = Mock()
        pushTestContext(mockTestContext)

        expect:
        testContext() == mockTestContext
    }

    def "empty test context should not propagate from parent to child"()
    {
        setup:
        TestContext mockTestContext = Mock()

        pushTestContext(mockTestContext)
        popTestContext()

        runAndJoin(new Runnable() {
            @Override
            void run()
            {
                assertTestContextNotSet()
                pushTestContext(mockTestContext)
            }
        })

        assertTestContextNotSet()
    }

    def "parent test context after child start should not propagate to child"()
    {
        setup:
        TestContext mockTestContext = Mock()

        CountDownLatch latch = new CountDownLatch(1)
        def threadAndThrowables = run(new Runnable() {
            @Override
            void run()
            {
                latch.await()
                assertTestContextNotSet()
            }
        })

        pushTestContext(mockTestContext)
        latch.countDown()

        join(threadAndThrowables)
    }

    def "test context should propagate from parent to child"()
    {
        setup:
        TestContext mockTestContext = Mock()
        TestContext childTestContext = Mock()
        mockTestContext.createChildContext() >> childTestContext

        pushTestContext(mockTestContext)

        runAndJoin(new Runnable() {
            @Override
            void run()
            {
                assert testContext() == mockTestContext
                popTestContext()
            }
        })

        runAndJoin(new Runnable() {
            @Override
            void run()
            {
                assert testContext() == mockTestContext
                popTestContext()
            }
        })

        runAndJoin(withChildTestContext(new Runnable() {
            @Override
            void run()
            {
                assert testContext() == childTestContext
                popTestContext()
            }
        }))

        assert testContext() == mockTestContext
    }

    def run(Runnable runnable)
    {
        def throwables = []
        def thread = new Thread(new Runnable() {
            @Override
            void run()
            {
                try {
                    runnable.run()
                }
                catch (Throwable e) {
                    throwables.add(e)
                }
            }
        })

        thread.start()

        return Pair.of(thread, throwables)
    }

    def join(Pair<Thread, List<Throwable>> threadAndThrowables)
    {
        threadAndThrowables.left.join()

        if (!threadAndThrowables.right.isEmpty()) {
            throw threadAndThrowables.right[0]
        }
    }

    def runAndJoin(Runnable runnable)
    {
        join(run(runnable))
    }
}
