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

public final class MessageReader {

    // must be accessible as long the scala tests access it
    final AllocatedArena arena;
    private final int nestingLimit;
    private final long serializedSize;

    /**
     * Construct a MessageReader with an injected custom {@link Arena}, that can
     * provide custom {@link GenericSegmentReader}.
     *
     * @param arena the Arena implementation.
     */
    public MessageReader(AllocatedArena arena) {
        this.arena = arena;
        // as the nesting limit is currently completely ignored, we use the default.
        this.nestingLimit = ReaderOptions.DEFAULT_NESTING_LIMIT;
        this.serializedSize = calculateSize();
    }

    public MessageReader(DataView[] segmentSlices, ReaderOptions options) {
        this.nestingLimit = options.nestingLimit;
        this.arena = new ReaderArena(segmentSlices, options.traversalLimitInWords);
        this.serializedSize = calculateSize();
    }

    public AllocatedArena getArena() {
        return arena;
    }

    public <T> T getRoot(FromPointerReader<T> factory) {
        GenericSegmentReader segment = this.arena.tryGetSegment(0);
        AnyPointer.Reader any = new AnyPointer.Reader(segment, 0, this.nestingLimit);
        return any.getAs(factory);
    }

    /**
     * Retrieve the Size in Bytes of the data that generated this Reader.
     *
     * @return The size in bytes.
     */
    public long getSerializedSize() {
        return serializedSize;

    }

    private long calculateSize() {
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

        // The content of each segment, in order.
        for (GenericSegmentReader segment : arena.getSegments()) {
            bytes += segment.getSize();
        }

        return bytes;
    }
}
