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

package io.prestodb.tempto.internal.fulfillment.table.hive.tpch;

import com.google.common.io.ByteSource;
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
public class TpchEntityByteSource<T extends TpchEntity>
        extends ByteSource
{
    private final IterableTpchEntityInputStream<T> inputStream;

    public TpchEntityByteSource(Iterable<T> iterable)
    {
        this.inputStream = new IterableTpchEntityInputStream<>(iterable);
    }

    @Override
    public InputStream openStream()
            throws IOException
    {
        return this.inputStream;
    }

    private static class IterableTpchEntityInputStream<T extends TpchEntity>
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
}
