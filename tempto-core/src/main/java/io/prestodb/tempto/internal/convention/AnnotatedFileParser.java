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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.newHashMap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Parses files where first line can be single line header.
 * The line must start with -- marker, and define semicolon separeated map of params.
 * <p>
 * Example contents:
 * -- database: hive; groups: example_smoketest,blah
 * SOME BODY
 * BODY
 * --- comment
 * BODY
 * <p>
 * Example multi-section contents:
 * -- database: hive; groups: foo
 * -- delimiter: |
 * --- comment
 * --! name: foo
 * select * from foo;
 * -- delimiter: ,
 * --!
 * 42|24|
 */
public class AnnotatedFileParser
{
    private static final String PROPERTIES_PREFIX = "--";
    private static final String COMMENT_PREFIX = "---";
    private static final String SECTION_PREFIX = "--!";
    private static final String SECTION_NAME_KEY = "name";
    private static final String NEW_LINE_LITERAL = "\\n";

    private static final Splitter.MapSplitter COMMENT_PROPERTIES_SPLITTER = Splitter.on(';')
            .omitEmptyStrings()
            .trimResults()
            .withKeyValueSeparator(Splitter.on(":").trimResults());
    public static final String LINE_ESCAPE = "\\";

    public List<SectionParsingResult> parseFile(Path path)
    {
        try (
                InputStream inputStream = new BufferedInputStream(newInputStream(path))
        ) {
            return parseFile(inputStream);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not load file " + path, e);
        }
    }

    public List<SectionParsingResult> parseFile(InputStream fileInput)
            throws IOException
    {
        byte[] fileBytes = ByteStreams.toByteArray(fileInput);
        List<String> lines = readLines(new ByteArrayInputStream(fileBytes), UTF_8);
        List<List<String>> sections = splitSections(lines);
        return sections.stream().map(this::parseSection).collect(toList());
    }

    private SectionParsingResult parseSection(List<String> lines)
    {
        Map<String, String> properties = newHashMap();
        lines.stream().forEach((String line) -> {
            if (lineHasProperties(line)) {
                Map<String, String> lineProperties = parseLineProperties(line);
                Map<String, ValueDifference<String>> difference = difference(properties, lineProperties).entriesDiffering();
                checkState(difference.isEmpty(), "Different properties: ", difference);
                properties.putAll(lineProperties);
            }
        });

        List<String> contentFiltered = filterContent(lines);
        Optional<String> sectionName = Optional.ofNullable(properties.get(SECTION_NAME_KEY));
        return new SectionParsingResult(sectionName, lines, properties, contentFiltered);
    }

    private List<List<String>> splitSections(List<String> lines)
    {
        List<List<String>> sections = newArrayList();
        int nextSectionIndex;
        while ((nextSectionIndex = findNextSectionIndex(lines)) != -1) {
            sections.add(lines.subList(0, nextSectionIndex));
            lines = lines.subList(nextSectionIndex, lines.size());
        }

        if (!lines.isEmpty() || sections.isEmpty()) {
            sections.add(lines);
        }

        return sections;
    }

    private int findNextSectionIndex(List<String> lines)
    {
        for (int i = 1; i < lines.size(); ++i) {
            if (isSectionLine(lines.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Remove all comments, properties, section and empty lines from content body
     */
    private List<String> filterContent(List<String> contentLines)
    {
        return contentLines.stream()
                .filter(s -> !(isSpecialLine(s) || isBlank(s)))
                .map(AnnotatedFileParser::unescapeLine)
                .map(line -> line.replace(NEW_LINE_LITERAL, "\n"))
                .collect(toList());
    }

    private static String unescapeLine(String s)
    {
        return s.startsWith(LINE_ESCAPE) ? s.substring(1) : s;
    }

    private Map<String, String> parseLineProperties(String line)
    {
        checkArgument(lineHasProperties(line));

        String prefix;
        if (isPropertiesLine(line)) {
            prefix = PROPERTIES_PREFIX;
        }
        else {
            checkState(isSectionLine(line));
            prefix = SECTION_PREFIX;
        }

        return COMMENT_PROPERTIES_SPLITTER.split(line.substring(prefix.length()));
    }

    private boolean isSpecialLine(String line)
    {
        return isPropertiesLine(line) || isCommentLine(line) || isSectionLine(line);
    }

    private boolean lineHasProperties(String line)
    {
        return isPropertiesLine(line) || isSectionLine(line);
    }

    private boolean isPropertiesLine(String line)
    {
        return line.startsWith(PROPERTIES_PREFIX) && !isCommentLine(line) && !isSectionLine(line);
    }

    private boolean isCommentLine(String line)
    {
        return line.startsWith(COMMENT_PREFIX);
    }

    private boolean isSectionLine(String line)
    {
        return line.startsWith(SECTION_PREFIX);
    }

    private <T> void addIfNotEmpty(List<T> list, Collection<List<T>> listCollection)
    {
        if (!list.isEmpty()) {
            listCollection.add(list);
        }
    }

    public static class SectionParsingResult
    {
        private final Optional<String> sectionName;
        private final List<String> sectionLines;
        private final Map<String, String> properties;
        private final List<String> contentLines;

        public SectionParsingResult(Optional<String> sectionName, List<String> sectionLines, Map<String, String> properties, List<String> contentLines)
        {
            this.sectionName = sectionName;
            this.sectionLines = sectionLines;
            this.properties = properties;
            this.contentLines = contentLines;
        }

        public Optional<String> getSectionName()
        {
            return sectionName;
        }

        public Optional<String> getProperty(String key)
        {
            return Optional.ofNullable(properties.get(key));
        }

        public Map<String, String> getProperties()
        {
            return properties;
        }

        public String getOriginalContent()
        {
            return Joiner.on('\n').join(sectionLines);
        }

        public String getContent()
        {
            return Joiner.on('\n').join(contentLines);
        }

        public List<String> getContentLines()
        {
            return contentLines;
        }

        /**
         * @return returns lines joined by ' ' character
         */
        public String getContentAsSingleLine()
        {
            return Joiner.on(' ').join(contentLines);
        }
    }
}
