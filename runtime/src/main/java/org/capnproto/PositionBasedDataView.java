package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * This represents the position based Data View. These methods depend on the current position and/or limit of the data view.
 */
public interface PositionBasedDataView {

    /**
     * Puts the data src at the current position and advances the position accordingly.
     *
     * @param src The data src.
     */
    void put(PositionBasedDataView src);

    /**
     * Puts the data src at the current position and advances the position accordingly.
     *
     * @param src The data src.
     */
    void put(ByteBuffer src);

    /**
     * Retrieves the data of the current position into the given byte array and advances position accordingly.
     *
     * @param dst The destination array
     */
    void get(byte[] dst);

    /**
     * Rewinds this DataView. Sets the position to 0.
     */
    void rewindReader();

    /**
     * Retrieve the number of bytes between current position and limit.
     *
     * @return the number of bytes left to either write or read.
     */
    int remainingReadableBytes();

    /**
     * Sets the limit of this view.
     *
     * @param limit The new limit
     */
    void limitReadableBytes(int limit);

    /**
     * Retrieves the current limit.
     *
     * @return the limit.
     */
    int limit();

    /**
     * Writes data from this view into the channel. The data between position and limit is written and the position set to limit.
     *
     * @param outputChannel The destination channel.
     *
     * @throws IOException in case the channel can not process the data.
     */
    void write(WritableByteChannel outputChannel) throws IOException;

    /**
     * Reads date from the source to the current position until either no more data is available or limit is reached.
     *
     * @param source The source of the data.
     *
     * @return the number of bytes read.
     *
     * @throws java.io.IOException in case the source throws it.
     */
    public int read(ReadableByteChannel source) throws IOException;

    /**
     * Retrieve if position is less than limit.
     *
     * @return {@code true} if more bytes can be read.
     */
    boolean hasRemainingReadableBytes();

    /**
     * Retrieve if position is less than limit.
     *
     * @return {@code true} if more bytes can be written.
     */
    boolean hasRemainingWriteableBytes();

    /**
     * Retrieve a byte from current position and advance position by 1.
     *
     * @return the byte.
     */
    byte getByte();

    /**
     *
     * @return
     */
    int getInt();

    /**
     * Retrieve the current position.
     *
     * @return the current position.
     */
    int readerPosition();

    /**
     * Sets a new position.
     *
     * @param pos The new position.
     */
    void readerPosition(int pos);

    /**
     * Puts the given data at current position and advances the position by the length of the src.
     *
     * @param src The data to be put.
     */
    void put(byte[] src);
}
