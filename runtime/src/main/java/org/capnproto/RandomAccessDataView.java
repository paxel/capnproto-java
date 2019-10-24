package org.capnproto;

/**
 * This interface represents methods that access the Data of the DataView ignoring the current position.
 */
public interface RandomAccessDataView {

    /**
     * Put the byte at position index
     *
     * @param index the index
     * @param value the data to put.
     */
    void put(int index, byte value);

    /**
     * Put the short (2 bytes) at position index
     *
     * @param index the index
     * @param value the data to put.
     */
    void putShort(int index, short value);

    /**
     * Put the int (4 bytes) at position index
     *
     * @param index the index
     * @param value the data to put.
     */
    void putInt(int index, int value);

    /**
     * Put the long (8 bytes) at position index
     *
     * @param index the index
     * @param value the data to put.
     */
    void putLong(int index, long value);

    /**
     * Put the float (4 bytes) at position index
     *
     * @param index the index
     * @param value the data to put.
     */
    void putFloat(int index, float value);

    /**
     * Put the double (8 bytes) at position index
     *
     * @param index the index
     * @param value the data to put.
     */
    void putDouble(int index, double value);

    /**
     * Puts data into this DataView
     *
     * @param bytes  The data to put.
     * @param index  The index where the data to put
     * @param length The amount of data to put.
     */
    void put(byte[] bytes, int index, int length);

    /**
     * Writes data from this DataView into the target DataView
     *
     * @param index       The index of the data in this data view
     * @param length      The length of data to be written from this data view.
     * @param dstDataView The destination data view
     * @param dstIndex    The destination index where to write the Data.
     */
    void writeTo(DataView dstDataView, int dstIndex, int index, int length);

}
