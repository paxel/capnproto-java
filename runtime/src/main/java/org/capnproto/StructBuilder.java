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

import java.util.function.Consumer;

public class StructBuilder {

    public interface Factory<T> {

        T constructBuilder(GenericSegmentBuilder segment, int data, int pointers, int dataSize,
                short pointerCount);

        StructSize structSize();
    }
    private static final Runnable VALIDATOR_INIT = () -> {
    };
    private static final Runnable VALIDATOR_DEINIT = () -> {
        throw new IllegalStateException("This reader is not initialized.");
    };

    protected GenericSegmentBuilder segment;
    protected int data; // byte offset to data section
    protected int pointers; // word offset of pointer section
    protected int dataSize; // in bits
    protected short pointerCount;
    protected Runnable validator = VALIDATOR_DEINIT;
    private Consumer<StructBuilder> recycleAction = f -> {
    };

    public StructBuilder() {
        deinit();
    }

    protected final void deinit() {
        this.segment = null;
        this.data = 0;
        this.pointers = 0;
        this.dataSize = 0;
        this.pointerCount = 0;
        validator = VALIDATOR_DEINIT;
    }

    protected final StructBuilder _init(GenericSegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) {
        this.segment = segment;
        this.data = data;
        this.pointers = pointers;
        this.dataSize = dataSize;
        this.pointerCount = pointerCount;
        validator = VALIDATOR_INIT;
        return this;
    }

    protected final boolean _getBooleanField(int offset) {
        validator.run();
        int bitOffset = offset;
        int position = this.data + (bitOffset / 8);
        return (this.segment.getBuffer().get(position) & (1 << (bitOffset % 8))) != 0;
    }

    protected final boolean _getBooleanField(int offset, boolean mask) {
        validator.run();
        return this._getBooleanField(offset) ^ mask;
    }

    protected final void _setBooleanField(int offset, boolean value) {
        validator.run();
        int bitOffset = offset;
        byte bitnum = (byte) (bitOffset % 8);
        int position = this.data + (bitOffset / 8);
        byte oldValue = this.segment.getBuffer().get(position);
        this.segment.getBuffer().put(position,
                (byte) ((oldValue & (~(1 << bitnum))) | ((value ? 1 : 0) << bitnum)));
    }

    protected final void _setBooleanField(int offset, boolean value, boolean mask) {
        validator.run();
        this._setBooleanField(offset, value ^ mask);
    }

    protected final byte _getByteField(int offset) {
        validator.run();
        return this.segment.getBuffer().get(this.data + offset);
    }

    protected final byte _getByteField(int offset, byte mask) {
        validator.run();
        return (byte) (this._getByteField(offset) ^ mask);
    }

    protected final void _setByteField(int offset, byte value) {
        validator.run();
        this.segment.getBuffer().put(this.data + offset, value);
    }

    protected final void _setByteField(int offset, byte value, byte mask) {
        validator.run();
        this._setByteField(offset, (byte) (value ^ mask));
    }

    protected final short _getShortField(int offset) {
        validator.run();
        return this.segment.getBuffer().getShort(this.data + offset * 2);
    }

    protected final short _getShortField(int offset, short mask) {
        validator.run();
        return (short) (this._getShortField(offset) ^ mask);
    }

    protected final void _setShortField(int offset, short value) {
        validator.run();
        this.segment.getBuffer().putShort(this.data + offset * 2, value);
    }

    protected final void _setShortField(int offset, short value, short mask) {
        validator.run();
        this._setShortField(offset, (short) (value ^ mask));
    }

    protected final int _getIntField(int offset) {
        validator.run();
        return this.segment.getBuffer().getInt(this.data + offset * 4);
    }

    protected final int _getIntField(int offset, int mask) {
        validator.run();
        return this._getIntField(offset) ^ mask;
    }

    protected final void _setIntField(int offset, int value) {
        validator.run();
        this.segment.getBuffer().putInt(this.data + offset * 4, value);
    }

    protected final void _setIntField(int offset, int value, int mask) {
        validator.run();
        this._setIntField(offset, value ^ mask);
    }

    protected final long _getLongField(int offset) {
        validator.run();
        return this.segment.getBuffer().getLong(this.data + offset * 8);
    }

    protected final long _getLongField(int offset, long mask) {
        validator.run();
        return this._getLongField(offset) ^ mask;
    }

    protected final void _setLongField(int offset, long value) {
        validator.run();
        this.segment.getBuffer().putLong(this.data + offset * 8, value);
    }

    protected final void _setLongField(int offset, long value, long mask) {
        validator.run();
        this._setLongField(offset, value ^ mask);
    }

    protected final float _getFloatField(int offset) {
        validator.run();
        return this.segment.getBuffer().getFloat(this.data + offset * 4);
    }

    protected final float _getFloatField(int offset, int mask) {
        validator.run();
        return Float.intBitsToFloat(
                this.segment.getBuffer().getInt(this.data + offset * 4) ^ mask);
    }

    protected final void _setFloatField(int offset, float value) {
        validator.run();
        this.segment.getBuffer().putFloat(this.data + offset * 4, value);
    }

    protected final void _setFloatField(int offset, float value, int mask) {
        validator.run();
        this.segment.getBuffer().putInt(this.data + offset * 4,
                Float.floatToIntBits(value) ^ mask);
    }

