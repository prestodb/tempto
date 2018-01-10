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

import com.google.common.base.Splitter;
import io.prestodb.tempto.internal.convention.AnnotatedFileParser.SectionParsingResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Base class for {@link SqlQueryDescriptor}
 * and {@link SqlResultDescriptor}.
 */
public class SqlDescriptor
{
    private static final Splitter HEADER_PROPERTY_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    protected final SectionParsingResult sqlSectionParsingResult;
    private final Map<String, String> baseProperties;

    protected SqlDescriptor(SectionParsingResult sqlSectionParsingResult)
    {
        this(sqlSectionParsingResult, Collections.emptyMap());
    }

    protected SqlDescriptor(SectionParsingResult sqlSectionParsingResult, Map<String, String> baseProperties)
    {
        this.sqlSectionParsingResult = sqlSectionParsingResult;
        this.baseProperties = baseProperties;
    }

    public Optional<String> getName()
    {
        return sqlSectionParsingResult.getSectionName();
    }

    public String getContent()
    {
        return sqlSectionParsingResult.getContent();
    }

    public String getOriginalContent()
    {
        return sqlSectionParsingResult.getOriginalContent();
    }

    protected List<String> getPropertyValues(String property)
    {
        List<String> propertyValues = newArrayList();

        if (sqlSectionParsingResult.getProperty(property).isPresent()) {
            propertyValues.addAll(newArrayList(HEADER_PROPERTY_SPLITTER.split(sqlSectionParsingResult.getProperty(property).get())));
        }

        if (baseProperties.containsKey(property)) {
            propertyValues.addAll(newArrayList(HEADER_PROPERTY_SPLITTER.split(baseProperties.get(property))));
        }

        return propertyValues;
    }

    protected Set<String> getPropertyValuesSet(String property)
    {
        return newHashSet(getPropertyValues(property));
    }

    protected Optional<String> getPropertyValue(String property)
    {
        if (sqlSectionParsingResult.getProperty(property).isPresent()) {
            return sqlSectionParsingResult.getProperty(property);
        }
        else {
            return Optional.ofNullable(baseProperties.get(property));
        }
    }
}
