package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AllocatedArenaBuilder {

    private static final int INT_SIZE = 4;

    private static ByteBuffer makeByteBuffer(int bytes) {
        ByteBuffer result = ByteBuffer.allocate(bytes);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.mark();
        return result;
    }

    private static void segmentCountValidator(final int segmentCount) throws IOException {
        if (segmentCount > Constants.MAX_SEGMENTS) {
            throw new IOException("too many segments");
        }
    }

    // allocate mem for parsing the header once
    private final ByteBuffer firstWord = makeByteBuffer(Constants.BYTES_PER_WORD);
    private final ByteBuffer segmentSizeHeader = makeByteBuffer(Constants.MAX_SEGMENTS * INT_SIZE);

    private SegmentCountValidator segmentCountValidator = AllocatedArenaBuilder::segmentCountValidator;
    private SegmentReader reader = this::createInternalByteBuffers;
    private Function<ByteBuffer[], AllocatedArena> arenaFactory = SimpleReaderArena::new;
    private Function<Integer, ByteBuffer> byteBufferFactory = AllocatedArenaBuilder::makeByteBuffer;

    public SegmentCountValidator getSegmentCountValidator() {
        return segmentCountValidator;
    }

    public SegmentReader getReader() {
        return reader;
    }

    public AllocatedArenaBuilder setReader(SegmentReader reader) {
        this.reader = reader;
        return this;
    }

    public AllocatedArenaBuilder setSegmentCountValidator(SegmentCountValidator segmentCountValidator) {
        this.segmentCountValidator = segmentCountValidator;
        return this;
    }

    public Function<ByteBuffer[], AllocatedArena> getArenaFactory() {
        return arenaFactory;
    }

    public AllocatedArenaBuilder setArenaFactory(Function<ByteBuffer[], AllocatedArena> arenaFactory) {
        this.arenaFactory = arenaFactory;
        return this;
    }

    public Function<Integer, ByteBuffer> getByteBufferFactory() {
        return byteBufferFactory;
    }

    public AllocatedArenaBuilder setByteBufferFactory(Function<Integer, ByteBuffer> byteBufferFactory) {
        this.byteBufferFactory = byteBufferFactory;
        return this;
    }

    /**
     * Builds an Arena if the input contains valid data.
     *
     * @param input the input data.
     * @return a new arena or null if input was at EOF
     * @throws IOException if reading was impossible or input data invalid.
     */
    public AllocatedArena build(ReadableByteChannel input) throws IOException {
        final ByteBuffer[] bb = reader.read(input);
        if (bb == null) {
            return null;
        }
        return arenaFactory.apply(bb);
    }

    private ByteBuffer[] createInternalByteBuffers(ReadableByteChannel bc) throws IOException {
        firstWord.rewind();
        int read = fillBuffer(bc, firstWord);
        if (read == 0) {
            // EOF at the beginning
            return null;
        } else if (read < Constants.BYTES_PER_WORD) {
            throw new IOException("Incomplete data: EOF after " + read);
        }
        final int segmentCount = 1 + firstWord.getInt(0);
        final int segment0Size = firstWord.getInt(4);

        int totalWords = segment0Size;
        segmentCountValidator.validate(segmentCount);
        // in words
        List<Integer> moreSizes = new ArrayList<>();
        segmentSizeHeader.rewind();
        final int segmentSizeHeaderSize = 4 * (segmentCount & ~1);
        segmentSizeHeader.limit(segmentSizeHeaderSize);
        read = fillBuffer(bc, segmentSizeHeader);
        if (read < segmentSizeHeaderSize) {
            throw new IOException("Incomplete data: EOF after " + read + Constants.BYTES_PER_WORD);
        }
        for (int segmentHeaderIndex = 0; segmentHeaderIndex < segmentCount - 1; ++segmentHeaderIndex) {
            int size = segmentSizeHeader.getInt(segmentHeaderIndex * 4);
            moreSizes.add(size);
            totalWords += size;
        }

        ByteBuffer allSegments = byteBufferFactory.apply(totalWords * Constants.BYTES_PER_WORD);
        read = fillBuffer(bc, allSegments);
        if (read < segmentSizeHeaderSize) {
            throw new IOException("Incomplete data: EOF after " + read + segmentSizeHeaderSize + Constants.BYTES_PER_WORD);
        }
        ByteBuffer[] segmentSlices = new ByteBuffer[segmentCount];
        allSegments.rewind();
        segmentSlices[0] = allSegments.slice();
        segmentSlices[0].limit(segment0Size * Constants.BYTES_PER_WORD);
        segmentSlices[0].order(ByteOrder.LITTLE_ENDIAN);
        int offset = segment0Size;
        for (int ii = 1; ii < segmentCount; ++ii) {
            allSegments.position(offset * Constants.BYTES_PER_WORD);
            segmentSlices[ii] = allSegments.slice();
            segmentSlices[ii].limit(moreSizes.get(ii - 1) * Constants.BYTES_PER_WORD);
            segmentSlices[ii].order(ByteOrder.LITTLE_ENDIAN);
            offset += moreSizes.get(ii - 1);
        }
        return segmentSlices;
    }

    /**
     * Creates an AllocatedArena by slicing the given ByteBuffer. The ByteBuffer
     * will be LittleEndian and positioned after the message on return. If the
     * Message is truncated an IllegalArgumentException is thrown.
     *
     * @param bb The
     * @return an AllocatedArena or null if bb was empty.
     * @throws IOException If the SegmentCountValidator does.
     * @throws IllegalArgumentException in case the message is trunctaed.
     */
    public AllocatedArena build(ByteBuffer bb) throws IOException {
        if (bb.remaining() == 0) {
            return null;
        }
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int segmentCount = 1 + bb.getInt();

        this.segmentCountValidator.validate(segmentCount);

        ByteBuffer[] segmentSlices = new ByteBuffer[segmentCount];

        int segmentSizesBase = bb.position();
        int segmentSizesSize = segmentCount * 4;

        int align = Constants.BYTES_PER_WORD - 1;
        int segmentBase = (segmentSizesBase + segmentSizesSize + align) & ~align;

        int totalWords = 0;

        for (int ii = 0; ii < segmentCount; ++ii) {
            int segmentSize = bb.getInt(segmentSizesBase + ii * 4);

            bb.position(segmentBase + totalWords * Constants.BYTES_PER_WORD);
            segmentSlices[ii] = bb.slice();
            segmentSlices[ii].limit(segmentSize * Constants.BYTES_PER_WORD);
            segmentSlices[ii].order(ByteOrder.LITTLE_ENDIAN);

            totalWords += segmentSize;
        }
        bb.position(segmentBase + totalWords * Constants.BYTES_PER_WORD);

        return arenaFactory.apply(segmentSlices);
    }

    /**
     * Reads as many bytes as possible into the
     *
     * @param source the source
     * @param target the target
     * @return how many bytes were read.
     * @throws IOException from
     * {@link ReadableByteChannel#read(java.nio.ByteBuffer)}.
     *
     */
    private int fillBuffer(ReadableByteChannel source, ByteBuffer target) throws IOException {
        int read = 0;
        while (target.hasRemaining()) {
            int r = source.read(target);
            if (r < 0) {
                break;
            }
            read += r;
        }
        return read;
    }

    @FunctionalInterface
    public static interface SegmentCountValidator {

        void validate(int count) throws IOException;
    }

    @FunctionalInterface
    public static interface SegmentReader {

        ByteBuffer[] read(ReadableByteChannel in) throws IOException;
    }
}
