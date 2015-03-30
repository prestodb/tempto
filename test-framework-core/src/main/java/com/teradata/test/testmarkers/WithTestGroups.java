/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.testmarkers;

import org.testng.ITest;

import java.util.Set;

public interface WithTestGroups
        extends ITest
{
    Set<String> getTestGroups();
}
