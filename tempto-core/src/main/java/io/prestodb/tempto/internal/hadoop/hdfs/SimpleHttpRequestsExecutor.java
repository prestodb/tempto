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

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

public class SimpleHttpRequestsExecutor
        implements HttpRequestsExecutor
{
    public static class Module
            extends AbstractModule
    {
        @Override
        public void configure()
        {
            bind(HttpRequestsExecutor.class)
                    .to(SimpleHttpRequestsExecutor.class)
                    .in(Scopes.SINGLETON);
        }
    }

    private final CloseableHttpClient httpClient;
    private final String username;

    @Inject
    public SimpleHttpRequestsExecutor(CloseableHttpClient httpClient,
            @Named("hdfs.username") String username)
    {
        this.httpClient = requireNonNull(httpClient, "httpClient is null");
        this.username = requireNonNull(username, "username is null");
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequest request)
            throws IOException
    {
        HttpUriRequest usernameContainingRequest = appendUsernameToQueryString(request);
        return httpClient.execute(usernameContainingRequest);
    }

    private HttpUriRequest appendUsernameToQueryString(HttpUriRequest request)
    {
        HttpRequestWrapper httpRequestWrapper = HttpRequestWrapper.wrap(request);
        URI originalUri = httpRequestWrapper.getURI();
        URI uriWithUsername = appendUsername(originalUri);
        httpRequestWrapper.setURI(uriWithUsername);
        return httpRequestWrapper;
    }

    private URI appendUsername(URI originalUri)
    {
        URIBuilder uriBuilder = new URIBuilder(originalUri);
        uriBuilder.setParameter("user.name", username);
        try {
            return uriBuilder.build();
        }
        catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}
