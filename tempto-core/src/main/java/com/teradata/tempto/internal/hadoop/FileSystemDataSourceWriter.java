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

package com.teradata.tempto.internal.hadoop;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.teradata.tempto.fulfillment.table.hive.HiveDataSource;
import com.teradata.tempto.hadoop.FileSystemClient;
import com.teradata.tempto.hadoop.FileSystemClient.RepeatableContentProducer;
import com.teradata.tempto.internal.hadoop.revisions.RevisionStorage;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class FileSystemDataSourceWriter
{
    private static final Logger LOGGER = getLogger(FileSystemDataSourceWriter.class);

    private final FileSystemClient fsClient;
    private final RevisionStorage revisionStorage;

    @Inject
    public FileSystemDataSourceWriter(FileSystemClient fsClient,
                                      RevisionStorage revisionStorage)
    {
        this.fsClient = fsClient;
        this.revisionStorage = revisionStorage;
    }

    public void ensureDataOnFileSystem(String dataSourcePath, String fsPrefix, HiveDataSource dataSource)
    {
        String path = dataSourcePath.replace(fsPrefix, "");

        if (isDataUpToDate(path, dataSource)) {
            return;
        }

        revisionStorage.remove(path);
        fsClient.deleteDirectory(path);
        fsClient.createDirectory(path);
        storeTableFiles(path, dataSource);
        revisionStorage.put(path, dataSource.revisionMarker());
    }

    private boolean isDataUpToDate(String dataSourcePath, HiveDataSource dataSource)
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<String> storedRevisionMarker = revisionStorage.get(dataSourcePath);
        LOGGER.debug("revisionMarker.get(\"{}\") took {}ms", dataSourcePath, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        if (storedRevisionMarker.isPresent()) {
            if (storedRevisionMarker.get().equals(dataSource.revisionMarker())) {
                LOGGER.debug("Directory {} ({}) already exists, skipping generation of data", dataSourcePath, storedRevisionMarker.get());
                return true;
            }
            else {
                LOGGER.info("Directory {} ({}) already exists, but has different revision marker than expected: {}, so data will be regenerated",
                        dataSourcePath, storedRevisionMarker.get(), dataSource.revisionMarker());
            }
        }
        return false;
    }

    private void storeTableFiles(String dataSourcePath, HiveDataSource dataSource)
    {
        int fileIndex = 0;
        for (RepeatableContentProducer fileContent : dataSource.data()) {
            String filePath = dataSourcePath + "/data_" + fileIndex;
            LOGGER.debug("Saving new file {} ({})", filePath, dataSource.revisionMarker());
            fsClient.saveFile(filePath, fileContent);
            fileIndex++;
        }
    }
}
