package com.teradata.test.context;

import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiceTestContext
        implements TestContext
{

    private final Module module;
    private final Injector injector;

    public GuiceTestContext(Module module, Injector injector)
    {
        this.module = module;
        this.injector = injector;
    }

    @Override
    public <T> T getDependency(Class<T> dependencyClass)
    {
        return injector.getInstance(dependencyClass);
    }
}
