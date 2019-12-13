package org.capnproto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This AllocatedArena ignores the traversal limit, because that is broken for
 * messages > DefaultTraversal limit.
 */
public class SimpleReaderArena implements AllocatedArena {

    private final List<GenericSegmentReader> segments;

    /**
     * Creates an Arena with a SegmentReader for each {@link DataView}.
     *
     * @param segments The segments.
     */
    public SimpleReaderArena(DataView[] segments) {
        this.segments = new ArrayList<>();
        for (int ii = 0; ii < segments.length; ++ii) {
            this.segments.add(new SegmentReader(segments[ii], this));
        }
    }

    /**
     * Creates an Arena with the given number of SegmentReader.
     *
     * @param segments     The segments.
     * @param segmentCount The number of segments to use.
     */
    public SimpleReaderArena(DataView[] segments, int segmentCount) {
        this.segments = new ArrayList<>();
        for (int ii = 0; ii < segmentCount; ++ii) {
            this.segments.add(new SegmentReader(segments[ii], this));
        }
    }

    /**
     * Creates an Arena with a SegmentReader for each {@link DataView}.
     *
     * @param segments The segments.
     */
    public SimpleReaderArena(Collection<DataView> segments) {
        this.segments = new ArrayList<>();
        for (DataView segmentSlice : segments) {
            this.segments.add(new SegmentReader(segmentSlice, this));
        }
    }

    @Override
    public List<GenericSegmentReader> getSegments() {
        return segments;
    }

    @Override
    public GenericSegmentReader tryGetSegment(int id) {
        return segments.get(id);
    }

    @Override
    public void checkReadLimit(int numBytes) {
        // ignore this
    }

}
