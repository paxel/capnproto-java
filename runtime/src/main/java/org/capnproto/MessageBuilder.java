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
import java.nio.channels.WritableByteChannel;

public final class MessageBuilder {

    private final AllocatingArena arena;

    /**
     * Creates a MessageBuilder with an injected custom AllocatingArena.
     *
     * @param arena The custom AllocatingArena.
     */
    public MessageBuilder(AllocatingArena arena) {
        this.arena = arena;
    }

    public MessageBuilder() {
        this.arena = new BuilderArena(BuilderArena.SUGGESTED_FIRST_SEGMENT_WORDS,
                BuilderArena.SUGGESTED_ALLOCATION_STRATEGY);
    }

    public MessageBuilder(int firstSegmentWords) {
        this.arena = new BuilderArena(firstSegmentWords,
                BuilderArena.SUGGESTED_ALLOCATION_STRATEGY);
    }

    public MessageBuilder(int firstSegmentWords, BuilderArena.AllocationStrategy allocationStrategy) {
        this.arena = new BuilderArena(firstSegmentWords,
                allocationStrategy);
    }

    private AnyPointer.Builder getRootInternal() {
        GenericSegmentBuilder rootSegment = this.arena.tryGetSegment(0);
        if (rootSegment.currentSize() == 0) {
            int location = rootSegment.allocate(1);
            if (location == GenericSegmentBuilder.FAILED_ALLOCATION) {
                throw new CapnProtoException("could not allocate root pointer");
            }
            if (location != 0) {
                throw new CapnProtoException("First allocated word of new segment was not at offset 0");
            }
            return new AnyPointer.Builder(rootSegment, location);
        } else {
            return new AnyPointer.Builder(rootSegment, 0);
        }
    }

    public <T> T getRoot(FromPointerBuilder<T> factory) {
        return this.getRootInternal().getAs(factory);
    }

    public <T, U> void setRoot(SetPointerBuilder<T, U> factory, U reader) {
        this.getRootInternal().setAs(factory, reader);
    }

    public <T> T initRoot(FromPointerBuilder<T> factory) {
        return this.getRootInternal().initAs(factory);
    }

    /**
     * provide the {@link AllocatingArena} for output.
     *
     * @return the arena.
     */
    public AllocatingArena getArena() {
        return arena;
    }

    /**
     * Retrieve the size of the message in Bytes when written with {@link MessageBuilder#write(java.nio.channels.WritableByteChannel)
     * }. This size might change if the content of this Builder is updated.
     *
     * @return the size in bytes.
     */
    public long getSerializedSize() {

        // From the capnproto documentation:
        // "When transmitting over a stream, the following should be sent..."
        long bytes = 0;
        // "(4 bytes) The number of segments, minus one..."
        bytes += 4;
        // "(N * 4 bytes) The size of each segment, in words."
        bytes += arena.getSegments().size() * 4;
        // "(0 or 4 bytes) Padding up to the next word boundary."
        if (bytes % Constants.BYTES_PER_WORD != 0) {
            bytes += 4;
        }
        for (GenericSegmentBuilder segment : arena.getSegments()) {
            // The content of each segment, in order.
            bytes += segment.currentSize() * Constants.BYTES_PER_WORD;
        }

        return bytes;

    }

    public void write(WritableByteChannel outputChannel) throws IOException {
        ByteBuffer[] segments = this.getArena().getSegmentsForOutput();
        int tableSize = (segments.length + 2) & (~1);

        ByteBuffer table = ByteBuffer.allocate(4 * tableSize);
        table.order(ByteOrder.LITTLE_ENDIAN);

        table.putInt(0, segments.length - 1);

        for (int i = 0; i < segments.length; ++i) {
            table.putInt(4 * (i + 1), segments[i].limit() / 8);
        }

        // Any padding is already zeroed.
        while (table.hasRemaining()) {
            outputChannel.write(table);
        }

        for (ByteBuffer buffer : segments) {
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        }
    }
}
