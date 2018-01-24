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

import sun.net.spi.nameservice.NameService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MapBasedNameService
        implements NameService
{
    private final Map<String, String> hosts;

    public MapBasedNameService(Map<String, String> hosts)
    {
        this.hosts = requireNonNull(hosts, "hosts is null");
    }

    @Override
    public InetAddress[] lookupAllHostAddr(String host)
            throws UnknownHostException
    {
        if (hosts.containsKey(host)) {
            return InetAddress.getAllByName(hosts.get(host));
        }
        throw new UnknownHostException();
    }

    @Override
    public String getHostByAddr(byte[] bytes)
            throws UnknownHostException
    {
        throw new UnknownHostException();
    }
}
