package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.WritableByteChannel;

public interface DataView {

    static final byte[] ERAZER = new byte[1024];

    int capacity();

    void put(DataView src);

    ByteBuffer get(byte[] dst);

    byte get(int i);

    short getShort(int i);

    int getInt(int i);

    float getFloat(int i);

    double getDouble(int i);

    void put(int position, byte b);

    void putShort(int i, short value);

    void putInt(int i, int value);

    void putFloat(int i, float value);

    void putDouble(int i, double value);

    void rewind();

    int remaining();

    ByteBuffer slice();

    void order(ByteOrder LITTLE_ENDIAN);

    void limit(int i);

    void write(WritableByteChannel outputChannel) throws IOException;

    boolean hasRemaining();

    byte get();

    LongBuffer asLongBuffer();

    int limit();

    long getLong(int i);

    void putLong(int i, long l);

    ByteBuffer duplicate();

    ByteBuffer asReadOnlyBuffer();

    void write(int srcOffset, int srcSize, DataView dstDataView, int dstOffset);

    int position();

    void position(int dstByteOffset);

    void put(byte[] ERAZER);

    void put(byte[] ERAZER, int i, int i0);

    default void zero(int dstByteOffset, int length) {

        // TODO write zeroes implementation
        int pos = 0;
        // store the buffer pos
        int position = position();
        // go to the start of the clear area
        position(dstByteOffset);
        final int size = ERAZER.length;
        while (length > pos + size) {
            // zero out in ERAZER steps
            put(ERAZER);
            pos += size;
        }
        // zero the rest
        put(ERAZER, 0, length - pos);

        // reset the buffer pos just to be sure
        position(position);
    }

}
