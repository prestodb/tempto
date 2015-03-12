/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test;

@FunctionalInterface
public interface RequirementsProvider
{
    Requirement getRequirements();
}
