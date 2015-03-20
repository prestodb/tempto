/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.tpch;

import io.airlift.tpch.TpchEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Output stream which prints out '|' delimited TPCH data for given entity, ex:
 * <pre>
 *     0|ALGERIA|0| haggle. carefully final deposits detect slyly agai|
 *     1|ARGENTINA|1|al foxes promise slyly according to the regular accounts. bold requests alon|
 * </pre>
 */
public class IterableTpchEntityInputStream<T extends TpchEntity>
        extends InputStream
{

    private final Iterator<T> rowIterator;
    private CharSequence currentLine;
    private int currentReadLineIndex;
    private boolean newLinePrinted = true;

    public IterableTpchEntityInputStream(Iterable<T> iterable)
    {
        this.rowIterator = iterable.iterator();
    }

    // TODO: implement read(byte[]) if this is slow
    @Override
    public int read()
            throws IOException
    {
        if (currentLine == null || currentReadLineIndex >= currentLine.length()) {
            if (rowIterator.hasNext()) {
                newLinePrinted = currentLine == null;
                currentReadLineIndex = 0;
                currentLine = rowIterator.next().toLine();
            }
            else {
                return -1;
            }
        }
        if (!newLinePrinted) {
            newLinePrinted = true;
            return '\n';
        }
        return currentLine.charAt(currentReadLineIndex++);
    }
}
