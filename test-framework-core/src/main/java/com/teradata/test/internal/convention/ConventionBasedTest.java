/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.teradata.test.ProductTest;
import com.teradata.test.RequirementsProvider;

public abstract class ConventionBasedTest
        extends ProductTest
        implements RequirementsProvider
{
    public abstract void test();

    public abstract String testName();

    public abstract String testCaseName();

    public abstract String[] testGroups();
}
