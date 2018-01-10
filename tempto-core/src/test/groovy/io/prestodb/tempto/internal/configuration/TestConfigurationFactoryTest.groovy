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

package io.prestodb.tempto.internal.configuration

import io.prestodb.tempto.configuration.Configuration
import spock.lang.Specification

import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.TEST_CONFIGURATION_URIS_KEY

class TestConfigurationFactoryTest
        extends Specification
{
    def 'read two test configurations'()
    {
        setup:
        System.setProperty(TEST_CONFIGURATION_URIS_KEY, "/configuration/global-configuration-tempto.yaml,/configuration/local-configuration-tempto.yaml");

        when:
        Configuration configuration = TestConfigurationFactory.createTestConfiguration()

        then:
        configuration.getStringMandatory('value.local') == 'local'
        configuration.getStringMandatory('value.both') == 'local'
        configuration.getStringMandatory('value.global') == 'global'
        configuration.getStringMandatory('value.default') == 'default_value'

        configuration.getStringMandatory('resolve.local') == 'local'
        configuration.getStringMandatory('resolve.both') == 'local'
        configuration.getStringMandatory('resolve.global') == 'global'
    }
}