    protected final double _getDoubleField(int offset) {
        validator.run();
        return this.segment.getBuffer().getDouble(this.data + offset * 8);
    }

    protected final double _getDoubleField(int offset, long mask) {
        validator.run();
        return Double.longBitsToDouble(
                this.segment.getBuffer().getLong(this.data + offset * 8) ^ mask);
    }

    protected final void _setDoubleField(int offset, double value) {
        validator.run();
        this.segment.getBuffer().putDouble(this.data + offset * 8, value);
    }

    protected final void _setDoubleField(int offset, double value, long mask) {
        validator.run();
        this.segment.getBuffer().putLong(this.data + offset * 8,
                Double.doubleToLongBits(value) ^ mask);
    }

    protected final boolean _pointerFieldIsNull(int ptrIndex) {
        validator.run();
        return ptrIndex >= this.pointerCount || this.segment.getBuffer().getLong((this.pointers + ptrIndex) * Constants.BYTES_PER_WORD) == 0;
    }

    protected final void _clearPointerField(int ptrIndex) {
        validator.run();
        int pointer = this.pointers + ptrIndex;
        WireHelpers.zeroObject(this.segment, pointer);
        this.segment.getBuffer().putLong(pointer * 8, 0L);
    }

    protected final <T> T _getPointerField(FromPointerBuilder<T> factory, int index) {
        validator.run();
        return factory.fromPointerBuilder(this.segment, this.pointers + index);
    }

    protected final <T> T _getPointerField(FromPointerBuilderRefDefault<T> factory, int index, SegmentDataContainer defaultSegment, int defaultOffset) {
        validator.run();
        return factory.fromPointerBuilderRefDefault(this.segment, this.pointers + index, defaultSegment, defaultOffset);
    }

    protected final <T> T _getPointerField(FromPointerBuilderBlobDefault<T> factory, int index, DataView defaultBuffer, int defaultOffset, int defaultSize) {
        validator.run();
        return factory.fromPointerBuilderBlobDefault(this.segment, this.pointers + index, defaultBuffer, defaultOffset, defaultSize);
    }

    protected final <T> T _initPointerField(FromPointerBuilder<T> factory, int index, int elementCount) {
        validator.run();
        return factory.initFromPointerBuilder(this.segment, this.pointers + index, elementCount);
    }

    protected final <Builder, Reader> void _setPointerField(SetPointerBuilder<Builder, Reader> factory, int index, Reader value) {
        validator.run();
        factory.setPointerBuilder(this.segment, this.pointers + index, value);
    }

    protected final void _copyContentFrom(StructReader other) {
        validator.run();
        // Determine the amount of data the builders have in common.
        int sharedDataSize = java.lang.Math.min(this.dataSize, other.dataSize);
        int sharedPointerCount = java.lang.Math.min(this.pointerCount, other.pointerCount);

        if (other.segment == this.segment
                && ((sharedDataSize > 0 && other.data == this.data)
                || (sharedPointerCount > 0 && other.pointers == this.pointers))) {
            // At least one of the section pointers is pointing to ourself. Verify that the other is too
            // (but ignore empty sections).
            if ((sharedDataSize == 0 || other.data == this.data)
                    && (sharedPointerCount == 0 || other.pointers == this.pointers)) {
                throw new Error("Only one of the section pointers is pointing to ourself");
            }

            // So `other` appears to be a reader for this same struct. No copying is needed.
            return;
        }

        if (this.dataSize > sharedDataSize) {
            // Since the target is larger than the source, make sure to zero out the extra bits that the
            // source doesn't have.
            if (this.dataSize == 1) {
                this._setBooleanField(0, false);
            } else {
                int unshared = this.data + sharedDataSize / Constants.BITS_PER_BYTE;
                WireHelpers.memClear(this.segment.getBuffer(),
                        unshared,
                        (this.dataSize - sharedDataSize) / Constants.BITS_PER_BYTE);
            }
        }

        // Copy over the shared part.
        if (sharedDataSize == 1) {
            this._setBooleanField(0, other._getBooleanField(0));
        } else {
            WireHelpers.memcpy(this.segment.getBuffer(),
                    this.data,
                    other.segment.getBuffer(),
                    other.data,
                    sharedDataSize / Constants.BITS_PER_BYTE);
        }

        // Zero out all pointers in the target.
        for (int ii = 0; ii < this.pointerCount; ++ii) {
            WireHelpers.zeroObject(this.segment, this.pointers + ii);
        }
        this.segment.getBuffer().putLong(this.pointers * Constants.BYTES_PER_WORD, 0);

        for (int ii = 0; ii < sharedPointerCount; ++ii) {
            WireHelpers.copyPointer(this.segment,
                    this.pointers + ii,
                    other.segment,
                    other.pointers + ii,
                    other.nestingLimit);
        }
    }

    protected void onRecycle(Consumer<StructBuilder> recycleAction) {
        this.recycleAction = recycleAction;
    }

    /**
     * If this StructReader was generated by a Pool, it will be made available
     * in this pool again, otherwise nothing happens.
     * <p>
     * Pooled StructReader might become unusable after recycle() was called.
     * Best practice is to remove references to the Reader after calling
     * recycle.
     */
    public void recycle() {
        if (validator == VALIDATOR_DEINIT) {
            throw new IllegalStateException("The Reader is already deinitialized. Probably recycle was called twice.");
        }
        this.recycleAction.accept(this);
    }

}
