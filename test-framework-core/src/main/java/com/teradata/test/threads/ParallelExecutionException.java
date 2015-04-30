/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.threads;

import com.google.common.base.Joiner;

import java.util.List;

public class ParallelExecutionException
        extends RuntimeException
{
    private final List<Throwable> throwables;

    public ParallelExecutionException(List<Throwable> throwables)
    {
        super("Throwables when running parallel runnables:\n" + Joiner.on("-------------------\n").join(throwables));
        this.throwables = throwables;
    }

    public List<Throwable> getThrowables()
    {
        return throwables;
    }
}