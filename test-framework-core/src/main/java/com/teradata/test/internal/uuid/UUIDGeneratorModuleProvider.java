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

package com.teradata.test.internal.uuid;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;

@AutoModuleProvider
public class UUIDGeneratorModuleProvider implements SuiteModuleProvider
{
    @Override
    public Module getModule(Configuration configuration)
    {
        return new AbstractModule() {
            @Override
            protected void configure()
            {
                bind(UUIDGenerator.class).to(DefaultUUIDGenerator.class).in(Singleton.class);
            }
        };
    }
}
