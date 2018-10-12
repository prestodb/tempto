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

package io.prestodb.tempto.internal.hadoop.hdfs;

import io.prestodb.tempto.fulfillment.table.hive.HiveDataSource;
import io.prestodb.tempto.hadoop.hdfs.HdfsClient;
import io.prestodb.tempto.hadoop.hdfs.HdfsClient.RepeatableContentProducer;
import org.slf4j.Logger;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

public class DefaultHdfsDataSourceWriter
        implements HdfsDataSourceWriter
{
    private static final Logger LOGGER = getLogger(DefaultHdfsDataSourceWriter.class);

    private final HdfsClient hdfsClient;

    @Inject
    public DefaultHdfsDataSourceWriter(HdfsClient hdfsClient)
    {
        this.hdfsClient = hdfsClient;
    }

    @Override
    public void ensureDataOnHdfs(String dataSourcePath, HiveDataSource dataSource)
    {
        hdfsClient.delete(dataSourcePath);
        hdfsClient.createDirectory(dataSourcePath);
        storeTableFiles(dataSourcePath, dataSource);
    }

    private void storeTableFiles(String dataSourcePath, HiveDataSource dataSource)
    {
        int fileIndex = 0;
        for (RepeatableContentProducer fileContent : dataSource.data()) {
            String filePath = dataSourcePath + "/data_" + fileIndex;
            LOGGER.debug("Saving new file {}", filePath);
            hdfsClient.saveFile(filePath, fileContent);
            fileIndex++;
        }
    }
}
