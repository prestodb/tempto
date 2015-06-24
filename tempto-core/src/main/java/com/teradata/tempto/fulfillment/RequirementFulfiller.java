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

package com.teradata.tempto.fulfillment;

import com.teradata.tempto.Requirement;
import com.teradata.tempto.context.State;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

public interface RequirementFulfiller
{

    public static final int DEFAULT_PRIORITY = 0;

    /**
     * Apply annotation to fulfillers which should be evaluated at suite level.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AutoSuiteLevelFulfiller
    {
        /**
         * With priority you can manage the order of fulfiller execution.
         * With higher priority fulfiller will {@link com.teradata.tempto.fulfillment.RequirementFulfiller#fulfill} sooner
         * and {@link com.teradata.tempto.fulfillment.RequirementFulfiller#cleanup()} later.
         *
         * @return priority level
         */
        int priority() default DEFAULT_PRIORITY;
    }

    /**
     * Apply annotation to fulfillers which should be evaluated at testLevel.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AutoTestLevelFulfiller
    {
        /**
         * With priority you can manage the order of fulfiller execution.
         * With higher priority fulfiller will {@link com.teradata.tempto.fulfillment.RequirementFulfiller#fulfill} sooner
         * and {@link com.teradata.tempto.fulfillment.RequirementFulfiller#cleanup()} later.
         *
         * @return priority level
         */
        int priority() default DEFAULT_PRIORITY;
    }

    Set<State> fulfill(Set<Requirement> requirements);

    void cleanup();
}
