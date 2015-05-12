/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

/**
 * Helper class used for execution of instances of {@link ContextRunnable} with context generated
 * by {@link ContextProvider#setup()}
 */
public final class ContextDsl
{

    public static <T> void executeWith(ContextProvider<T> provider, ContextRunnable<T> runnable)
    {
        T context = provider.setup();
        try {

            runnable.run(context);
        }
        finally {
            provider.cleanup(context);
        }
    }

    private ContextDsl()
    {
    }
}
