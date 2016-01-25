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

import com.teradata.tempto.hadoop.hdfs.HdfsClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.slf4j.LoggerFactory.getLogger;

public class RevisionStorageProvider
        implements Provider<RevisionStorage>
{
    private static final Logger LOGGER = getLogger(RevisionStorageProvider.class);

    private static final String TEST_X_ATTR_KEY = "user.test-attr-key";
    private static final String TEST_X_ATTR_VALUE = "test-attr-value";

    private final HdfsClient hdfsClient;
    private final String testDataBasePath;

    @Inject
    public RevisionStorageProvider(HdfsClient hdfsClient,
            @Named("tests.hdfs.path") String testDataBasePath)
    {
        this.hdfsClient = hdfsClient;
        this.testDataBasePath = testDataBasePath;
    }

    public RevisionStorage get()
    {
        if (xAttrsSupported()) {
            LOGGER.debug("HDFS xAttrs supported. Lets use RevisionMarkerXAttr.");
            return new RevisionStorageXAttr(hdfsClient);
        }
        else {
            LOGGER.debug("HDFS xAttrs are not supported. Lets use RevisionMarkerFile.");
            return new RevisionStorageFile(hdfsClient, testDataBasePath);
        }
    }

    private boolean xAttrsSupported()
    {
        try {
            hdfsClient.createDirectory(testDataBasePath);
            hdfsClient.setXAttr(testDataBasePath, TEST_X_ATTR_KEY, TEST_X_ATTR_VALUE);
            boolean supported = hdfsClient.getXAttr(testDataBasePath, TEST_X_ATTR_KEY).orElse("").equals(TEST_X_ATTR_VALUE);
            hdfsClient.removeXAttr(testDataBasePath, TEST_X_ATTR_KEY);
            return supported;
        }
        catch (RuntimeException e) {
            if (isXAttrsWebCallRelated(e)) {
                LOGGER.debug("Could not get xAttr for path: " + testDataBasePath + " in hdfs; e=" + e.getMessage());
                return false;
            }
            throw e;
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
        while ((cause = cause.getCause()) != null);

        return false;
    }
}
