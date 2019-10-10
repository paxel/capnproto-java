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
import java.nio.charset.Charset;

public final class Text {

    public static final class Factory implements
            FromPointerReaderBlobDefault<Reader>,
            FromPointerBuilderBlobDefault<Builder>,
            PointerFactory<Builder, Reader>,
            SetPointerBuilder<Builder, Reader> {

        @Override
        public final Reader fromPointerReaderBlobDefault(SegmentDataContainer segment, int pointer, DataView defaultBuffer, int defaultOffset, int defaultSize) {
            return WireHelpers.readTextPointer(segment, pointer, defaultBuffer, defaultOffset, defaultSize);
        }

        @Override
        public final Reader fromPointerReader(SegmentDataContainer segment, int pointer, int nestingLimit) {
            return WireHelpers.readTextPointer(segment, pointer, null, 0, 0);
        }

        @Override
        public final Builder fromPointerBuilderBlobDefault(GenericSegmentBuilder segment, int pointer,
                DataView defaultBuffer, int defaultOffset, int defaultSize) {
            return WireHelpers.getWritableTextPointer(pointer,
                    segment,
                    defaultBuffer,
                    defaultOffset,
                    defaultSize);
        }

        @Override
        public final Builder fromPointerBuilder(GenericSegmentBuilder segment, int pointer) {
            return WireHelpers.getWritableTextPointer(pointer,
                    segment,
                    null, 0, 0);
        }

        @Override
        public final Builder initFromPointerBuilder(GenericSegmentBuilder segment, int pointer, int size) {
            return WireHelpers.initTextPointer(pointer, segment, size);
        }

        @Override
        public final void setPointerBuilder(GenericSegmentBuilder segment, int pointer, Reader value) {
            WireHelpers.setTextPointer(pointer, segment, value);
        }
    }
    public static final Factory factory = new Factory();

    public static final class Reader {

        private final DataView buffer;
        private final int offset; // in bytes
        private final int size; // in bytes, not including NUL terminator

        public DataView getBuffer() {
            return buffer;
        }

        public int getOffset() {
            return offset;
        }

        public int getSize() {
            return size;
        }

        public Reader() {
            this.buffer = ByteBufferDataView.allocate(0);
            this.offset = 0;
            this.size = 0;
        }

        public Reader(DataView buffer, int offset, int size) {
            this.buffer = buffer;
            this.offset = offset * 8;
            this.size = size;
        }

        public Reader(String value) {
            try {
                byte[] bytes = value.getBytes("UTF-8");
                this.buffer = ByteBufferDataView.wrap(bytes);
                this.offset = 0;
                this.size = bytes.length;
            } catch (java.io.UnsupportedEncodingException e) {
                throw new CapnProtoException("UTF-8 is unsupported");
            }
        }

        public final int size() {
            return this.size;
        }

        /**
         * Creates a new ByteBuffer, wrapping the data.
         *
         * @return the data.
         */
        public ByteBuffer asByteBuffer() {
            return ByteBuffer.wrap(toArray());
        }

        /**
         * Retrieves the bytes of the Data portion.
         *
         * @return the data.
         */
        public byte[] toArray() {
            byte[] result = new byte[size];
            buffer.readerPosition(offset);
            buffer.get(result);
            return result;
        }

        @Override
        public final String toString() {
            return new String(toArray(), Charset.forName("utf-8"));
        }
    }

    public static final class Builder {

        public final DataView buffer;
        public final int offset; // in bytes
        public final int size; // in bytes

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

        /**
         * Creates a new ByteBuffer, wrapping the data.
         *
         * @return the data.
         */
        public ByteBuffer asByteBuffer() {
            return ByteBuffer.wrap(toArray());
        }

        /**
         * Retrieves the bytes of the Data portion.
         *
         * @return the data.
         */
        public byte[] toArray() {
            byte[] result = new byte[size];
            buffer.readerPosition(offset);
            buffer.get(result);
            return result;
        }

        @Override
        public final String toString() {
            return new String(toArray(), Charset.forName("utf-8"));
        }

        void copy(Reader srcReader) {
            DataView src = srcReader.getBuffer();
            src.write(srcReader.getOffset(), srcReader.getSize(), buffer, offset);
        }

        void writeData(int offset, int size, DataView dataView) {
            dataView.write(this.offset, Math.min(size, this.size), buffer, offset);
        }

        void put(int i, byte get) {
            buffer.put(i, get);
        }

        DataView getBuffer() {
            return buffer;
        }

    }

}
