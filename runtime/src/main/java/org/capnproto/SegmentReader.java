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

public class SegmentReader implements GenericSegmentReader {

    public final DataView buffer;
    private final AllocatedArena arena;
    private final int size;

    /**
     * This constructor will rewind the bytebuffer to find out it's size.
     *
     * @param buffer the data of this segment.
     * @param arena The parent Arena.
     */
    public SegmentReader(DataView buffer, AllocatedArena arena) {
        this.buffer = buffer;
        buffer.rewindReader();
        size = buffer.remainingReadableBytes();
        this.arena = arena;
    }

    @Override
    public long get(int index) {
        return buffer.getLong(index * Constants.BYTES_PER_WORD);
    }

    @Override
    public AllocatedArena getArena() {
        return arena;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public DataView getBuffer() {
        return buffer;
    }

}
