/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.threads;

/**
 * An interface of a runnable that has an index and can throw checked exceptions.
 */
@FunctionalInterface
public interface IndexedRunnable
{
    /**
     * @param threadIndex an index of thread that executes this {@link IndexedRunnable}.
     */
    void run(int threadIndex)
            throws Exception;
}
