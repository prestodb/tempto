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

package com.teradata.test.internal.hadoop.hdfs.revisions;

import com.teradata.test.hadoop.hdfs.HdfsClient;

import java.util.Optional;

/**
 * Store table data revision as file in /tmp path with revision id as content
 */
public class RevisionStorageFile
        implements RevisionStorage
{

    private final HdfsClient hdfsClient;
    private final String hdfsUser;
    private final String testDataBasePath;

    RevisionStorageFile(HdfsClient hdfsClient, String hdfsUser, String testDataBasePath)
    {
        this.hdfsClient = hdfsClient;
        this.hdfsUser = hdfsUser;
        this.testDataBasePath = testDataBasePath;
    }

    @Override
    public Optional<String> get(String hdfsPath)
    {
        String markerFilePath = markerFilePath(hdfsPath);

        if (!hdfsClient.exist(markerFilePath, hdfsUser)) {
            return Optional.empty();
        }

        return Optional.of(hdfsClient.loadFile(markerFilePath, hdfsUser));
    }

    @Override
    public void put(String hdfsPath, String revision)
    {
        String markerFilePath = markerFilePath(hdfsPath);

        hdfsClient.delete(markerFilePath, hdfsUser);
        hdfsClient.saveFile(markerFilePath, hdfsUser, revision);
    }

    @Override
    public void remove(String hdfsPath)
    {
        hdfsClient.delete(markerFilePath(hdfsPath), hdfsUser);
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
