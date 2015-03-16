/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.initialization;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener2;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public abstract class TestSuiteAwareTestInvocationListener
        implements IInvokedMethodListener2
{
    // todo proper exception handling
    // todo make it work if test are executed in parallel. requires locking and changes to way we determine first and last method.

    @Override
    final public void beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context)
    {
        if (isFirstTestMethod(method, context)) {
            beforeSuite(context);
        }
        beforeTest(method, testResult, context);
    }

    @Override
    final public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context)
    {
        afterTest(method, testResult, context);
        if (isLastTestMethod(method, context)) {
            afterSuite(context);
        }
    }

    private boolean isFirstTestMethod(IInvokedMethod method, ITestContext context)
    {
        return context.getAllTestMethods()[0].equals(method.getTestMethod());
    }

    private boolean isLastTestMethod(IInvokedMethod method, ITestContext context)
    {
        ITestNGMethod[] allTestMethods = context.getAllTestMethods();
        return allTestMethods[allTestMethods.length - 1].equals(method.getTestMethod());
    }

    @Override
    final public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
    {
        // do nothing all happen in method with richer interface
    }

    @Override
    final public void afterInvocation(IInvokedMethod method, ITestResult testResult)
    {
        // do nothing all happen in method with richer interface
    }

    public abstract void beforeSuite(ITestContext context);

    public abstract void afterSuite(ITestContext context);

    public abstract void beforeTest(IInvokedMethod method, ITestResult testResult, ITestContext context);

    public abstract void afterTest(IInvokedMethod method, ITestResult testResult, ITestContext context);
}
