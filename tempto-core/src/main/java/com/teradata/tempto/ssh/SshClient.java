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

import com.teradata.tempto.process.CliProcess;

import java.nio.file.Path;
import java.util.List;

/**
 * Simple SSH client.
 */
public interface SshClient
{
    /**
     * Executes command on a remote machine.
     *
     * @param command Command to be executed on remote machine.
     * @return CLIProcess
     */
    CliProcess execute(String command);


    CliProcess execute(List<String> command);

    /**
     * Uploads file to a remote machine. It works like SCP.
     *
     * @param file Local path to file which is to be uploaded
     * @param remotePath Destination path for file on remote machine.
     */
    void upload(Path file, String remotePath);
}
