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

package io.prestodb.tempto.fulfillment.table.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkArgument;

public class KafkaMessageContentsBuilder
{
    private final ByteArrayOutputStream contents = new ByteArrayOutputStream();

    public static KafkaMessageContentsBuilder contentsBuilder()
    {
        return new KafkaMessageContentsBuilder();
    }

    public KafkaMessageContentsBuilder appendBytes(byte[] bytes)
    {
        try {
            contents.write(bytes);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public KafkaMessageContentsBuilder appendBytes(int... bytes)
    {
        for (int b : bytes) {
            checkArgument(b >= 0 && b <= 255, "Byte must be in 0-255 range");
            contents.write(b);
        }
        return this;
    }

    public KafkaMessageContentsBuilder appendIntBigEndian(int v)
    {
        for (int i = 3; i >= 0; --i) {
            contents.write((v >> (i * 8)) & 0xff);
        }
        return this;
    }

    public KafkaMessageContentsBuilder appendLongBigEndian(long v)
    {
        for (int i = 7; i >= 0; --i) {
            contents.write((int) ((v >> (i * 8)) & 0xffL));
        }
        return this;
    }

    public KafkaMessageContentsBuilder appendUTF8(String s)
    {
        return appendBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] build()
    {
        return contents.toByteArray();
    }
}
