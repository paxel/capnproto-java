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

import java.nio.ByteBuffer;

public final class Data {

    private static final DataView EMPTY = ByteBufferDataView.allocate(0);

    public static final class Factory implements FromPointerReaderBlobDefault<Reader>,
            PointerFactory<Builder, Reader>,
            FromPointerBuilderBlobDefault<Builder>,
            SetPointerBuilder<Builder, Reader> {

        private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
            @Override
            protected org.capnproto.Recycler<Reader> initialValue() {
                return new org.capnproto.Recycler<>(Reader::new);
            }
        };

        @Override
        public final Reader fromPointerReaderBlobDefault(SegmentDataContainer segment, int pointer, DataView defaultBuffer,
                int defaultOffset, int defaultSize) {
            return WireHelpers.readDataPointer(segment,
                    pointer, RECYCLER.get(),
                    r -> r.init(defaultBuffer, defaultOffset, defaultSize));
        }

        @Override
        public final Reader fromPointerReader(SegmentDataContainer segment, int pointer, int nestingLimit) {
            return WireHelpers.readDataPointer(segment, pointer, RECYCLER.get(), Reader::init);
        }

        @Override
        public final Builder fromPointerBuilderBlobDefault(GenericSegmentBuilder segment, int pointer,
                DataView defaultBuffer, int defaultOffset, int defaultSize) {
            return WireHelpers.getWritableDataPointer(pointer,
                    segment,
                    defaultBuffer,
                    defaultOffset,
                    defaultSize);
        }

        @Override
        public final Builder fromPointerBuilder(GenericSegmentBuilder segment, int pointer) {
            return WireHelpers.getWritableDataPointer(pointer,
                    segment,
                    null, 0, 0);
        }

        @Override
        public final Builder initFromPointerBuilder(GenericSegmentBuilder segment, int pointer, int size) {
            return WireHelpers.initDataPointer(pointer, segment, size);
        }

        @Override
        public final void setPointerBuilder(GenericSegmentBuilder segment, int pointer, Reader src) {
            WireHelpers.setDataPointer(pointer, segment, src);
        }
    }
    public static final Factory factory = new Factory();

    public static final class Reader implements Recycable<Reader> {

        private boolean recycled;
        private Recycler<Reader> recycler;
        private DataView buffer;
        public int offset; // in bytes
        public int size; // in bytes

        /**
         * Used by the Recycler.
         */
        public Reader() {
            recycled = true;
        }

        /**
         * Used by tests.
         *
         * @param buffer
         * @param offset
         * @param size
         */
        public Reader(DataView buffer, int offset, int size) {
            init(buffer, offset, size);
        }

        /**
         * Used in unit tests and builder.
         *
         * @param bytes The bytes wrapped by the Reader.
         */
        public Reader(byte[] bytes) {
            init(bytes);
        }

        public final void init() {
            this.buffer = EMPTY;
            this.offset = 0;
            this.size = 0;
            recycled = false;
        }

        public final void init(DataView buffer1, int offset1, int size1) {
            this.buffer = buffer1;
            this.offset = offset1 * 8;
            this.size = size1;
            this.recycled = false;
        }

        public final void init(byte[] bytes) {
            this.buffer = ByteBufferDataView.wrap(bytes);
            this.offset = 0;
            this.size = bytes.length;
            this.recycled = false;
        }

        public final int size() {
            checkRecycled();
            return this.size;
        }

        public int getOffset() {
            return offset;
        }

        public int getSize() {
            return size;
        }

        /**
         * Retrieve the internal buffer. This buffer contains the whole message, not just the Data portion. You need offset and size to find the data portion.
         *
         * @return the Buffer
         */
        public DataView getBuffer() {
            return buffer;
        }

        /**
         * Creates a new DataView, that shares the Data portion of the message. The new buffer only contains the data portion.
         *
         * @return a new buffer.
         */
        public ByteBuffer asByteBuffer() {
            checkRecycled();
            ByteBuffer dup = this.buffer.asReadOnlyBuffer();
            dup.position(this.offset);
            ByteBuffer result = dup.slice();
            result.limit(this.size);
            return result;
        }

        /**
         * Retrieves the bytes of the Data portion.
         *
         * @return the data.
         */
        public byte[] toArray() {
            checkRecycled();
            // todo: don't duplicate
            ByteBuffer dup = this.buffer.duplicate();
            byte result[] = new byte[this.size];
            dup.position(this.offset);
            dup.get(result, 0, this.size);
            return result;
        }

        @Override
        public String toString() {
            checkRecycled();
            return "Data{" + new ByteBufferFormatter().format(asByteBuffer()) + '}';
        }

        @Override
        public void init(Recycler<Reader> recycler) {
            this.recycler = recycler;
        }

        @Override
        public void recycle() {
            if (recycled) {
                throw new IllegalArgumentException("This reader is already recycled");
            }
            recycled = true;
            this.buffer = null;
            this.offset = 0;
            this.size = 0;
            if (recycler != null) {
                recycler.recycle(this);
            }
        }

        private void checkRecycled() throws IllegalStateException {
            if (recycled) {
                throw new IllegalStateException("Reader is recycled.");
            }
        }

        public void writeData(int offset, int size, DataView dst, int dstOffset) {
            buffer.write(offset, size, dst, dstOffset);
        }

    }

    public static final class Builder {

        private final DataView buffer;
        private final int offset; // in bytes
        private final int size; // in bytes

        public Builder() {
            this.buffer = ByteBufferDataView.allocate(0);
            this.offset = 0;
            this.size = 0;
        }

        public Builder(DataView buffer, int offset, int size) {
            this.buffer = buffer;
            this.offset = offset;
            this.size = size;
        }

        public ByteBuffer asByteBuffer() {
            ByteBuffer dup = this.buffer.duplicate();
            dup.position(this.offset);
            ByteBuffer result = dup.slice();
            result.limit(this.size);
            return result;
        }

        public byte[] toArray() {
            ByteBuffer dup = this.buffer.duplicate();
            byte result[] = new byte[this.size];
            dup.position(this.offset);
            dup.get(result, 0, this.size);
            return result;
        }

        public DataView getBuffer() {
            return buffer;
        }

        public int getOffset() {
            return offset;
        }

        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "Data{" + new ByteBufferFormatter().format(asByteBuffer()) + '}';
        }

        DataView getDataView() {
            return buffer;
        }
    }
}
