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

public class StructReader {

    @FunctionalInterface
    public interface ReaderFactory<T> {

        T constructReader(SegmentDataContainer segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit);
    }

    public interface Factory<T> {

        abstract T constructReader(SegmentDataContainer segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit);
    }

    private static Runnable VALIDATOR_INIT = () -> {
    };
    private static Runnable VALIDATOR_DEINIT = () -> {
        throw new IllegalStateException("This reader is not initialized.");
    };

    protected volatile SegmentDataContainer segment;
    protected volatile int data; //byte offset to data section
    protected volatile int pointers; // word offset of pointer section
    protected volatile int dataSize; // in bits
    protected volatile short pointerCount;
    protected volatile int nestingLimit;
    private volatile Runnable validator = VALIDATOR_DEINIT;

    public StructReader() {
        deinit();
    }

    public StructReader(SegmentDataContainer segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) {
        init(segment, data, pointers, dataSize, pointerCount, nestingLimit);
    }

    public final void init(SegmentDataContainer segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) {
        this.segment = segment;
        this.data = data;
        this.pointers = pointers;
        this.dataSize = dataSize;
        this.pointerCount = pointerCount;
        this.nestingLimit = nestingLimit;
        validator = VALIDATOR_INIT;
    }

    public final void deinit() {
        this.segment = GenericSegmentReader.EMPTY;
        this.data = 0;
        this.pointers = 0;
        this.dataSize = 0;
        this.pointerCount = 0;
        this.nestingLimit = 0x7fff_ffff;
        validator = VALIDATOR_DEINIT;
    }

    protected final boolean _getBooleanField(int offset) {
        validator.run();
        // XXX should use unsigned operations
        if (offset < this.dataSize) {
            byte b = this.segment.getBuffer().get(this.data + offset / 8);

            return (b & (1 << (offset % 8))) != 0;
        } else {
            return false;
        }
    }

    protected final boolean _getBooleanField(int offset, boolean mask) {
        validator.run();
        return this._getBooleanField(offset) ^ mask;
    }

    protected final byte _getByteField(int offset) {
        validator.run();
        if ((offset + 1) * 8 <= this.dataSize) {
            return this.segment.getBuffer().get(this.data + offset);
        } else {
            return 0;
        }
    }

    protected final byte _getByteField(int offset, byte mask) {
        validator.run();
        return (byte) (this._getByteField(offset) ^ mask);
    }

    protected final short _getShortField(int offset) {
        validator.run();
        if ((offset + 1) * 16 <= this.dataSize) {
            return this.segment.getBuffer().getShort(this.data + offset * 2);
        } else {
            return 0;
        }
    }

    protected final short _getShortField(int offset, short mask) {
        validator.run();
        return (short) (this._getShortField(offset) ^ mask);
    }

    protected final int _getIntField(int offset) {
        validator.run();
        if ((offset + 1) * 32 <= this.dataSize) {
            return this.segment.getBuffer().getInt(this.data + offset * 4);
        } else {
            return 0;
        }
    }

    protected final int _getIntField(int offset, int mask) {
        validator.run();
        return this._getIntField(offset) ^ mask;
    }

    protected final long _getLongField(int offset) {
        validator.run();
        if ((offset + 1) * 64 <= this.dataSize) {
            return this.segment.getBuffer().getLong(this.data + offset * 8);
        } else {
            return 0;
        }
    }

    protected final long _getLongField(int offset, long mask) {
        validator.run();
        return this._getLongField(offset) ^ mask;
    }

    protected final float _getFloatField(int offset) {
        validator.run();
        if ((offset + 1) * 32 <= this.dataSize) {
            return this.segment.getBuffer().getFloat(this.data + offset * 4);
        } else {
            return 0;
        }
    }

    protected final float _getFloatField(int offset, int mask) {
        validator.run();
        if ((offset + 1) * 32 <= this.dataSize) {
            return Float.intBitsToFloat(this.segment.getBuffer().getInt(this.data + offset * 4) ^ mask);
        } else {
            return Float.intBitsToFloat(mask);
        }
    }

    protected final double _getDoubleField(int offset) {
        validator.run();
        if ((offset + 1) * 64 <= this.dataSize) {
            return this.segment.getBuffer().getDouble(this.data + offset * 8);
        } else {
            return 0;
        }
    }

    protected final double _getDoubleField(int offset, long mask) {
        validator.run();
        if ((offset + 1) * 64 <= this.dataSize) {
            return Double.longBitsToDouble(this.segment.getBuffer().getLong(this.data + offset * 8) ^ mask);
        } else {
            return Double.longBitsToDouble(mask);
        }
    }

    protected final boolean _pointerFieldIsNull(int ptrIndex) {
        validator.run();
        return ptrIndex >= this.pointerCount || this.segment.getBuffer().getLong((this.pointers + ptrIndex) * Constants.BYTES_PER_WORD) == 0;
    }

    protected final <T> T _getPointerField(FromPointerReader<T> factory, int ptrIndex) {
        validator.run();
        if (ptrIndex < this.pointerCount) {
            return factory.fromPointerReader(this.segment,
                    this.pointers + ptrIndex,
                    this.nestingLimit);
        } else {
            return factory.fromPointerReader(GenericSegmentReader.EMPTY,
                    0,
                    this.nestingLimit);
        }
    }

    protected final <T> T _getPointerField(FromPointerReaderRefDefault<T> factory, int ptrIndex, SegmentDataContainer defaultSegment, int defaultOffset) {
        validator.run();
        if (ptrIndex < this.pointerCount) {
            return factory.fromPointerReaderRefDefault(this.segment,
                    this.pointers + ptrIndex,
                    defaultSegment,
                    defaultOffset,
                    this.nestingLimit);
        } else {
            return factory.fromPointerReaderRefDefault(GenericSegmentReader.EMPTY,
                    0,
                    defaultSegment,
                    defaultOffset,
                    this.nestingLimit);
        }
    }

    protected final <T> T _getPointerField(FromPointerReaderBlobDefault<T> factory, int ptrIndex, java.nio.ByteBuffer defaultBuffer, int defaultOffset, int defaultSize) {
        validator.run();
        if (ptrIndex < this.pointerCount) {
            return factory.fromPointerReaderBlobDefault(this.segment,
                    this.pointers + ptrIndex,
                    defaultBuffer,
                    defaultOffset,
                    defaultSize);
        } else {
            return factory.fromPointerReaderBlobDefault(GenericSegmentReader.EMPTY,
                    0,
                    defaultBuffer,
                    defaultOffset,
                    defaultSize);
        }
    }

}
