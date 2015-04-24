/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal;

import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.fulfillment.RequirementFulfiller.AutoSuiteLevelFulfiller;
import com.teradata.test.fulfillment.RequirementFulfiller.AutoTestLevelFulfiller;

import java.util.Comparator;

public class RequirementFulfillerByPriorityComparator
        implements Comparator<Class<? extends RequirementFulfiller>>
{

    @Override
    public int compare(Class<? extends RequirementFulfiller> o1, Class<? extends RequirementFulfiller> o2)
    {
        return getPriority(o1) - getPriority(o2);
    }

    private int getPriority(Class<? extends RequirementFulfiller> c)
    {
        if (c.getAnnotation(AutoSuiteLevelFulfiller.class) != null) {
            return c.getAnnotation(AutoSuiteLevelFulfiller.class).priority();
        }
        else if (c.getAnnotation(AutoTestLevelFulfiller.class) != null) {
            return c.getAnnotation(AutoTestLevelFulfiller.class).priority();
        }
        else {
            throw new RuntimeException(
                    String.format("Class '%s' is not annotated with '%' or '%s'.",
                            c.getName(), AutoSuiteLevelFulfiller.class.getName(), AutoTestLevelFulfiller.class.getName()));
        }
    }
}
