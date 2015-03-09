/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import java.util.Set;

/**
 * Specifies a sets of requirements.
 */
@FunctionalInterface
public interface RequirementsInfo {

    Set<Set<Requirement>> getRequirements();
}
