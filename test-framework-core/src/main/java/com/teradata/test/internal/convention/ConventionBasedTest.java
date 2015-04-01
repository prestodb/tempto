/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.teradata.test.ProductTest;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.testmarkers.WithName;
import com.teradata.test.testmarkers.WithTestGroups;

public abstract class ConventionBasedTest
        extends ProductTest
        implements RequirementsProvider, WithName, WithTestGroups
{
}
