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

package io.prestodb.tempto.fulfillment.table.hive;

import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.internal.fulfillment.table.TableName;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle;
import static io.prestodb.tempto.fulfillment.table.hive.InlineDataSource.createSameRowDataSource;
import static io.prestodb.tempto.fulfillment.table.hive.InlineDataSource.createStringDataSource;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class HiveTableDefinition
        extends TableDefinition
{
    private static final String NAME_MARKER = "%NAME%";
    private static final String LOCATION_MARKER = "%LOCATION%";
    private static final String PARTITION_SPEC_MARKER = "%PARTITION_SPEC%";
    private static final String EXTERNAL_MARKER = "%EXTERNAL%";

    private final Optional<HiveDataSource> dataSource;
    private final Optional<List<PartitionDefinition>> partitionDefinitions;
    private final String createTableDDLTemplate;
    private final Optional<Boolean> injectStats;

    private HiveTableDefinition(
            TableHandle handle,
            String createTableDDLTemplate,
            Optional<HiveDataSource> dataSource,
            Optional<List<PartitionDefinition>> partitionDefinitions,
            Optional<Boolean> injectStats)
    {
        super(handle);
        checkArgument(dataSource.isPresent() != partitionDefinitions.isPresent(), "either dataSource or partitionDefinitions must be set (but not both)");
        this.dataSource = dataSource;
        this.partitionDefinitions = partitionDefinitions;
        this.createTableDDLTemplate = createTableDDLTemplate;
        this.injectStats = requireNonNull(injectStats, "injectStats is null");

        checkArgument(createTableDDLTemplate.contains(NAME_MARKER), "Create table DDL must contain %NAME% placeholder");
    }

    public HiveDataSource getDataSource()
    {
        checkState(!isPartitioned(), "not supported for partitioned table");
        return dataSource.get();
    }

    public List<PartitionDefinition> getPartitionDefinitions()
    {
        checkState(isPartitioned(), "not supported for not partitioned table");
        return partitionDefinitions.get();
    }

    public boolean isPartitioned()
    {
        return partitionDefinitions.isPresent();
    }

    public String getCreateTableDDL(String name, Optional<String> location)
    {
        String ddl = createTableDDLTemplate.replace(NAME_MARKER, name);
        String external = "";
        if (location.isPresent()) {
            external = " EXTERNAL ";
            if (ddl.contains(LOCATION_MARKER)) {
                ddl = ddl.replace(LOCATION_MARKER, location.get());
            }
            else {
                ddl += " LOCATION '" + location.get() + "'";
            }
        }

        return ddl.replace(EXTERNAL_MARKER, external);
    }

    public Optional<Boolean> getInjectStats()
    {
        return injectStats;
    }

    public static HiveTableDefinition hiveTableDefinition(String name, String createTableDDLTemplate, HiveDataSource dataSource)
    {
        return hiveTableDefinition(tableHandle(name), createTableDDLTemplate, dataSource);
    }

    public static HiveTableDefinition hiveTableDefinition(TableHandle handle, String createTableDDLTemplate, HiveDataSource dataSource)
    {
        return new HiveTableDefinition(handle, createTableDDLTemplate, Optional.of(dataSource), Optional.empty(), Optional.empty());
    }

    public static HiveTableDefinitionBuilder builder(String name)
    {
        return new HiveTableDefinitionBuilder(name);
    }

    public static HiveTableDefinitionBuilder from(HiveTableDefinition initialDefinition)
    {
        return new HiveTableDefinitionBuilder(initialDefinition);
    }

    public static HiveTableDefinitionBuilder like(HiveTableDefinition hiveTableDefinition)
    {
        HiveTableDefinitionBuilder hiveTableDefinitionBuilder = new HiveTableDefinitionBuilder(hiveTableDefinition.getName());
        if (hiveTableDefinition.getSchema().isPresent()) {
            hiveTableDefinitionBuilder.inSchema(hiveTableDefinition.getSchema().get());
        }
        hiveTableDefinitionBuilder.setCreateTableDDLTemplate(hiveTableDefinition.createTableDDLTemplate);
        hiveTableDefinitionBuilder.setDataSource(hiveTableDefinition.getDataSource());
        return hiveTableDefinitionBuilder;
    }

    public static class HiveTableDefinitionBuilder
    {
        private TableHandle handle;
        private String createTableDDLTemplate;
        private Optional<HiveDataSource> dataSource = Optional.empty();
        private Optional<List<PartitionDefinition>> partitionDefinitions = Optional.empty();
        private Optional<Boolean> injectStats = Optional.empty();

        private HiveTableDefinitionBuilder(HiveTableDefinition initialDefinition)
        {
            requireNonNull(initialDefinition, "initialDefinition is null");
            this.handle = initialDefinition.handle;
            this.createTableDDLTemplate = initialDefinition.createTableDDLTemplate;
            this.dataSource = initialDefinition.dataSource;
            this.partitionDefinitions = initialDefinition.partitionDefinitions;
            this.injectStats = initialDefinition.injectStats;
        }

        private HiveTableDefinitionBuilder(String name)
        {
            handle = tableHandle(name);
        }

        public HiveTableDefinitionBuilder inDatabase(String database)
        {
            this.handle = handle.inDatabase(database);
            return this;
        }

        public HiveTableDefinitionBuilder inSchema(String schema)
        {
            this.handle = handle.inSchema(schema);
            return this;
        }

        public HiveTableDefinitionBuilder setName(String name)
        {
            this.handle = handle.withName(name);
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
            return setDataSource(createStringDataSource(handle.getName(), ""));
        }

        public HiveTableDefinitionBuilder setData(String revision, String data)
        {
            return setDataSource(createStringDataSource(handle.getName(), data));
        }

        public HiveTableDefinitionBuilder setRows(String revision, int rowsCount, String rowData)
        {
            return setDataSource(createSameRowDataSource(handle.getName(), 1, rowsCount, rowData));
        }

        public HiveTableDefinitionBuilder setRows(String revision, int splitCount, int rowsInEachSplit, String rowData)
        {
            return setDataSource(createSameRowDataSource(handle.getName(), splitCount, rowsInEachSplit, rowData));
        }

        public HiveTableDefinitionBuilder addPartition(String partitionSpec, HiveDataSource dataSource)
        {
            if (!partitionDefinitions.isPresent()) {
                partitionDefinitions = Optional.of(newArrayList());
            }
            partitionDefinitions.get().add(new PartitionDefinition(partitionSpec, dataSource));
            return this;
        }

        /**
         * Whether the table statistics should be loaded after table creation. If not set, configurable global default will be used.
         */
        public HiveTableDefinitionBuilder injectStats(boolean injectStats)
        {
            this.injectStats = Optional.of(injectStats);
            return this;
        }

        public HiveTableDefinition build()
        {
            return new HiveTableDefinition(handle, createTableDDLTemplate, dataSource, partitionDefinitions, injectStats);
        }
    }

    public static class PartitionDefinition
    {
        /**
         * Partition spec inserted into {@link #ADD_PARTITION_DDL_TEMPLATE}.
         */
        private final String partitionSpec;
        private final HiveDataSource dataSource;

        private static final String ADD_PARTITION_DDL_TEMPLATE =
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

        public String getAddPartitionTableDDL(TableName tableName, String location)
        {
            return ADD_PARTITION_DDL_TEMPLATE.replace(NAME_MARKER, tableName.getNameInDatabase())
                    .replace(PARTITION_SPEC_MARKER, partitionSpec)
                    .replace(LOCATION_MARKER, location);
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
