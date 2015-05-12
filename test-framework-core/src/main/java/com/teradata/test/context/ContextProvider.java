/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

/**
 * Provider class used for generation and cleanup of dsl defined contexts.
 *
 * @param <T> context class
 */
public interface ContextProvider<T>
{
    /**
     * Method generating new context.
     *
     * @return generated context
     */
    T setup();

    /**
     * Method invoked after finishing {@link ContextRunnable#run}
     *
     * @param context dls defined context
     */
    void cleanup(T context);
}
