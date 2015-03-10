/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test;

import java.util.Set;

/**
 * Specifies a sets of requirements.
 */
public interface CompositeRequirement extends Requirement {

    Set<Set<Requirement>> getRequirementsSets();
}
