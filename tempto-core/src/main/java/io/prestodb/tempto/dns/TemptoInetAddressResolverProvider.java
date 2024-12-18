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
package io.prestodb.tempto.dns;

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;
import java.util.Map;

import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.testConfiguration;

public class TemptoInetAddressResolverProvider
        extends InetAddressResolverProvider
{
    public static final String PROVIDER_NAME = "map_based";
    public static final String TYPE = "dns";

    public static void enableHostMapping()
    {
        System.setProperty("sun.net.spi.nameservice.provider.1", TYPE + "," + PROVIDER_NAME);
        System.setProperty("sun.net.spi.nameservice.provider.2", "default");
    }

    @Override
    public InetAddressResolver get(Configuration configuration)
    {
       io.prestodb.tempto.configuration.Configuration temptoConfiguration = testConfiguration();
        Map hosts = temptoConfiguration.getSubconfiguration("hosts").asMap();
        return new MapBasedAddressResolver(hosts);
    }

    @Override
    public String name()
    {
        return TemptoInetAddressResolverProvider.class.getName();
    }
}
