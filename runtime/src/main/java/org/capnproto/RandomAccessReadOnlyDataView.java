package org.capnproto;

/**
 * This represents the index based data retrieval.
 */
public interface RandomAccessReadOnlyDataView {

    /**
     * Retrieve the byte at position index.
     *
     * @param index the index
     *
     * @return the byte.
     */
    byte get(int index);

    /**
     * Retrieve the short (2 bytes) at position index.
     *
     * @param index the index
     *
     * @return the short.
     */
    short getShort(int index);

    /**
     * Retrieve the int (4 bytes) at position index.
     *
     * @param index the index
     *
     * @return the int.
     */
    int getInt(int index);

    /**
     * Retrieve the long (8 bytes) at position index.
     *
     * @param index the index
     *
     * @return the long.
     */
    long getLong(int index);

    /**
     * Retrieve the float(4 bytes) at position index.
     *
     * @param index the index
     *
     * @return the float.
     */
    float getFloat(int index);

    /**
     * Retrieve the double (8 bytes) at position index.
     *
     * @param index the index
     *
     * @return the double.
     */
    double getDouble(int index);

}
