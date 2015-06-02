/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tempto.internal;

import com.teradata.tempto.fulfillment.RequirementFulfiller;
import com.teradata.tempto.fulfillment.RequirementFulfiller.AutoSuiteLevelFulfiller;
import com.teradata.tempto.fulfillment.RequirementFulfiller.AutoTestLevelFulfiller;

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
