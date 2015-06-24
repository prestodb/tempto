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

import com.teradata.tempto.fulfillment.table.hive.HiveDataSource;
import com.teradata.tempto.hadoop.hdfs.HdfsClient.RepeatableContentProducer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.singleton;

public class FileBasedDataSource
        implements HiveDataSource
{

    private final ConventionTableDefinitionDescriptor conventionTableDefinitionDescriptor;
    private String revisionMarker;

    public FileBasedDataSource(ConventionTableDefinitionDescriptor conventionTableDefinitionDescriptor)
    {
        this.conventionTableDefinitionDescriptor = conventionTableDefinitionDescriptor;
    }

    @Override
    public String getPathSuffix()
    {
        // {TESTS_PATH}/datasets/{dataSetName}
        return format("datasets/%s", conventionTableDefinitionDescriptor.getName());
    }

    @Override
    public Collection<RepeatableContentProducer> data()
    {
        Path dataFile = conventionTableDefinitionDescriptor.getDataFile();

        if (!exists(dataFile)) {
            throw new IllegalStateException("Data file " + conventionTableDefinitionDescriptor.getDataFile() + " should exist");
        }

        return singleton(() -> newInputStream(dataFile));
    }

    @Override
    public String revisionMarker()
    {
        try {
            if (revisionMarker == null) {
                revisionMarker = new String(readAllBytes(conventionTableDefinitionDescriptor.getRevisionFile()));
            }
            return revisionMarker;
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read revision file: " + conventionTableDefinitionDescriptor.getRevisionFile());
        }
    }
}
