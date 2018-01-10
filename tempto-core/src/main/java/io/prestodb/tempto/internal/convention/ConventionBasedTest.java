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

package io.prestodb.tempto.internal.convention;

import io.prestodb.tempto.ProductTest;
import io.prestodb.tempto.RequirementsProvider;
import io.prestodb.tempto.testmarkers.WithName;
import io.prestodb.tempto.testmarkers.WithTestGroups;

public abstract class ConventionBasedTest
        extends ProductTest
        implements RequirementsProvider, WithName, WithTestGroups
{
    public abstract void test();
}
