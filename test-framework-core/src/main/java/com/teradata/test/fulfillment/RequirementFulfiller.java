package com.teradata.test.fulfillment;

import com.teradata.test.Requirement;
import com.teradata.test.context.State;

import java.util.List;

public interface RequirementFulfiller
{
    // todo do more thinking on this

    List<State> fulfill(List<Requirement> requirements);

    void cleanup();
}
