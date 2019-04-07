// Copyright (c) 2013-2014 Sandstorm Development Group, Inc. and contributors
// Licensed under the MIT License:
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * This class is mainly kept for explaining how it was replaced. And for the
 * time being run the scala unit tests.
 *
 * {@link MessageReader} and {@link MessageBuilder} have getSerializedSize() to
 * find the binary size.
 *
 * To read a Message from a Channel or ByteBuffer use
 * {@link AllocatedArenaBuilder} to create an Arena and create a
 * {@link MessageReader} with the Arena.
 *
 * To write a Message use {@link MessageBuilder#write}
 *
 */
@Deprecated
public final class Serialize {

    @Deprecated
    static ByteBuffer makeByteBuffer(int bytes) {
        ByteBuffer result = ByteBuffer.allocate(bytes);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.mark();
        return result;
    }

    @Deprecated
    public static void fillBuffer(ByteBuffer buffer, ReadableByteChannel bc) throws IOException {
        while (buffer.hasRemaining()) {
            int r = bc.read(buffer);
            if (r < 0) {
                throw new IOException("premature EOF");
            }
            // TODO check for r == 0 ?.
        }
    }

    @Deprecated
    public static MessageReader read(ReadableByteChannel bc) throws IOException {
        return read(bc, ReaderOptions.DEFAULT_READER_OPTIONS);
    }

    /**
     * Reads a capnp message into a MessageReader object.
     *
     * @param bc the input data.
     * @param options the options.
     * @return a messageReader or null if EOF.
     * @throws java.io.IOException if thrown by the input data or the data is
     * incomplete.
     * @deprecated Use {@link AllocatedArenaBuilder#build(java.nio.channels.ReadableByteChannel) instead.
     */
    @Deprecated
    public static MessageReader read(ReadableByteChannel bc, ReaderOptions options) throws IOException {
        final AllocatedArena arena = new AllocatedArenaBuilder().build(bc);
        if (arena == null) {
            return null;
        }
        return new MessageReader(arena);
    }

    /**
     *
     * @param bb
     * @return
     * @throws IOException
     * @deprecated Use {@link AllocatedArenaBuilder#build(bb) instead.
     */
    @Deprecated
    public static MessageReader read(ByteBuffer bb) throws IOException {
        return read(bb, ReaderOptions.DEFAULT_READER_OPTIONS);
    }

    /**
     * Upon return, `bb.position()` will be at the end of the message.
     *
     * @param bb the input data.
     * @param options the options are ignored.
     * @return a messageReader or null if bb is empty.
     * @throws java.io.IOException if thrown by the input data or the data is
     * incomplete.
     *
     * @deprecated Use {@link AllocatedArenaBuilder#build(java.nio.ByteBuffer)  instead.
     */
    @Deprecated
    public static MessageReader read(ByteBuffer bb, ReaderOptions options) throws IOException {
        final AllocatedArena arena = new AllocatedArenaBuilder().build(bb);
        if (arena == null) {
            return null;
        }
        return new MessageReader(arena);
    }

    /**
     * Retrieve the size of the message in words.
     *
     * @deprecated Use message.getSerializedSize() / Constants.BYTES_PER_WORD
     * instead.
     */
    @Deprecated
    public static long computeSerializedSizeInWords(MessageBuilder message) {
        return message.getSerializedSize() / Constants.BYTES_PER_WORD;
    }

    /**
     * @deprecated use message.write(outputChannel); instead.
     */
    @Deprecated
    public static void write(WritableByteChannel outputChannel, MessageBuilder message) throws IOException {
        message.write(outputChannel);
    }
}
