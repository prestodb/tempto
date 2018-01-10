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

package io.prestodb.tempto.internal.context;

import io.prestodb.tempto.context.TestContext;

import java.util.Iterator;
import java.util.Stack;

public class TestContextStack<C extends TestContext>
        implements Iterable<C>
{
    private final Stack<C> testContextStack = new Stack<>();

    public void push(C testContext)
    {
        testContextStack.push(testContext);
    }

    public C pop()
    {
        return testContextStack.pop();
    }

    public C peek()
    {
        return testContextStack.peek();
    }

    public int size()
    {
        return testContextStack.size();
    }

    public boolean empty()
    {
        return testContextStack.empty();
    }

    @Override
    public Iterator<C> iterator()
    {
        return testContextStack.iterator();
    }
}
