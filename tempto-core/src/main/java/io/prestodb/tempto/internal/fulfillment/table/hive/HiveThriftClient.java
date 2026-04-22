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
package io.prestodb.tempto.internal.fulfillment.table.hive;

import io.prestodb.tempto.fulfillment.table.hive.statistics.ColumnStatistics;
import io.prestodb.tempto.fulfillment.table.hive.statistics.TableStatistics;
import io.prestodb.tempto.internal.fulfillment.table.TableName;
import org.apache.hadoop.hive.metastore.api.ColumnStatisticsData;
import org.apache.hadoop.hive.metastore.api.ColumnStatisticsDesc;
import org.apache.hadoop.hive.metastore.api.ColumnStatisticsObj;
import org.apache.hadoop.hive.metastore.api.Date;
import org.apache.hadoop.hive.metastore.api.DateColumnStatsData;
import org.apache.hadoop.hive.metastore.api.Decimal;
import org.apache.hadoop.hive.metastore.api.DecimalColumnStatsData;
import org.apache.hadoop.hive.metastore.api.DoubleColumnStatsData;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.GetTableRequest;
import org.apache.hadoop.hive.metastore.api.LongColumnStatsData;
import org.apache.hadoop.hive.metastore.api.StringColumnStatsData;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.primitives.Shorts.checkedCast;
import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class HiveThriftClient
        implements AutoCloseable
{
    private final TTransport transport;
    private final ThriftHiveMetastore.Client client;

    public HiveThriftClient(String thriftHost, int thriftPort)
    {
        try {
            transport = new TSocket(thriftHost, thriftPort);
            transport.open();
        }
        catch (TTransportException e) {
            throw new RuntimeException(e);
        }
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        client = new ThriftHiveMetastore.Client(protocol);
    }

    void setStatistics(TableName tableName, TableStatistics tableStatistics)
    {
        try {
            GetTableRequest getTableRequest = new GetTableRequest(
                    getSchema(tableName), tableName.getSchemalessNameInDatabase());

            Table table = client.get_table_req(getTableRequest).getTable();
            setRowsCount(tableName, tableStatistics, table);
            try {
                setColumnStatistics(tableName, tableStatistics, table, fieldSchema -> true);
            }
            catch (TException ignore) {
                transport.close();
                transport.open();
                // try to avoid date type as it not supported in hive 1.1.0
                setColumnStatistics(tableName, tableStatistics, table, fieldSchema -> !fieldSchema.getType().equals("date"));
            }
        }
        catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    private void setRowsCount(TableName tableName, TableStatistics tableStatistics, Table table)
            throws TException
    {
        Map<String, String> tableParameters = table.getParameters();
        tableParameters.put("numRows", Long.toString(tableStatistics.getRowCount()));

        // setting other parameters just to pretend hive analyze task
        String dataSize = Long.toString(tableStatistics.getRowCount() * table.getSd().getCols().size() * 42);
        tableParameters.put("rawDataSize", dataSize);
        tableParameters.put("totalSize", dataSize);
        tableParameters.put("numFiles", "1");
        tableParameters.put("STATS_GENERATED_VIA_STATS_TASK", "true");

        client.alter_table(getSchema(tableName), tableName.getSchemalessNameInDatabase(), table);
    }

    private String getSchema(TableName tableName)
    {
        return tableName.getSchema().orElse("default");
    }

    private void setColumnStatistics(TableName tableName, TableStatistics tableStatistics, Table table, Predicate<FieldSchema> fieldsPredicate)
            throws TException
    {
        ColumnStatisticsDesc hiveColumnStatisticsDesc = new ColumnStatisticsDesc();
        hiveColumnStatisticsDesc.setIsTblLevel(false);
        hiveColumnStatisticsDesc.setDbName(getSchema(tableName));
        hiveColumnStatisticsDesc.setTableName(tableName.getSchemalessNameInDatabase());

        List<ColumnStatisticsObj> hiveColumnStatisticsObjs = table.getSd().getCols().stream()
                .filter(fieldsPredicate)
                .map(fieldSchema -> toHiveColumnStatistics(fieldSchema, tableStatistics.getColumns().get(fieldSchema.getName())))
                .collect(toList());

        org.apache.hadoop.hive.metastore.api.ColumnStatistics hiveColumnStatistics = new org.apache.hadoop.hive.metastore.api.ColumnStatistics();
        hiveColumnStatistics.setStatsDesc(hiveColumnStatisticsDesc);
        hiveColumnStatistics.setStatsObj(hiveColumnStatisticsObjs);
        client.update_table_column_statistics(hiveColumnStatistics);
    }

    private ColumnStatisticsObj toHiveColumnStatistics(FieldSchema fieldSchema, ColumnStatistics columnStatistics)
    {
        requireNonNull(columnStatistics, "columnStatistics is null");
        Optional<Object> min = columnStatistics.getMin();
        Optional<Object> max = columnStatistics.getMax();

        ColumnStatisticsData hiveColumnStatisticsData = new ColumnStatisticsData();
        String type = fieldSchema.getType();
        if (type.startsWith("varchar(") || type.startsWith("char(")) {
            int maxLength = parseInt(type.substring(type.indexOf("(") + 1).replaceAll("\\)", ""));
            StringColumnStatsData hiveColumnStatsData = new StringColumnStatsData();
            hiveColumnStatsData.setNumDVs(columnStatistics.getDistinctValuesCount());
            hiveColumnStatsData.setMaxColLen(maxLength);
            hiveColumnStatsData.setAvgColLen(maxLength / 3.0);
            hiveColumnStatsData.setNumNulls(columnStatistics.getNullsCount());
            hiveColumnStatisticsData.setStringStats(hiveColumnStatsData);
        }
        else if (type.equals("tinyint") || type.equals("smallint") || type.equals("int") || type.equals("bigint")) {
            LongColumnStatsData hiveColumnStatsData = new LongColumnStatsData();
            hiveColumnStatsData.setNumDVs(columnStatistics.getDistinctValuesCount());
            if (min.isPresent()) {
                checkState(max.isPresent());
                hiveColumnStatsData.setLowValue(((Number) min.get()).longValue());
                hiveColumnStatsData.setHighValue(((Number) max.get()).longValue());
            }
            hiveColumnStatsData.setNumNulls(columnStatistics.getNullsCount());
            hiveColumnStatisticsData.setLongStats(hiveColumnStatsData);
        }
        else if (type.startsWith("decimal(")) {
            int scale = parseInt(type.substring(type.indexOf(",") + 1).replaceAll("\\)", ""));
            DecimalColumnStatsData hiveColumnStatsData = new DecimalColumnStatsData();
            hiveColumnStatsData.setNumDVs(columnStatistics.getDistinctValuesCount());
            if (min.isPresent()) {
                checkState(max.isPresent());
                hiveColumnStatsData.setLowValue(toHiveDecimal(min.get(), scale));
                hiveColumnStatsData.setHighValue(toHiveDecimal(max.get(), scale));
            }
            hiveColumnStatsData.setNumNulls(columnStatistics.getNullsCount());
            hiveColumnStatisticsData.setDecimalStats(hiveColumnStatsData);
        }
        else if (type.equals("double")) {
            DoubleColumnStatsData hiveColumnStatsData = new DoubleColumnStatsData();
            hiveColumnStatsData.setNumDVs(columnStatistics.getDistinctValuesCount());
            if (min.isPresent()) {
                checkState(max.isPresent());
                hiveColumnStatsData.setLowValue((Double) min.get());
                hiveColumnStatsData.setHighValue((Double) max.get());
            }
            hiveColumnStatsData.setNumNulls(columnStatistics.getNullsCount());
            hiveColumnStatisticsData.setDoubleStats(hiveColumnStatsData);
        }
        else if (type.equals("date")) {
            DateColumnStatsData hiveColumnStatsData = new DateColumnStatsData();
            hiveColumnStatsData.setNumDVs(columnStatistics.getDistinctValuesCount());
            if (min.isPresent()) {
                checkState(max.isPresent());
                hiveColumnStatsData.setLowValue(new Date(((Number) min.get()).longValue()));
                hiveColumnStatsData.setHighValue(new Date(((Number) max.get()).longValue()));
            }
            hiveColumnStatsData.setNumNulls(columnStatistics.getNullsCount());
            hiveColumnStatisticsData.setDateStats(hiveColumnStatsData);
        }
        else {
            throw new IllegalStateException("Unsupported column type: " + type);
        }

        ColumnStatisticsObj hiveColumnStatistics = new ColumnStatisticsObj();
        hiveColumnStatistics.setColName(fieldSchema.getName());
        hiveColumnStatistics.setColType(type);
        hiveColumnStatistics.setStatsData(hiveColumnStatisticsData);
        return hiveColumnStatistics;
    }

    @Override
    public void close()
    {
        transport.close();
    }

    private static Decimal toHiveDecimal(Object objectValue, int scale)
    {
        double value = ((Number) objectValue).doubleValue();
        BigInteger bigInteger = BigInteger.valueOf(Math.round(value * scale));
        return new Decimal(checkedCast(scale), ByteBuffer.wrap(bigInteger.toByteArray()));
    }
}
