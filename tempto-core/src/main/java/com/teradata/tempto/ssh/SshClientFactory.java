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

package com.teradata.tempto.ssh;

import java.util.Optional;

public interface SshClientFactory
{

    String DEFAULT_USER = "root";

    default SshClient create(String host)
    {
        return create(host, 22, DEFAULT_USER, Optional.empty());
    }

    default SshClient create(String host, int port)
    {
        return create(host, port, DEFAULT_USER, Optional.empty());
    }

    default SshClient create(String host, int port, String user)
    {
        return create(host, port, user, Optional.empty());
    }

    SshClient create(String host, int port, String user, Optional<String> password);

    void addIdentity(String pathToPem);
}
