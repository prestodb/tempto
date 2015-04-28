/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getFirst;
import static java.nio.file.Files.newInputStream;
import static java.util.Collections.emptyMap;
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
 * BODY
 */
public class HeaderFileParser
{
    private static final String COMMENT_PREFIX = "--";
    private static final Splitter.MapSplitter COMMENT_PROPERTIES_SPLITTER = Splitter.on(';')
            .omitEmptyStrings()
            .trimResults()
            .withKeyValueSeparator(Splitter.on(":").trimResults());

    public ParsingResult parseFile(Path path)
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

    public ParsingResult parseFile(InputStream fileInput)
            throws IOException
    {
        List<String> lines = readLines(fileInput);
        String firstLine = getFirst(lines, "");

        Map<String, String> commentProperties;
        List<String> contentLines;
        if (isCommentLine(firstLine)) {
            commentProperties = parseCommentLine(firstLine);
            contentLines = lines.subList(1, lines.size());
        }
        else {
            commentProperties = emptyMap();
            contentLines = lines;
        }

        List<String> contentFiltered = filterContent(contentLines);

        return new ParsingResult(commentProperties, contentFiltered);
    }

    /**
     * Remove all comments and empty lines from content body
     */
    private List<String> filterContent(List<String> contentLines)
    {
        contentLines = contentLines.stream()
                .filter(s -> !(isCommentLine(s) || isBlank(s)))
                .collect(toList());
        return contentLines;
    }

    private Map<String, String> parseCommentLine(String line)
    {
        checkArgument(isCommentLine(line));
        return COMMENT_PROPERTIES_SPLITTER.split(line.substring(COMMENT_PREFIX.length()));
    }

    private boolean isCommentLine(String line)
    {
        return line.startsWith(COMMENT_PREFIX);
    }

    public static class ParsingResult
    {
        private final Map<String, String> commentProperties;
        private final List<String> contentLines;

        private ParsingResult(Map<String, String> commentProperties, List<String> contentLines)
        {
            this.commentProperties = commentProperties;
            this.contentLines = contentLines;
        }

        public Optional<String> getProperty(String key)
        {
            return Optional.ofNullable(commentProperties.get(key));
        }

        public List<String> getContentLines()
        {
            return contentLines;
        }

        /**
         * @return returns lines joined by ' ' character
         */
        public String getContent()
        {
            return Joiner.on(' ').join(contentLines);
        }
    }
}