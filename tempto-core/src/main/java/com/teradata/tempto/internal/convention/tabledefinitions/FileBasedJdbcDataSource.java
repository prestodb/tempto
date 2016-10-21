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

package com.teradata.tempto.internal.convention.tabledefinitions;

import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource;
import com.teradata.tempto.spi.convention.tabledefinitions.ConventionTableDefinitionDescriptor;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.teradata.tempto.internal.convention.tabledefinitions.JdbcDataFileDescriptor.sqlResultDescriptorFor;
import static java.util.Collections.emptyIterator;

public class FileBasedJdbcDataSource
        implements JdbcTableDataSource
{
    private final ConventionTableDefinitionDescriptor tableDefinitionDescriptor;

    public FileBasedJdbcDataSource(ConventionTableDefinitionDescriptor tableDefinitionDescriptor)
    {
        this.tableDefinitionDescriptor = checkNotNull(tableDefinitionDescriptor, "tableDefinitionDescriptor is null");
    }

    @Override
    public Iterator<List<Object>> getDataRows()
    {
        return tableDefinitionDescriptor.getDataFile()
                .map(dataFile -> sqlResultDescriptorFor(dataFile).getRows().iterator())
                .orElse(emptyIterator());
    }
}
