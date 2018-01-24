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

import io.prestodb.tempto.configuration.Configuration;
import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

import java.util.Map;

import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.testConfiguration;

public class TemptoNameServiceDescriptor
        implements NameServiceDescriptor
{
    public static final String PROVIDER_NAME = "map_based";
    public static final String TYPE = "dns";

    public static void enableHostMapping()
    {
        System.setProperty("sun.net.spi.nameservice.provider.1", TYPE + "," + PROVIDER_NAME);
        System.setProperty("sun.net.spi.nameservice.provider.2", "default");
    }

    @Override
    public NameService createNameService()
            throws Exception
    {
        Configuration configuration = testConfiguration();
        Map hosts = configuration.getSubconfiguration("hosts").asMap();
        return new MapBasedNameService(hosts);
    }

    @Override
    public String getProviderName()
    {
        return PROVIDER_NAME;
    }

    @Override
    public String getType()
    {
        return TYPE;
    }
}
