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
package io.prestodb.tempto.process;

import io.prestodb.tempto.internal.process.CliProcessBase;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Wrapping class for {@link Process} that helps interacting with CLI processes.
 */
public class LocalCliProcess
        extends CliProcessBase
{
    private final Process process;

    public LocalCliProcess(Process process)
    {
        super(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
        this.process = process;
    }

    @Override
    public void waitForWithTimeoutAndKill(Duration timeout)
            throws InterruptedException
    {
        if (!process.waitFor(timeout.toMillis(), MILLISECONDS)) {
            close();
            throw new RuntimeException("Child process didn't finish within given timeout");
        }

        int exitValue = process.exitValue();
        if (exitValue != 0) {
            throw new RuntimeException("Child process exited with non-zero code: " + exitValue);
        }
    }

    /**
     * Terminates process and closes all related streams
     */
    @Override
    public void close()
    {
        // destroy process
        if (process.isAlive()) {
            process.destroy();
        }

        // then close all related streams
        super.close();
    }
}
