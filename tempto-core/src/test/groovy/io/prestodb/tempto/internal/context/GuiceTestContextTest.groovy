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

import com.google.inject.Binder
import com.google.inject.Module
import io.prestodb.tempto.context.State
import io.prestodb.tempto.context.TestContextCloseCallback
import spock.lang.Specification

class GuiceTestContextTest
        extends Specification
{
    private static final def A = 'A'
    private static final def B = 'B'
    private static final def C = 'C'

    def 'test get dependency'()
    {
        setup:
        def context = new GuiceTestContext()
        def state = new DummyState()

        expect:
        assert context.createChildContext(state).getDependency(DummyState) == state
    }

    def 'test override'()
    {
        setup:
        def state1 = new DummyState(A)
        def state2 = new DummyState(B)
        def context1 = new GuiceTestContext(new Module() {
            @Override
            void configure(Binder binder)
            {
                binder.bind(DummyState).toInstance(state1)
            }
        })

        def context2 = context1.createChildContext([], [new Module() {
            @Override
            void configure(Binder binder)
            {
                binder.bind(DummyState).toInstance(state2)
            }
        }])

        expect:
        assert context1.getDependency(DummyState) == state1
        assert context2.getDependency(DummyState) == state2
    }

    def 'test spawning no naming'()
    {
        setup:
        def context = new GuiceTestContext()
        def state = new DummyState()

        expect:
        assert context.createChildContext(state).getDependency(DummyState) == state
    }

    def 'test spawning external naming'()
    {
        setup:
        def context = new GuiceTestContext()
        def state = new DummyState(A)

        expect:
        assert context.createChildContext(state).getDependency(DummyState, A) == state
    }

    def 'test context close'()
    {
        setup:
        def context1 = new GuiceTestContext()
        def context2 = context1.createChildContext([])

        def callback1 = Mock(TestContextCloseCallback)
        def callback2 = Mock(TestContextCloseCallback)

        context1.registerCloseCallback(callback1)
        context2.registerCloseCallback(callback2)

        when:
        context1.close()

        then:
        1 * callback2.testContextClosed(context2)
        then:
        1 * callback1.testContextClosed(context1)

        when:
        context2.close()
        context1.close()

        then:
        1 * callback2.testContextClosed(context2)
        then:
        1 * callback1.testContextClosed(context1)

        when:
        context2.close()

        then:
        1 * callback2.testContextClosed(context2)
    }

    private class DummyState
            implements State
    {
        private final Optional<String> name;

        DummyState(String name = null)
        {
            this.name = Optional.ofNullable(name)
        }

        @Override
        Optional<String> getName()
        {
            return name
        }
    }

    private static class DummyClass
    {
    }
}
