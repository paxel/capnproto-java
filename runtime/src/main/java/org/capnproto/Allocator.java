package org.capnproto;

/**
 * An object that allocates memory for a Cap'n Proto message as it is being built.
 */
public interface Allocator {
    /**
     * Allocates a ByteBuffer to be used as a segment in a message. The returned
     * buffer must contain at least `minimumSize` bytes, all of which MUST be
     * set to zero.
     */
    public DataView allocateSegment(int minimumSize);
}
