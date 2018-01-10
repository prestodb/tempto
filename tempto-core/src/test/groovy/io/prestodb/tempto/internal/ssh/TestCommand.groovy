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
package io.prestodb.tempto.internal.ssh

import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

class TestCommand
        implements Command
{
    private final String response;
    private final int errorCode;

    private InputStream input;
    private OutputStream output;
    private OutputStream error;
    private ExitCallback callback;

    TestCommand(String response, int errorCode)
    {
        this.response = response;
        this.errorCode = errorCode;
    }

    void setInputStream(InputStream input)
    {
        this.input = input;
    }

    void setOutputStream(OutputStream out)
    {
        this.output = out;
    }

    void setErrorStream(OutputStream err)
    {
        this.error = err;
    }

    void setExitCallback(ExitCallback callback)
    {
        this.callback = callback;
    }

    void start(Environment env)
            throws IOException
    {
        output.write(response.getBytes());
        output.flush();
        callback.onExit(errorCode, response);
    }

    void destroy()
    {
    }
}
