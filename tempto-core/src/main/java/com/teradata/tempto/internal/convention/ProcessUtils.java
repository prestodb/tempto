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
package com.teradata.tempto.internal.convention;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

public final class ProcessUtils
{
    private static final int SUCCESS_EXIT_CODE = 0;

    private ProcessUtils()
    {
    }

    public static void execute(String... cmdarray)
    {
        checkState(cmdarray.length > 0);

        try {
            Process process = Runtime.getRuntime().exec(cmdarray);
            process.waitFor();
            checkState(process.exitValue() == SUCCESS_EXIT_CODE, "%s exited with status code: %s", cmdarray[0], process.exitValue());
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
