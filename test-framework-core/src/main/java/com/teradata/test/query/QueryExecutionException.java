/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

public class QueryExecutionException
        extends RuntimeException
{
    public QueryExecutionException(Throwable e)
    {
        super(e);
    }
}
