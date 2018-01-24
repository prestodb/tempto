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

package io.prestodb.tempto.internal.configuration;

import io.prestodb.tempto.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;

public class YamlConfiguration
        extends DelegateConfiguration
{
    private final MapConfiguration mapConfiguration;

    public YamlConfiguration(String yamlString)
    {
        this(new ByteArrayInputStream(yamlString.getBytes(UTF_8)));
    }

    public YamlConfiguration(InputStream yamlInputStream)
    {
        mapConfiguration = loadConfiguration(inputStreamToStringSafe(yamlInputStream));
    }

    private MapConfiguration loadConfiguration(String yamlString)
    {
        Yaml yaml = new Yaml();
        Object loadedYaml = yaml.load(yamlString);
        checkArgument(loadedYaml instanceof Map, "yaml does not evaluate to map object; got %s", loadedYaml.getClass().getName());
        Map<String, Object> loadedYamlMap = (Map<String, Object>) loadedYaml;
        return new MapConfiguration(loadedYamlMap);
    }

    @Override
    protected Configuration getDelegate()
    {
        return mapConfiguration;
    }

    private String inputStreamToStringSafe(InputStream yamlInputStream)
    {
        try {
            return IOUtils.toString(yamlInputStream, UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
