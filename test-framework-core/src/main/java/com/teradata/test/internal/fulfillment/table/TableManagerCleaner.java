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

package com.teradata.test.internal.fulfillment.table;


import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManagerDispatcher;

import java.util.Set;

/**
 * Calls dropAllTables on all table managers once per product-test execution, before other fulfillers
 */
public class TableManagerCleaner
    implements RequirementFulfiller
{
    @Inject
    TableManagerDispatcher tableManagerDispatcher;

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {
        tableManagerDispatcher.getAllTableManagers().stream().forEach(TableManager::dropAllTables);
        return Sets.newHashSet();
    }

    @Override
    public void cleanup()
    {
    }
}
