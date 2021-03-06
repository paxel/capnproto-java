package org.capnproto;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.function.Function;

public class AllocatedArenaBuilder {

    private static final int INT_SIZE = 4;
    // The single instance is reused to transfer segment data to the ArenaFactory.
    private final List<DataView> transferList = new ArrayList<>();

    private static DataView makeDataView(int bytes) {
        DataView result = ByteBufferDataView.allocate(bytes);
        result.order(ByteOrder.LITTLE_ENDIAN);
        return result;
    }

    private static void segmentCountValidator(final int segmentCount) throws IOException {
        if (segmentCount > Constants.MAX_SEGMENTS) {
            throw new IOException("too many segments");
        }
    }
    private Function<Integer, DataView> dataViewFactory;
    private final DataView firstWord;
    private final DataView segmentSizeHeader;

    public AllocatedArenaBuilder(Function<Integer, DataView> dataViewFactory) {
        this.dataViewFactory = requireNonNull(dataViewFactory, "Parameter dataViewFactory can not be null.");
        firstWord = dataViewFactory.apply(Constants.BYTES_PER_WORD);
        segmentSizeHeader = dataViewFactory.apply(Constants.MAX_SEGMENTS * INT_SIZE);
    }

    public AllocatedArenaBuilder() {
        this(AllocatedArenaBuilder::makeDataView);
    }

    private SegmentCountValidator segmentCountValidator = AllocatedArenaBuilder::segmentCountValidator;
    private SegmentReader reader = this::createInternalDataViews;
    private Function<List<DataView>, AllocatedArena> arenaFactory = SimpleReaderArena::new;

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

    public Function<List<DataView>, AllocatedArena> getArenaFactory() {
        return arenaFactory;
    }

    public AllocatedArenaBuilder setArenaFactory(Function<List<DataView>, AllocatedArena> arenaFactory) {
        this.arenaFactory = arenaFactory;
        return this;
    }

    public Function<Integer, DataView> getByteBufferFactory() {
        return dataViewFactory;
    }

    public AllocatedArenaBuilder setByteBufferFactory(Function<Integer, DataView> dataViewFactory) {
        this.dataViewFactory = dataViewFactory;
        return this;
    }

    /**
     * Builds an Arena if the input contains valid data.
     *
     * @param input the input data.
     *
     * @return a new arena or null if input was at EOF
     *
     * @throws IOException if reading was impossible or input data invalid.
     */
    public AllocatedArena build(ReadableByteChannel input) throws IOException {
        final List<DataView> segments = reader.read(input);
        if (segments == null) {
            return null;
        }
        final AllocatedArena result = arenaFactory.apply(segments);
        segments.clear();
        return result;
    }

    /**
     * Reads the frame data of one Capnp message into a byteBuffer.
     *
     * @param bc The input data.
     *
     * @return a ByteBuffer containing a complete Frame or null if EOF
     *
     * @throws IOException if reading was impossible or input data invalid.
     */
    public DataView readFrame(ReadableByteChannel bc) throws IOException {
        firstWord.rewindWriterPosition();
        int read = fillDataView(bc, firstWord);
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
        segmentSizeHeader.rewindWriterPosition();
        final int segmentSizeHeaderSize = 4 * (segmentCount & ~1);
        segmentSizeHeader.limit(segmentSizeHeaderSize);
        read = fillDataView(bc, segmentSizeHeader);
        if (read < segmentSizeHeaderSize) {
            throw new IOException("Incomplete data: EOF after " + read + Constants.BYTES_PER_WORD);
        }
        for (int segmentHeaderIndex = 0; segmentHeaderIndex < segmentCount - 1; ++segmentHeaderIndex) {
            totalWords += segmentSizeHeader.getInt(segmentHeaderIndex * 4);
        }

        DataView frame = dataViewFactory.apply(totalWords * Constants.BYTES_PER_WORD + segmentSizeHeaderSize + Constants.BYTES_PER_WORD);
        // write first word
        firstWord.rewindWriterPosition();
        frame.put(firstWord);
        // write additional sizes
        segmentSizeHeader.rewindWriterPosition();
        frame.put(segmentSizeHeader);
        // read remaining stuff or fail
        read = fillDataView(bc, frame);
        if (read < totalWords * Constants.BYTES_PER_WORD) {
            throw new IOException("Incomplete data: EOF after " + read + segmentSizeHeaderSize + Constants.BYTES_PER_WORD);
        }
        frame.rewindReaderPosition();
        return frame;
    }

