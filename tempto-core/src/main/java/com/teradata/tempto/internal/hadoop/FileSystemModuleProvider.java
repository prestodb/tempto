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

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.hadoop.FileSystemClient;
import com.teradata.tempto.initialization.AutoModuleProvider;
import com.teradata.tempto.initialization.SuiteModuleProvider;
import com.teradata.tempto.internal.hadoop.azure.WasbClient;
import com.teradata.tempto.internal.hadoop.hdfs.SimpleHttpRequestsExecutor;
import com.teradata.tempto.internal.hadoop.hdfs.SpnegoHttpRequestsExecutor;
import com.teradata.tempto.internal.hadoop.hdfs.WebHdfsClient;
import com.teradata.tempto.internal.hadoop.revisions.RevisionStorage;
import com.teradata.tempto.internal.hadoop.revisions.DispatchingRevisionStorage;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import static com.teradata.tempto.internal.hadoop.hdfs.WebHdfsClient.CONF_HDFS_WEBHDFS_HOST_KEY;
import static com.teradata.tempto.internal.hadoop.revisions.DispatchingRevisionStorage.CONF_TESTS_HDFS_PATH_KEY;

@AutoModuleProvider
public class FileSystemModuleProvider
        implements SuiteModuleProvider
{
    private static final Logger logger = LoggerFactory.getLogger(FileSystemModuleProvider.class);

    private static final String AUTHENTICATION_SPNEGO = "SPNEGO";
    private static final String HDFS_FILE_SYSTEM = "hdfs";
    private static final String WASB_FILE_SYSTEM = "wasb";
    private static final String DEFAULT_FILE_SYSTEM = HDFS_FILE_SYSTEM;
    private static final int NUMBER_OF_HTTP_RETRIES = 3;

    @Override
    public Module getModule(Configuration configuration)
    {
        return new PrivateModule()
        {
            @Override
            protected void configure()
            {
                Set<String> configurationKeys = configuration.listKeys();
                if (!configurationKeys.contains(CONF_HDFS_WEBHDFS_HOST_KEY)
                        || !configurationKeys.contains(CONF_TESTS_HDFS_PATH_KEY)) {
                    logger.debug("No HDFS support enabled as '{}' or '{}' is configured",
                            CONF_HDFS_WEBHDFS_HOST_KEY,
                            CONF_TESTS_HDFS_PATH_KEY);
                    return;
                }

                Configuration hdfsSectionConfiguration = configuration
                    .getSubconfiguration("tests")
                    .getSubconfiguration("fs");
                String testFileSystem = hdfsSectionConfiguration
                    .getString("type")
                    .orElse(DEFAULT_FILE_SYSTEM);

                install(httpRequestsExecutorModule());

                if (testFileSystem.toLowerCase().equals(HDFS_FILE_SYSTEM)) {
                    logger.debug("Using HDFS file system");
                    bind(FileSystemClient.class).to(WebHdfsClient.class).in(Scopes.SINGLETON);
                } else if (testFileSystem.toLowerCase().equals(WASB_FILE_SYSTEM)) {
                    logger.debug("Using WASB file system");
                    bind(FileSystemClient.class).to(WasbClient.class).in(Scopes.SINGLETON);
                }


                bind(RevisionStorage.class).to(DispatchingRevisionStorage.class).in(Scopes.SINGLETON);
                bind(FileSystemDataSourceWriter.class).in(Scopes.SINGLETON);

                expose(FileSystemClient.class);
                expose(RevisionStorage.class);
                expose(FileSystemDataSourceWriter.class);
            }

            private Module httpRequestsExecutorModule()
            {
                if (spnegoAuthenticationRequired()) {
                    return new SpnegoHttpRequestsExecutor.Module();
                }
                else {
                    return new SimpleHttpRequestsExecutor.Module();
                }
            }

            private boolean spnegoAuthenticationRequired()
            {
                Optional<String> authentication = configuration.getString("hdfs.webhdfs.authentication");
                return authentication.isPresent() && authentication.get().equalsIgnoreCase(AUTHENTICATION_SPNEGO);
            }

            @Inject
            @Provides
            @Singleton
            CloseableHttpClient createHttpClient()
            {
                HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
                httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(NUMBER_OF_HTTP_RETRIES, true));
                return httpClientBuilder.build();
            }
        };
    }
}
