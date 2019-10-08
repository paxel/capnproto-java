package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.WritableByteChannel;
import static java.util.Objects.requireNonNull;

public class ByteBufferDataView implements DataView {

    private final ByteBuffer buffer;

    public static DataView allocate(int i) {
        return new ByteBufferDataView(ByteBuffer.allocate(i));
    }

    public void put(DataView src) {
        if (src instanceof ByteBufferDataView) {
            buffer.put(((ByteBufferDataView) src).buffer);
        } else {
            throw new IllegalArgumentException("Unsupported DataView: " + src.getClass());
        }
    }

    @Override
    public final int capacity() {
        return buffer.capacity();
    }

    @Override
    public byte get() {
        return buffer.get();
    }

    public static DataView wrap(byte[] bytes) {
        return new ByteBufferDataView(ByteBuffer.wrap(bytes));
    }

    public ByteBufferDataView(ByteBuffer buffer) {
        this.buffer = requireNonNull(buffer, "Parameter buffer can not be null.");
    }

    @Override
    public ByteBuffer get(byte[] dst) {
        return buffer.get(dst);
    }

    @Override
    public byte get(int i) {
        return buffer.get(i);
    }

    @Override
    public short getShort(int i) {
        return buffer.getShort(i);
    }

    @Override
    public int getInt(int i) {
        return buffer.getInt(i);
    }

    @Override
    public LongBuffer asLongBuffer() {
        return buffer.asLongBuffer();
    }

    @Override
    public float getFloat(int i) {
        return buffer.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        return buffer.getDouble(i);
    }

    @Override
    public void put(int position, byte b) {
        buffer.put(position, b);
    }

    @Override
    public void putShort(int i, short value) {
        buffer.putShort(i, value);
    }

    @Override
    public void putInt(int i, int value) {
        buffer.putInt(i, value);
    }

    @Override
    public void putFloat(int i, float value) {
        buffer.putFloat(i, value);
    }

    @Override
    public void putDouble(int i, double value) {
        buffer.putDouble(i, value);
    }

    @Override
    public void rewind() {
        buffer.rewind();
    }

    @Override
    public int remaining() {
        return buffer.remaining();
    }

    @Override
    public ByteBuffer slice() {
        return buffer.slice();
    }

    @Override
    public long getLong(int i) {
        return buffer.getLong(i);
    }

    @Override
    public void putLong(int i, long l) {
        buffer.putLong(i, l);
    }

    @Override
    public ByteBuffer duplicate() {
        return buffer.duplicate();
    }

    @Override
    public ByteBuffer asReadOnlyBuffer() {
        return buffer.asReadOnlyBuffer();
    }

    @Override
    public void write(int offset, int length, DataView dst, int dstOffset) {
        if (dst instanceof ByteBufferDataView) {

            // create a limited bb
            ByteBuffer tmpSrc = buffer.duplicate();
            tmpSrc.position(offset);
            tmpSrc.limit(offset + length);

            dst.position(dstOffset);
            ((ByteBufferDataView) dst).buffer.put(tmpSrc);
        } else {
            throw new IllegalArgumentException("Unsupported DataView: " + dst.getClass());
        }
    }

    @Override
    public int position() {
        return buffer.position();
    }

    @Override
    public void position(int pos) {
        buffer.position(pos);
    }

    @Override
    public void put(byte[] data) {
        buffer.put(data);
    }

    @Override
    public void put(byte[] data, int offset, int length) {
        buffer.put(data, offset, length);
    }

    @Override
    public void order(ByteOrder order) {
        buffer.order(order);
    }

    @Override
    public void limit(int i) {
        buffer.limit(i);
    }

    @Override
    public void write(WritableByteChannel outputChannel) throws IOException {
        outputChannel.write(buffer);
    }

    @Override
    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    @Override
    public int limit() {
        return buffer.limit();
    }

}
