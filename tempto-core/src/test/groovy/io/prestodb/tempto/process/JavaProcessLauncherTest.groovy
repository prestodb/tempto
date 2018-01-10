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
package io.prestodb.tempto.process

import spock.lang.Specification

import static io.prestodb.tempto.process.JavaProcessLauncher.defaultJavaProcessLauncher
import static io.prestodb.tempto.process.TestClassWithMain.EXPECTED_ARGUMENT
import static io.prestodb.tempto.process.TestClassWithMain.EXPECTED_LINE
import static io.prestodb.tempto.process.TestClassWithMain.PRODUCED_LINE

class JavaProcessLauncherTest
        extends Specification
{
    def 'test execute CLI Java process'()
            throws IOException, InterruptedException
    {
        setup:
        LocalCliProcess child = new LocalCliProcess(defaultJavaProcessLauncher().launch(TestClassWithMain.class, [EXPECTED_ARGUMENT]))
        child.getProcessInput().println(EXPECTED_LINE)

        expect:
        child.readRemainingOutputLines() == [PRODUCED_LINE]
        child.waitForWithTimeoutAndKill()
    }
}
