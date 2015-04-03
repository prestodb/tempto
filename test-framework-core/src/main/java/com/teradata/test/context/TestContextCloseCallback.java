package com.teradata.test.context;

@FunctionalInterface
public interface TestContextCloseCallback
{
    void testContextClosed(TestContext testContext);
}
