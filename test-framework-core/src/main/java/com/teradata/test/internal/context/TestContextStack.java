package com.teradata.test.internal.context;

import com.teradata.test.context.TestContext;

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
