/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.assertions;

import org.assertj.core.api.AbstractListAssert;

import java.util.List;

/**
 * Interface used for passing lambda expression assertions into
 * {@link com.teradata.test.assertions.QueryAssert#column}
 */
public interface ColumnValuesAssert<T>
{

    void assertColumnValues(AbstractListAssert<?, ? extends List<? extends T>, T> columnAssert);
}
