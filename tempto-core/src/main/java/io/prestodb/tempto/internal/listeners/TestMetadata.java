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

package io.prestodb.tempto.internal.listeners;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.copyOf;

public class TestMetadata
{
    public final Set<String> testGroups;
    public final String testName;

    public TestMetadata(Set<String> testGroups, String testName)
    {
        this.testGroups = copyOf(checkNotNull(testGroups, "testGroups can not be null"));
        this.testName = checkNotNull(testName, "testName can not be null");
    }
}