    private List<DataView> createInternalDataViews(ReadableByteChannel bc) throws IOException {
        firstWord.rewindWriterPosition();
        int read = fillDataView(bc, firstWord);
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
        segmentSizeHeader.rewindWriterPosition();
        final int segmentSizeHeaderSize = 4 * (segmentCount & ~1);
        segmentSizeHeader.limit(segmentSizeHeaderSize);
        read = fillDataView(bc, segmentSizeHeader);
        if (read < segmentSizeHeaderSize) {
            throw new IOException("Incomplete data: EOF after " + read + Constants.BYTES_PER_WORD);
        }
        for (int segmentHeaderIndex = 0; segmentHeaderIndex < segmentCount - 1; ++segmentHeaderIndex) {
            int size = segmentSizeHeader.getInt(segmentHeaderIndex * 4);
            moreSizes.add(size);
            totalWords += size;
        }

        DataView allSegments = dataViewFactory.apply(totalWords * Constants.BYTES_PER_WORD);
        read = fillDataView(bc, allSegments);
        if (read < totalWords * Constants.BYTES_PER_WORD) {
            throw new IOException("Incomplete data: EOF after " + read + segmentSizeHeaderSize + Constants.BYTES_PER_WORD);
        }
        transferList.clear();
        allSegments.rewindReaderPosition();
        final DataView slice = allSegments.slice();
        slice.limit(segment0Size * Constants.BYTES_PER_WORD);
        transferList.add(slice);
        int offset = segment0Size;
        for (int ii = 1; ii < segmentCount; ++ii) {
            allSegments.readerPosition(offset * Constants.BYTES_PER_WORD);
            final DataView slice1 = allSegments.slice();
            slice1.limit(moreSizes.get(ii - 1) * Constants.BYTES_PER_WORD);
            transferList.add(slice1);
            offset += moreSizes.get(ii - 1);
        }
        return transferList;
    }

    /**
     * Creates an AllocatedArena by slicing the given ByteBuffer. The ByteBuffer
     * will be LittleEndian and positioned after the message on return. If the
     * Message is truncated an IllegalArgumentException is thrown.
     *
     * @param bb The
     *
     * @return an AllocatedArena or null if bb was empty.
     *
     * @throws IOException              If the SegmentCountValidator does.
     * @throws IllegalArgumentException in case the message is trunctaed.
     */
    public AllocatedArena build(DataView bb) throws IOException {
        if (bb.remainingReadableBytes() == 0) {
            return null;
        }
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int segmentCount = 1 + bb.getInt();

        this.segmentCountValidator.validate(segmentCount);

        transferList.clear();
        int segmentSizesBase = bb.readerPosition();
        int segmentSizesSize = segmentCount * 4;

        int align = Constants.BYTES_PER_WORD - 1;
        int segmentBase = (segmentSizesBase + segmentSizesSize + align) & ~align;

        int totalWords = 0;

        for (int ii = 0; ii < segmentCount; ++ii) {
            int segmentSize = bb.getInt(segmentSizesBase + ii * 4);

            bb.readerPosition(segmentBase + totalWords * Constants.BYTES_PER_WORD);
            final DataView slice = bb.slice();
            slice.limit(segmentSize * Constants.BYTES_PER_WORD);
            transferList.add(slice);

            totalWords += segmentSize;
        }
        bb.readerPosition(segmentBase + totalWords * Constants.BYTES_PER_WORD);
        final AllocatedArena result = arenaFactory.apply(transferList);
        transferList.clear();
        return result;
    }

    /**
     * Reads as many bytes as possible into the
     *
     * @param source the source
     * @param target the target
     *
     * @return how many bytes were read.
     *
     * @throws IOException from
     *                     {@link ReadableByteChannel#read(java.nio.ByteBuffer)}.
     *
     */
    private int fillDataView(ReadableByteChannel source, DataView target) throws IOException {
        int read = 0;
        while (target.hasRemainingWriteableBytes()) {
            int r = target.readFrom(source);
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

        List<DataView> read(ReadableByteChannel in) throws IOException;
    }
}
