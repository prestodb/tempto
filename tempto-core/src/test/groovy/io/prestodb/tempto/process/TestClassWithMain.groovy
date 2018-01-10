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

import static org.assertj.core.api.Assertions.assertThat

class TestClassWithMain
{
    public static final String EXPECTED_ARGUMENT = "foo";
    public static final String EXPECTED_LINE = "hello";
    public static final String PRODUCED_LINE = "world";

    static void main(String[] args)
    {
        assertThat(args.length).isEqualTo(1)
        assertThat(args[0]).isEqualTo(EXPECTED_ARGUMENT)

        Scanner scanner = new Scanner(System.in)
        assertThat(scanner.nextLine()).isEqualTo(EXPECTED_LINE)
        System.out.println(PRODUCED_LINE)
    }
}
