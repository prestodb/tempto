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
package io.prestodb.tempto.internal.query;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.query.QueryExecutionException;
import io.prestodb.tempto.query.QueryResult;

import java.sql.JDBCType;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class CassandraQueryExecutor
        implements AutoCloseable
{
    private static final Map<DataType, JDBCType> typeMapping;
    private final Cluster cluster;
    private Session session;

    static {
        typeMapping = ImmutableMap.<DataType, JDBCType>builder()
                .put(DataType.ascii(), JDBCType.VARCHAR)
                .put(DataType.bigint(), JDBCType.BIGINT)
                .put(DataType.blob(), JDBCType.BLOB)
                .put(DataType.cboolean(), JDBCType.BOOLEAN)
                .put(DataType.counter(), JDBCType.BIGINT)
                .put(DataType.date(), JDBCType.DATE)
                .put(DataType.decimal(), JDBCType.DECIMAL)
                .put(DataType.cdouble(), JDBCType.DOUBLE)
                .put(DataType.cfloat(), JDBCType.REAL)
                .put(DataType.cint(), JDBCType.INTEGER)
                .put(DataType.smallint(), JDBCType.SMALLINT)
                //.put(DataType.text(), JDBCType.NVARCHAR)
                .put(DataType.time(), JDBCType.TIME)
                .put(DataType.timestamp(), JDBCType.TIMESTAMP)
                .put(DataType.tinyint(), JDBCType.TINYINT)
                .put(DataType.varchar(), JDBCType.VARCHAR)
                .build();
    }

    public static class TypeNotSupportedException
            extends IllegalStateException
    {
        TypeNotSupportedException(DataType type)
        {
            super(format("Type is not supported: %s.", type));
        }
    }

    public CassandraQueryExecutor(Configuration configuration)
    {
        cluster = Cluster.builder()
                .addContactPoint(configuration.getStringMandatory("databases.cassandra.host"))
                .withPort(configuration.getIntMandatory("databases.cassandra.port"))
                .build();
    }

    public QueryResult executeQuery(String sql)
            throws QueryExecutionException
    {
        ensureConnected();

        ResultSet rs = session.execute(sql);
        List<ColumnDefinitions.Definition> definitions = rs.getColumnDefinitions().asList();
        List<JDBCType> types = definitions.stream()
                .map(definition -> getJDBCType(definition.getType()))
                .collect(toList());

        List<String> columnNames = definitions.stream()
                .map(ColumnDefinitions.Definition::getName)
                .collect(toList());

        QueryResult.QueryResultBuilder resultBuilder = new QueryResult.QueryResultBuilder(types, columnNames);

        for (Row row : rs) {
            List<Object> builderRow = newArrayList();
            for (int i = 0; i < types.size(); ++i) {
                builderRow.add(row.getToken(i).getValue());
            }
            resultBuilder.addRow(builderRow);
        }

        return resultBuilder.build();
    }

    public Session getSession()
    {
        return session;
    }

    public List<String> getColumnNames(String keySpace, String tableName)
    {
        checkState(tableExists(keySpace, tableName), "table %s.%s does not exist", keySpace, tableName);
        KeyspaceMetadata keyspaceMetadata = session.getCluster().getMetadata().getKeyspace(keySpace);
        TableMetadata tableMetadata = keyspaceMetadata.getTable(tableName);
        return tableMetadata.getColumns().stream().map(ColumnMetadata::getName).collect(toList());
    }

    public boolean tableExists(String keySpace, String tableName)
    {
        KeyspaceMetadata keyspaceMetadata = cluster.getMetadata().getKeyspace(keySpace);
        if (keyspaceMetadata == null) {
            return false;
        }
        return keyspaceMetadata.getTable(tableName) != null;
    }

    public List<String> getTableNames(String keySpace)
    {
        Metadata clusterMetadata = cluster.getMetadata();
        KeyspaceMetadata keyspaceMetadata = clusterMetadata.getKeyspace(keySpace);
        if (keyspaceMetadata == null) {
            return ImmutableList.of();
        }
        return keyspaceMetadata.getTables().stream()
                .map(TableMetadata::getName)
                .collect(toList());
    }

    @Override
    public void close()
    {
        cluster.close();
    }

    private void ensureConnected()
    {
        checkState(!cluster.isClosed(), "Trying to connect using closed Cluster");

        if (session == null || session.isClosed()) {
            session = cluster.connect();
        }
    }

    private static JDBCType getJDBCType(DataType type)
    {
        JDBCType jdbcType = typeMapping.get(type);
        if (type == null) {
            throw new TypeNotSupportedException(type);
        }

        return jdbcType;
    }
}
