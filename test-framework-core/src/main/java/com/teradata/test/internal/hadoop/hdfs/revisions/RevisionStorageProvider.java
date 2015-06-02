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
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class RevisionStorageProvider
        implements Provider<RevisionStorage>
{
    private static final Logger LOGGER = getLogger(RevisionStorageProvider.class);

    private static final String TEST_X_ATTR_KEY = "user.test-attr-key";
    private static final String TEST_X_ATTR_VALUE = "test-attr-value";

    private final HdfsClient hdfsClient;
    private final String hdfsUser;
    private final String testDataBasePath;

    @Inject
    public RevisionStorageProvider(HdfsClient hdfsClient,
            @Named("hdfs.username") String hdfsUser,
            @Named("tests.hdfs.path") String testDataBasePath)
    {
        this.hdfsClient = hdfsClient;
        this.hdfsUser = hdfsUser;
        this.testDataBasePath = testDataBasePath;
    }

    public RevisionStorage get()
    {
        if (xAttrsSupported()) {
            LOGGER.info("HDFS xAttrs supported. Lets use RevisionMarkerXAttr.");
            return new RevisionStorageXAttr(hdfsClient, hdfsUser);
        }
        else {
            LOGGER.info("HDFS xAttrs are not supported. Lets use RevisionMarkerFile.");
            return new RevisionStorageFile(hdfsClient, hdfsUser, testDataBasePath);
        }
    }

    private boolean xAttrsSupported()
    {
        String tmpFilePath = "/tmp/" + UUID.randomUUID().toString();
        try {
            hdfsClient.saveFile(tmpFilePath, hdfsUser, "RevisionMarkerFactory.xAttrsSupported()");
            hdfsClient.setXAttr(tmpFilePath, hdfsUser, TEST_X_ATTR_KEY, TEST_X_ATTR_VALUE);
            boolean supported = hdfsClient.getXAttr(tmpFilePath, hdfsUser, TEST_X_ATTR_KEY).orElse("").equals(TEST_X_ATTR_VALUE);
            hdfsClient.removeXAttr(tmpFilePath, hdfsUser, TEST_X_ATTR_KEY);
            return supported;
        }
        catch (RuntimeException e) {
            if (isXAttrsWebCallRelated(e)) {
                LOGGER.debug("Could not get xAttr for path: " + tmpFilePath + " in hdfs, user: " + hdfsUser + "; e=" + e.getMessage());
                return false;
            }
            throw e;
        }
        finally {
            hdfsClient.delete(tmpFilePath, hdfsUser);
        }
    }

    private boolean isXAttrsWebCallRelated(Exception e)
    {
        Throwable cause = e;
        do {
            if (cause.getMessage().contains("XATTR") && cause.getMessage().contains("Operation")) {
                return true;
            }
        }
        while ((cause = e.getCause()) != null);

        return false;
    }
}
