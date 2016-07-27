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

package com.teradata.tempto.fulfillment.table;

import java.util.List;

import static com.teradata.tempto.context.ThreadLocalTestContextHolder.testContext;

public class ImmutableTablesState
        extends TablesState
{

    public ImmutableTablesState(List<TableInstance> tables)
    {
        super(tables, "immutable table");
    }

    public static ImmutableTablesState immutableTablesState()
    {
        return testContext().getDependency(ImmutableTablesState.class);
    }
}
