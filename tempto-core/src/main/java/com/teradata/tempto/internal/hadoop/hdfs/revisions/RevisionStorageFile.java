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

package com.teradata.tempto.internal.hadoop.hdfs.revisions;

import com.teradata.tempto.hadoop.FileSystemClient;

import java.util.Optional;

/**
 * Store table data revision as file in /tmp path with revision id as content
 */
public class RevisionStorageFile
        implements RevisionStorage
{

    private final FileSystemClient fsClient;
    private final String testDataBasePath;

    RevisionStorageFile(FileSystemClient fsClient, String testDataBasePath)
    {
        this.fsClient = fsClient;
        this.testDataBasePath = testDataBasePath;
    }

    @Override
    public Optional<String> get(String hdfsPath)
    {
        String markerFilePath = markerFilePath(hdfsPath);

        if (!fsClient.exist(markerFilePath)) {
            return Optional.empty();
        }

        return Optional.of(fsClient.loadFile(markerFilePath));
    }

    @Override
    public void put(String hdfsPath, String revision)
    {
        String markerFilePath = markerFilePath(hdfsPath);

        fsClient.delete(markerFilePath);
        fsClient.saveFile(markerFilePath, revision);
    }

    @Override
    public void remove(String hdfsPath)
    {
        fsClient.delete(markerFilePath(hdfsPath));
    }

    private String markerFilePath(String path)
    {
        return getMarkerFilesDirectory() + path;
    }

    private String getMarkerFilesDirectory()
    {
        return testDataBasePath + "/data_revision_markers";
    }
}
