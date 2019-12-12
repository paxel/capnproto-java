package org.capnproto;

import java.nio.ByteOrder;

public interface DataView extends RandomAccessDataView, RandomAccessReadOnlyDataView, PositionBasedDataView {

    static final byte[] ERAZER = new byte[1024];

    /**
     * Create a copy of this DataView of the given range. The new View has the same endianness as the this View.
     *
     * @param offset The start of the range
     * @param size   The size of the range.
     *
     * @return The new DataView.
     */
    DataView copy(int offset, int size);

    /**
     * Some implementations support recycling of the DataView.
     */
    default void recycle() {
    }

    /**
     * Retrieves the maximum data in this view.
     *
     * @return the maximum number of bytes in this View.
     */
    int capacity();

    /**
     * Set the order of this DataView and all DataViews that are generated from this. If not set, the ByteOrder is BIG ENDIAN.
     *
     * @param order the byte order.
     */
    void order(ByteOrder order);

    /**
     * Creates a new DataView that has the same endianness as this DataView and starts from this DataViews current position.
     * The data is shared, the positions and limit not.
     *
     * @return a new DataView.
     */
    DataView slice();

    default void zero(int index, int length) {

        // TODO write zeroes implementation
        int pos = 0;
        // store the buffer pos
        int position = readerPosition();
        // go to the start of the clear area
        readerPosition(index);
        final int size = ERAZER.length;
        while (length > pos + size) {
            // zero out in ERAZER steps
            put(ERAZER);
            pos += size;
        }
        // zero the rest
        put(ERAZER, 0, length - pos);

        // reset the buffer pos just to be sure
        readerPosition(position);
    }

    /**
     * Creates a new DataView that has the same endianness, reader and writer position and limit as this DataView.
     * The data is shared, the positions and limit not.
     *
     * @return a new DataView.
     */
    public DataView duplicate();
}
