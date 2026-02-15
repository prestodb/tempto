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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.query.QueryExecutionException;
import io.prestodb.tempto.query.QueryResult;

import java.net.InetSocketAddress;
import java.sql.JDBCType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class CassandraQueryExecutor
        implements AutoCloseable
{
    private static final Map<DataType, JDBCType> typeMapping;
    private final CqlSession session;

    static {
        typeMapping = ImmutableMap.<DataType, JDBCType>builder()
                .put(DataTypes.ASCII, JDBCType.VARCHAR)
                .put(DataTypes.BIGINT, JDBCType.BIGINT)
                .put(DataTypes.BLOB, JDBCType.BLOB)
                .put(DataTypes.BOOLEAN, JDBCType.BOOLEAN)
                .put(DataTypes.COUNTER, JDBCType.BIGINT)
                .put(DataTypes.DATE, JDBCType.DATE)
                .put(DataTypes.DECIMAL, JDBCType.DECIMAL)
                .put(DataTypes.DOUBLE, JDBCType.DOUBLE)
                .put(DataTypes.FLOAT, JDBCType.REAL)
                .put(DataTypes.INT, JDBCType.INTEGER)
                .put(DataTypes.SMALLINT, JDBCType.SMALLINT)
                .put(DataTypes.TEXT, JDBCType.VARCHAR)
                .put(DataTypes.TIME, JDBCType.TIME)
                .put(DataTypes.TIMESTAMP, JDBCType.TIMESTAMP)
                .put(DataTypes.TINYINT, JDBCType.TINYINT)
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
        String host = configuration.getStringMandatory("databases.cassandra.host");
        int port = configuration.getIntMandatory("databases.cassandra.port");
        String dc = configuration.getString("databases.cassandra.datacenter").orElse("datacenter1");
        
        // Driver 4.x requires a local datacenter to be specified
        // Using "datacenter1" as the default, which is the standard for single-datacenter deployments
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter(dc)
                .build();
    }

    public QueryResult executeQuery(String sql)
            throws QueryExecutionException
    {
        checkState(!session.isClosed(), "Trying to execute query using closed Session");

        ResultSet rs = session.execute(sql);
        List<ColumnDefinition> definitions = newArrayList();
        for (ColumnDefinition def : rs.getColumnDefinitions()) {
            definitions.add(def);
        }
        
        List<JDBCType> types = definitions.stream()
                .map(definition -> getJDBCType(definition.getType()))
                .collect(toList());

        List<String> columnNames = definitions.stream()
                .map(ColumnDefinition::getName)
                .map(Object::toString)
                .collect(toList());

        QueryResult.QueryResultBuilder resultBuilder = new QueryResult.QueryResultBuilder(types, columnNames);

        for (Row row : rs) {
            List<Object> builderRow = newArrayList();
            for (int i = 0; i < types.size(); ++i) {
                builderRow.add(row.getObject(i));
            }
            resultBuilder.addRow(builderRow);
        }

        return resultBuilder.build();
    }

    public CqlSession getSession()
    {
        return session;
    }

    public List<String> getColumnNames(String keyspaceName, String tableName)
    {
        Optional<KeyspaceMetadata> keyspaceMetadata = session.getMetadata().getKeyspace(keyspaceName);
        if (!keyspaceMetadata.isPresent()) {
            throw new IllegalStateException(format("Keyspace %s does not exist", keyspaceName));
        }
        Optional<TableMetadata> tableMetadata = keyspaceMetadata.get().getTable(tableName);
        if (!tableMetadata.isPresent()) {
            throw new IllegalStateException(format("Table %s.%s does not exist", keyspaceName, tableName));
        }
        return tableMetadata.get().getColumns().values().stream()
                .map(ColumnMetadata::getName)
                .map(Object::toString)
                .collect(toList());
    }

    public boolean tableExists(String keyspaceName, String tableName)
    {
        Optional<KeyspaceMetadata> keyspaceMetadata = session.getMetadata().getKeyspace(keyspaceName);
        if (!keyspaceMetadata.isPresent()) {
            return false;
        }
        return keyspaceMetadata.get().getTable(tableName).isPresent();
    }

    public List<String> getTableNames(String keyspaceName)
    {
        Metadata clusterMetadata = session.getMetadata();
        Optional<KeyspaceMetadata> keyspaceMetadata = clusterMetadata.getKeyspace(keyspaceName);
        if (!keyspaceMetadata.isPresent()) {
            return ImmutableList.of();
        }
        return keyspaceMetadata.get().getTables().values().stream()
                .map(TableMetadata::getName)
                .map(Object::toString)
                .collect(toList());
    }

    @Override
    public void close()
    {
        if (session != null && !session.isClosed()) {
            session.close();
        }
    }

    private static JDBCType getJDBCType(DataType type)
    {
        JDBCType jdbcType = typeMapping.get(type);
        if (jdbcType == null) {
            throw new TypeNotSupportedException(type);
        }

        return jdbcType;
    }
}
