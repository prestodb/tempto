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
package io.prestodb.tempto.fulfillment.table.kafka;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import io.prestodb.tempto.fulfillment.table.MutableTableRequirement;
import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.internal.fulfillment.table.TableName;
import io.prestodb.tempto.query.QueryExecutor;
import io.prestodb.tempto.query.QueryResult;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;


@TableManager.Descriptor(tableDefinitionClass = KafkaTableDefinition.class, type = "KAFKA")
@Singleton
public class KafkaTableManager
        implements TableManager<KafkaTableDefinition>
{
    private static final Logger LOGGER = getLogger(KafkaTableManager.class);
    private final String databaseName;
    private final QueryExecutor prestoQueryExecutor;
    private final String brokerHost;
    private final Integer brokerPort;
    private final String prestoKafkaCatalog;

    @Inject
    public KafkaTableManager(
            @Named("databaseName") String databaseName,
            @Named("broker.host") String brokerHost,
            @Named("broker.port") int brokerPort,
            @Named("presto_database_name") String prestoDatabaseName,
            @Named("presto_kafka_catalog") String prestoKafkaCatalog,
            Injector injector)
    {
        this.databaseName = requireNonNull(databaseName, "databaseName is null");
        this.brokerHost = requireNonNull(brokerHost, "brokerHost is null");
        this.brokerPort = brokerPort;
        requireNonNull(injector, "injector is null");
        requireNonNull(prestoDatabaseName, "prestoDatabaseName is null");
        this.prestoQueryExecutor = injector.getInstance(Key.get(QueryExecutor.class, Names.named(prestoDatabaseName)));
        this.prestoKafkaCatalog = requireNonNull(prestoKafkaCatalog, "prestoKafkaCatalog is null");
    }

    @Override
    public TableInstance<KafkaTableDefinition> createImmutable(KafkaTableDefinition tableDefinition, TableHandle tableHandle)
    {
        verifyTableExistsInPresto(tableHandle.getSchema().orElseThrow(() -> new IllegalArgumentException("Schema required for Kafka tables")), tableHandle.getName());
        deleteTopic(tableDefinition.getTopic());
        createTopic(tableDefinition.getTopic(), tableDefinition.getPartitionsCount(), tableDefinition.getReplicationLevel());
        insertDataIntoTopic(tableDefinition.getTopic(), tableDefinition.getDataSource());
        TableName createdTableName = new TableName(
                tableHandle.getDatabase().orElse(getDatabaseName()),
                tableHandle.getSchema(),
                tableHandle.getName(),
                tableHandle.getName());
        return new KafkaTableInstance(createdTableName, tableDefinition);
    }

    private void withKafkaAdminClient(Consumer<AdminClient> routine) {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerHost + ":" + brokerPort);
        try (AdminClient adminClient = AdminClient.create(properties)) {
            routine.accept(adminClient);
        } catch (Exception e) {
            LOGGER.error("An error occurred while performing an operation with Kafka AdminClient on broker "
                + brokerHost + ":" + brokerPort);
            throw new RuntimeException("An error occurred while performing an operation with Kafka AdminClient", e);
        }
    }

    private void verifyTableExistsInPresto(String schema, String name)
    {
        String sql = format("select count(1) from %s.information_schema.tables where table_schema='%s' and table_name='%s'", prestoKafkaCatalog, schema, name);
        QueryResult queryResult = prestoQueryExecutor.executeQuery(sql);
        if ((long) queryResult.row(0).get(0) != 1) {
            throw new RuntimeException(format("Table %s.%s not defined if kafka catalog (%s)", schema, name, prestoKafkaCatalog));
        }
    }

    private void deleteTopic(String topic) {
        withKafkaAdminClient(adminClient -> {
            try {
                // Check if topic exists
                if (!adminClient.listTopics().names().get().contains(topic)) {
                    LOGGER.warn("Topic {} does not exist. Skipping deletion.", topic);
                    return;
                }
                DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singletonList(topic));
                deleteTopicsResult.all().get();
                try {
                    Thread.sleep(1_000); // Wait for metadata propagation
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for topic deletion: " + topic, e);
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Failed to delete topic " + topic, e);
            }
        });
    }

    private void createTopic(String topic, int partitionsCount, int replicationLevel) {
        withKafkaAdminClient(adminClient -> {
            NewTopic newTopic = new NewTopic(topic, partitionsCount, (short) replicationLevel);
            try {
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Failed to create topic " + topic, e);
            }
        });
    }

    private void insertDataIntoTopic(String topic, KafkaDataSource dataSource)
    {
        // create instance for properties to access producer configs
        Properties props = new Properties();

        props.put("bootstrap.servers", brokerHost + ":" + brokerPort);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        Producer<byte[], byte[]> producer = new KafkaProducer<>(props);

        Iterator<KafkaMessage> messages = dataSource.getMessages();
        while (messages.hasNext()) {
            KafkaMessage message = messages.next();
            try {
                producer.send(new ProducerRecord<>(
                        topic,
                        message.getPartition().isPresent() ? message.getPartition().getAsInt() : null,
                        message.getKey().orElse(null),
                        message.getValue())).get();
            }
            catch (Exception e) {
                throw new RuntimeException("could not send message to topic " + topic);
            }
        }
    }

    @Override
    public TableInstance<KafkaTableDefinition> createMutable(KafkaTableDefinition tableDefinition, MutableTableRequirement.State state, TableHandle tableHandle)
    {
        throw new IllegalArgumentException("Mutable tables are not supported by KafkaTableManager");
    }

    @Override
    public void dropTable(TableName tableName)
    {
        throw new IllegalArgumentException("dropTable not supported by KafkaTableManager");
    }

    @Override
    public void dropStaleMutableTables()
    {
        // noop
    }

    @Override
    public String getDatabaseName()
    {
        return databaseName;
    }

    @Override
    public Class<? extends TableDefinition> getTableDefinitionClass()
    {
        return KafkaTableDefinition.class;
    }
}
