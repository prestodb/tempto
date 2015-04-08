/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.hive.tpch;

import com.google.common.io.ByteSource;
import io.airlift.tpch.TpchEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Output stream which prints out '|' delimited TPCH data for given entity, ex:
 * <pre>
 *     0|ALGERIA|0| haggle. carefully final deposits detect slyly agai|
 *     1|ARGENTINA|1|al foxes promise slyly according to the regular accounts. bold requests alon|
 * </pre>
 */
public class TpchEntityByteSource<T extends TpchEntity>
        extends ByteSource
{

    private static final String NEW_LINE = "\n";
    private static final int EOF = -1;

    private final Iterable<T> rowsIterable;

    public TpchEntityByteSource(Iterable<T> iterable)
    {
        this.rowsIterable = iterable;
    }

    @Override
    public InputStream openStream()
            throws IOException
    {
        final Iterator<T> rowsIterator = rowsIterable.iterator();
        return new SequenceInputStream(new Enumeration<InputStream>()
        {
            private boolean nextIsNewLine = false;

            @Override
            public boolean hasMoreElements()
            {
                return rowsIterator.hasNext();
            }

            @Override
            public InputStream nextElement()
            {
                if (nextIsNewLine) {
                    nextIsNewLine = false;
                    return toInputStream(NEW_LINE);
                }
                else {
                    nextIsNewLine = true;
                    return toInputStream(rowsIterator.next().toLine());
                }
            }
        });
    }

    private InputStream toInputStream(String string)
    {

        return new InputStream()
        {

            private int position = 0;
            private int stringLength = string.length();

            @Override
            public int read()
                    throws IOException
            {
                if (position < stringLength) {
                    return string.charAt(position++);
                }
                else {
                    return EOF;
                }
            }
        };
    }
}
