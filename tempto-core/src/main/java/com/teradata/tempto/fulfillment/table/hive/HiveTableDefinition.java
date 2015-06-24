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

package com.teradata.tempto.fulfillment.table.hive;

import com.teradata.tempto.fulfillment.table.TableDefinition;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.tempto.fulfillment.table.hive.InlineDataSource.createSameRowDataSource;
import static com.teradata.tempto.fulfillment.table.hive.InlineDataSource.createStringDataSource;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class HiveTableDefinition
        extends TableDefinition
{
    private static final String NAME_MARKER = "%NAME%";
    private static final String LOCATION_MARKER = "%LOCATION%";
    private static final String PARTITION_SPEC_MARKER = "%PARTITION_SPEC%";
    private static final String NO_DATA_REVISION = "NO_DATA_REVISION";

    private final Optional<HiveDataSource> dataSource;
    private final Optional<List<PartitionDefinition>> partitionDefinitions;
    private final String createTableDDLTemplate;

    private HiveTableDefinition(String name, String createTableDDLTemplate, Optional<HiveDataSource> dataSource, Optional<List<PartitionDefinition>> partitionDefinitions)
    {
        super(name);
        checkArgument(dataSource.isPresent() ^ partitionDefinitions.isPresent(), "either dataSource or partitionDefinitions must be set");
        this.dataSource = dataSource;
        this.partitionDefinitions = partitionDefinitions;
        this.createTableDDLTemplate = createTableDDLTemplate;

        checkArgument(createTableDDLTemplate.contains(NAME_MARKER), "Create table DDL must contain %NAME% placeholder");
        if (partitionDefinitions.isPresent()) {
            checkArgument(!createTableDDLTemplate.contains(LOCATION_MARKER), "Create table DDL must contain not %LOCATION% placeholder for partitioned table");
        }
        else {
            checkArgument(createTableDDLTemplate.contains(LOCATION_MARKER), "Create table DDL must contain %LOCATION% placeholder");
        }
    }

    public HiveDataSource getDataSource()
    {
        checkState(!isPartitioned(), "not supported for partitioned table");
        return dataSource.get();
    }

    public List<PartitionDefinition> getPartitionDefinitons()
    {
        checkState(isPartitioned(), "not supported for not partitioned table");
        return partitionDefinitions.get();
    }

    public boolean isPartitioned()
    {
        return partitionDefinitions.isPresent();
    }

    public String getCreateTableDDL(String name, String location)
    {
        return createTableDDLTemplate.replace(NAME_MARKER, name).replace(LOCATION_MARKER, location);
    }

    public static HiveTableDefinition hiveTableDefinition(String name, String createTableDDLTemplate, HiveDataSource dataSource)
    {
        return new HiveTableDefinition(name, createTableDDLTemplate, Optional.of(dataSource), Optional.empty());
    }

    public static HiveTableDefinitionBuilder builder()
    {
        return new HiveTableDefinitionBuilder();
    }

    public static HiveTableDefinitionBuilder like(HiveTableDefinition hiveTableDefinition)
    {
        HiveTableDefinitionBuilder hiveTableDefinitionBuilder = new HiveTableDefinitionBuilder();
        hiveTableDefinitionBuilder.setName(hiveTableDefinition.getName());
        hiveTableDefinitionBuilder.setCreateTableDDLTemplate(hiveTableDefinition.createTableDDLTemplate);
        hiveTableDefinitionBuilder.setDataSource(hiveTableDefinition.getDataSource());
        return hiveTableDefinitionBuilder;
    }

    public static class HiveTableDefinitionBuilder
    {

        private String name;
        private String createTableDDLTemplate;
        private Optional<HiveDataSource> dataSource = Optional.empty();
        private Optional<List<PartitionDefinition>> partitionDefinitions = Optional.empty();

        private HiveTableDefinitionBuilder()
        {
        }

        public HiveTableDefinitionBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public HiveTableDefinitionBuilder setCreateTableDDLTemplate(String createTableDDLTemplate)
        {
            this.createTableDDLTemplate = createTableDDLTemplate;
            return this;
        }

        public HiveTableDefinitionBuilder setDataSource(HiveDataSource dataSource)
        {
            this.dataSource = Optional.of(dataSource);
            return this;
        }

        public HiveTableDefinitionBuilder setNoData()
        {
            return setDataSource(createStringDataSource(name, NO_DATA_REVISION, ""));
        }

        public HiveTableDefinitionBuilder setData(String revision, String data)
        {
            return setDataSource(createStringDataSource(name, revision, data));
        }

        public HiveTableDefinitionBuilder setRows(String revision, int rowsCount, String rowData)
        {
            return setDataSource(createSameRowDataSource(name, revision, 1, rowsCount, rowData));
        }

        public HiveTableDefinitionBuilder setRows(String revision, int splitCount, int rowsInEachSplit, String rowData)
        {
            return setDataSource(createSameRowDataSource(name, revision, splitCount, rowsInEachSplit, rowData));
        }

        public HiveTableDefinitionBuilder addPartition(String partitionSpec, HiveDataSource dataSource) {
            if (!partitionDefinitions.isPresent()) {
                partitionDefinitions = Optional.of(newArrayList());
            }
            partitionDefinitions.get().add(new PartitionDefinition(partitionSpec, dataSource));
            return this;
        }

        public HiveTableDefinition build()
        {
            return new HiveTableDefinition(name, createTableDDLTemplate, dataSource, partitionDefinitions);
        }
    }

    public static class PartitionDefinition
    {
        /**
         * Partition spec inserted into {link #ADD_PARTION_DDL_TEMPLATE}.
         */
        private final String partitionSpec;
        private final HiveDataSource dataSource;

        private static final String ADD_PARTION_DDL_TEMPLATE =
                "ALTER TABLE " + NAME_MARKER +
                        " ADD PARTITION (" + PARTITION_SPEC_MARKER + ")" +
                        " LOCATION '" + LOCATION_MARKER + "'";

        public PartitionDefinition(String partitionSpec, HiveDataSource dataSource)
        {
            this.partitionSpec = partitionSpec;
            this.dataSource = dataSource;
        }

        public String getPartitionSpec()
        {
            return partitionSpec;
        }

        public HiveDataSource getDataSource()
        {
            return dataSource;
        }

        public String getAddPartitionTableDDL(String tableName, String location)
        {
            return ADD_PARTION_DDL_TEMPLATE.replace(NAME_MARKER, tableName).replace(PARTITION_SPEC_MARKER, partitionSpec).replace(LOCATION_MARKER, location);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }
}
