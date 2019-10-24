package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import static java.util.Objects.requireNonNull;

/**
 * A DataView wrapping a ByteBuffer.
 */
public class ByteBufferDataView implements DataView {

    public static ByteBufferDataView allocate(int i) {
        return new ByteBufferDataView(ByteBuffer.allocate(i));
    }

    public static ByteBufferDataView allocateDirect(int i) {
        return new ByteBufferDataView(ByteBuffer.allocateDirect(i));
    }

    public static ByteBufferDataView wrap(byte[] bytes) {
        return new ByteBufferDataView(ByteBuffer.wrap(bytes));
    }

    private final ByteBuffer buffer;

    public ByteBufferDataView(ByteBuffer buffer) {
        this.buffer = requireNonNull(buffer, "Parameter buffer can not be null.");
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public void put(PositionBasedDataView src) {
        if (src instanceof ByteBufferDataView) {
            buffer.put(((ByteBufferDataView) src).buffer);
        } else {
            while (src.hasRemainingReadableBytes() && buffer.remaining() > 0) {
                buffer.put(src.getByte());
            }
        }
    }

    @Override
    public final int capacity() {
        return buffer.capacity();
    }

    @Override
    public byte getByte() {
        return buffer.get();
    }

    @Override
    public void get(byte[] dst) {
        buffer.get(dst);
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
    public void rewindReaderPosition() {
        buffer.rewind();
    }

    @Override
    public int remainingReadableBytes() {
        return buffer.remaining();
    }

    @Override
    public ByteBufferDataView slice() {
        final ByteBufferDataView result = new ByteBufferDataView(buffer.slice());
        result.order(buffer.order());
        return result;
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
    public void writeTo(DataView dst, int dstOffset, int srcOffset, int length) {
        if (dst instanceof ByteBufferDataView) {

            // we temporary limit our buffer. we have to reset that later
            int srcLimit = buffer.limit();
            buffer.position(srcOffset);
            buffer.limit(srcOffset + length);

            ByteBuffer tmpDst = getDstByteBuffer(dst);
            // we extend the limit here. Deal with it.
            if (tmpDst.limit() <= dstOffset) {
                tmpDst.limit(dstOffset + length);
            }
            tmpDst.position(dstOffset);
            tmpDst.put(buffer);
            buffer.limit(srcLimit);
        } else {
            for (int i = 0; i < length; i++) {
                dst.put(i + dstOffset, buffer.get(i + srcOffset));
            }

        }
    }

    private ByteBuffer getDstByteBuffer(DataView dst) {
        ByteBuffer tmpDst = ((ByteBufferDataView) dst).buffer;
        if (tmpDst == buffer) {
            // if the data is to be written into the same ByteBuffer, we have to duplicate it.
            final ByteBuffer duplicate = tmpDst.duplicate();
            duplicate.order(tmpDst.order());
            return duplicate;
        }
        return tmpDst;
    }

    @Override
    public int readerPosition() {
        return buffer.position();
    }

    @Override
    public void readerPosition(int pos) {
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
        if (buffer.position() > i) {
            buffer.position(i);
        }
        buffer.limit(i);
    }

    @Override
    public void writeTo(WritableByteChannel outputChannel) throws IOException {
        outputChannel.write(buffer);
    }

    @Override
    public boolean hasRemainingReadableBytes() {
        return buffer.hasRemaining();
    }

    @Override
    public int limit() {
        return buffer.limit();
    }

    @Override
    public int readFrom(ReadableByteChannel source) throws IOException {
        return source.read(buffer);
    }

    @Override
    public int getInt() {
        return buffer.getInt();
    }

    @Override
    public void put(ByteBuffer src) {
        buffer.put(src);
    }

    @Override
    public boolean hasRemainingWriteableBytes() {
        // no writer index
        return hasRemainingReadableBytes();
    }

    @Override
    public void writerPosition(int dstOffset) {
        // no writer index
        readerPosition(dstOffset);
    }

    @Override
    public void rewindWriterPosition() {
        // no writer index
        rewindReaderPosition();
    }

    @Override
    public DataView duplicate() {
        final ByteBufferDataView result = new ByteBufferDataView(buffer.duplicate());
        result.order(buffer.order());
        return result;
    }

}
