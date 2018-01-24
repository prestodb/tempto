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

package io.prestodb.tempto.internal.convention;

import com.google.common.base.Splitter;
import io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State;
import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.internal.convention.AnnotatedFileParser.SectionParsingResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static io.prestodb.tempto.query.QueryExecutor.DEFAULT_DB_NAME;
import static java.util.stream.Collectors.toSet;

public class SqlQueryDescriptor
        extends SqlDescriptor
{
    private static final String GROUPS_HEADER_PROPERTY = "groups";
    private static final String DATABASE_HEADER_PROPERTY = "database";
    private static final String TABLES_HEADER_PROPERTY = "tables";
    private static final String MUTABLE_TABLES_HEADER_PROPERTY = "mutable_tables";
    private static final String REQUIRES_HEADER_PROPERTY = "requires";

    // mutable table property format is: mutable_table_name|state|name
    private static final Splitter MUTABLE_TABLE_PROPERTY_SPLITTER = Splitter.on('|');
    private static final int MUTABLE_TABLE_DEFINITION_NAME_PROPERTY_INDEX = 0;
    private static final int MUTABLE_TABLE_STATE_PROPERTY_INDEX = 1;
    private static final int MUTABLE_TABLE_NAME_PROPERTY_INDEX = 2;

    public SqlQueryDescriptor(SectionParsingResult sqlSectionParsingResult)
    {
        this(sqlSectionParsingResult, newHashMap());
    }

    public SqlQueryDescriptor(SectionParsingResult sqlSectionParsingResult, Map<String, String> baseProperties)
    {
        super(sqlSectionParsingResult, baseProperties);
    }

    public String getDatabaseName()
    {
        return getPropertyValue(DATABASE_HEADER_PROPERTY).orElse(DEFAULT_DB_NAME);
    }

    public Set<TableHandle> getTableDefinitionHandles()
    {
        return getPropertyValues(TABLES_HEADER_PROPERTY).stream()
                .map(TableHandle::parse)
                .collect(toSet());
    }

    public List<MutableTableDescriptor> getMutableTableDescriptors()
    {
        List<String> mutableTableValues = getPropertyValues(MUTABLE_TABLES_HEADER_PROPERTY);
        List<MutableTableDescriptor> mutableTableDescriptors = newArrayList();
        for (String mutableTableValue : mutableTableValues) {
            List<String> properties = newArrayList(MUTABLE_TABLE_PROPERTY_SPLITTER.split(mutableTableValue));

            checkState(properties.size() >= 1, "At least mutable table name must be specified, format is: mutable_table_name|state|name_in_database");
            checkState(properties.size() <= 3, "Too many mutable table properties, format is: mutable_table_name|state|name_in_database");

            String tableDefinitionName = properties.get(MUTABLE_TABLE_DEFINITION_NAME_PROPERTY_INDEX);
            State state = properties.size() >= 2 ? State.valueOf(properties.get(MUTABLE_TABLE_STATE_PROPERTY_INDEX).toUpperCase()) : LOADED;
            String rawTableName = properties.size() >= 3 ? properties.get(MUTABLE_TABLE_NAME_PROPERTY_INDEX) : tableDefinitionName;
            TableHandle tableHandle = TableHandle.parse(rawTableName);

            checkState(!mutableTableDescriptors
                            .stream()
                            .filter(mutableTableDescriptor -> mutableTableDescriptor.tableHandle.equals(tableHandle))
                            .findAny().isPresent(),
                    "Table with name %s is defined twice", tableHandle.getName());

            mutableTableDescriptors.add(new MutableTableDescriptor(tableDefinitionName, state, tableHandle));
        }

        return mutableTableDescriptors;
    }

    public Set<String> getTestGroups()
    {
        return getPropertyValuesSet(GROUPS_HEADER_PROPERTY);
    }

    public Set<String> getRequirementClassNames()
    {
        return getPropertyValuesSet(REQUIRES_HEADER_PROPERTY);
    }
}
