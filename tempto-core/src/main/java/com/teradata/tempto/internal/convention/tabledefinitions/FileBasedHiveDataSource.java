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

import com.google.common.collect.ImmutableSet;
import com.teradata.tempto.fulfillment.table.hive.HiveDataSource;
import com.teradata.tempto.hadoop.hdfs.HdfsClient.RepeatableContentProducer;
import com.teradata.tempto.spi.convention.tabledefinitions.ConventionTableDefinitionDescriptor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readAllBytes;

public class FileBasedHiveDataSource
        implements HiveDataSource
{

    private final ConventionTableDefinitionDescriptor tableDefinitionDescriptor;
    private String revisionMarker;

    public FileBasedHiveDataSource(ConventionTableDefinitionDescriptor tableDefinitionDescriptor)
    {
        this.tableDefinitionDescriptor = tableDefinitionDescriptor;
    }

    @Override
    public String getPathSuffix()
    {
        // {TESTS_PATH}/datasets/{dataSetName}
        return format("datasets/%s", tableDefinitionDescriptor.getName());
    }

    @Override
    public Collection<RepeatableContentProducer> data()
    {
        return tableDefinitionDescriptor.getDataFile()
                .map(this::asRepeatableContentProducer)
                .map(ImmutableSet::of)
                .orElse(ImmutableSet.of());
    }

    private RepeatableContentProducer asRepeatableContentProducer(Path dataFile)
    {
        return () -> newInputStream(dataFile);
    }

    @Override
    public String revisionMarker()
    {
        return tableDefinitionDescriptor.getRevisionFile()
                .map(revisionFile -> {
                    try {
                        if (revisionMarker == null) {
                            revisionMarker = new String(readAllBytes(revisionFile));
                        }
                        return revisionMarker;
                    }
                    catch (IOException e) {
                        throw new IllegalStateException("Could not read revision file: " + tableDefinitionDescriptor.getRevisionFile());
                    }
                })
                .orElse("");
    }
}
