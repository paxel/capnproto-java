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

    static ByteBuffer makeByteBuffer(int bytes) {
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
    private Function<ByteBuffer[], AllocatedArena> arenaFactory = x -> new ReaderArena(x, 0);
    private Function<Integer, ByteBuffer> byteBufferFactory = AllocatedArenaBuilder::makeByteBuffer;

    public SegmentCountValidator getSegmentCountValidator() {
        return segmentCountValidator;
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

    public AllocatedArena build(ReadableByteChannel bc) throws IOException {
        return arenaFactory.apply(reader.read(bc));
    }

    private ByteBuffer[] createInternalByteBuffers(ReadableByteChannel bc) throws IOException {
        firstWord.rewind();
        fillBuffer(firstWord, bc);
        final int segmentCount = 1 + firstWord.getInt(0);
        final int segment0Size = firstWord.getInt(4);

        int totalWords = segment0Size;
        segmentCountValidator.validate(segmentCount);
        // in words
        List<Integer> moreSizes = new ArrayList<>();
        segmentSizeHeader.rewind();
        segmentSizeHeader.limit(4 * (segmentCount & ~1));
        fillBuffer(segmentSizeHeader, bc);
        for (int segmentHeaderIndex = 0; segmentHeaderIndex < segmentCount - 1; ++segmentHeaderIndex) {
            int size = segmentSizeHeader.getInt(segmentHeaderIndex * 4);
            moreSizes.add(size);
            totalWords += size;
        }

        ByteBuffer allSegments = byteBufferFactory.apply(totalWords * Constants.BYTES_PER_WORD);
        fillBuffer(allSegments, bc);
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

    /*
     * Upon return, `bb.position()` will be at the end of the message.
     */
    public MessageReader build(ByteBuffer bb, ReaderOptions options) throws IOException {
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

        return new MessageReader(segmentSlices, options);
    }

    private void fillBuffer(ByteBuffer buffer, ReadableByteChannel bc) throws IOException {
        while (buffer.hasRemaining()) {
            int r = bc.read(buffer);
            if (r < 0) {
                throw new IOException("premature EOF");
            }
        }
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
