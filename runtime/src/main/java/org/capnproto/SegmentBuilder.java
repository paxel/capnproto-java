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

public final class SegmentBuilder implements GenericSegmentBuilder {

    private static final int ERAZER_SIZE = 8000;

    public int pos = 0; // in words
    public int id = 0;
    public final DataView buffer;
    // store the AllocatingArena
    private final AllocatingArena arena;

    public SegmentBuilder(DataView buf, AllocatingArena arena) {
        this.buffer = buf;
        this.arena = arena;
    }

    // the total number of words the buffer can hold
    private int capacity() {
        buffer.rewindReader();
        return buffer.remainingReadableBytes() / 8;
    }

    // return how many words have already been allocated
    @Override
    public final int currentSize() {
        return this.pos;
    }

    /*
       Allocate `amount` words.
     */
    @Override
    public final int allocate(int amount) {
        assert amount >= 0 : "tried to allocate a negative number of words";

        if (amount > this.capacity() - this.currentSize()) {
            return FAILED_ALLOCATION; // no space left;
        } else {
            int result = this.pos;
            this.pos += amount;
            return result;
        }
    }

    @Override
    public final AllocatingArena getArena() {
        return this.arena;
    }

    @Override
    public final boolean isWritable() {
        // TODO support external non-writable segments
        return true;
    }

    @Override
    public void put(int index, long value) {
        buffer.putLong(index * Constants.BYTES_PER_WORD, value);
    }

    @Override
    public DataView getBuffer() {
        return buffer;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public DataView getSegmentForOutput() {
        DataView slice = buffer.slice();
        slice.rewindReader();
        slice.limitReadableBytes(currentSize() * Constants.BYTES_PER_WORD);
        return slice;
    }

    @Override
    public void setId(int len) {
        this.id = len;
    }

    @Override
    public long get(int index) {
        return buffer.getLong(index * Constants.BYTES_PER_WORD);
    }

    @Override
    public final void clear() {
        int posInBytes = pos * Constants.BYTES_PER_WORD;
        byte[] erazer = new byte[ERAZER_SIZE];
        // write the erazer into buffer until the erazer is too big for the remaining data.
        int current = 0;
        buffer.readerPosition(0);
        while (posInBytes - current > ERAZER_SIZE) {
            buffer.put(erazer);
            current += ERAZER_SIZE;
        }
        // write a fraction of the erazer into the buffer and reset all
        int remaining = posInBytes - current;
        buffer.put(erazer, 0, remaining);
        buffer.readerPosition(0);
        this.pos = 0;
    }
}
