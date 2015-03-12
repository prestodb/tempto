/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment;

import com.teradata.test.Requirement;
import com.teradata.test.context.State;

import java.util.List;

public interface RequirementFulfiller<S extends State>
{
    // todo do more thinking on this

    List<S> fulfill(List<Requirement> requirements);

    void cleanup();
}
