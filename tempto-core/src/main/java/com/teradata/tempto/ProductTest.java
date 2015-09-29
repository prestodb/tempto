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

package com.teradata.tempto;

import com.teradata.tempto.internal.initialization.RequirementsExpanderInterceptor;
import com.teradata.tempto.internal.initialization.TestInitializationListener;
import com.teradata.tempto.internal.listeners.ProgressLoggingListener;
import com.teradata.tempto.internal.listeners.TestNameGroupNameMethodSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners({RequirementsExpanderInterceptor.class, TestInitializationListener.class, ProgressLoggingListener.class})
public class ProductTest
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProductTest.class);
}
