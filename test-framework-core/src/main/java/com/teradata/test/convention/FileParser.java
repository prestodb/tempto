/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getFirst;
import static java.util.Collections.emptyMap;
import static org.apache.commons.io.IOUtils.readLines;

public class FileParser
{
    private static final String COMMENT_PREFIX = "--";
    private static final Splitter.MapSplitter COMMENT_PROPERTIES_SPLITTER = Splitter.on(',')
            .omitEmptyStrings()
            .trimResults()
            .withKeyValueSeparator(Splitter.on(":").trimResults());

    public ParsingResult parseFile(InputStream queryFileInput)
            throws IOException
    {
        List<String> lines = readLines(queryFileInput);
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

        return new ParsingResult(commentProperties, contentLines);
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