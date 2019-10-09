package org.capnproto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public interface DataView extends RandomAccessDataView, RandomAccessReadOnlyDataView, PositionBasedDataView {

    static final byte[] ERAZER = new byte[1024];

    int capacity();

    void order(ByteOrder LITTLE_ENDIAN);

    LongBuffer asLongBuffer();

    ByteBuffer slice();

    ByteBuffer duplicate();

    ByteBuffer asReadOnlyBuffer();

    default void zero(int index, int length) {

        // TODO write zeroes implementation
        int pos = 0;
        // store the buffer pos
        int position = position();
        // go to the start of the clear area
        position(index);
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
