/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment;

import com.teradata.test.Requirement;
import com.teradata.test.context.State;

import java.util.Set;

public interface RequirementFulfiller
{
    Set<State> fulfill(Set<Requirement> requirements);

    void cleanup();
}
