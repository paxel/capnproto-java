package org.capnproto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This AllocatedArena ignores the traversal limit, because that is broken for
 * messages > DefaultTraversal limit.
 */
public class SimpleReaderArena implements AllocatedArena {

    private final List<GenericSegmentReader> segments;

    public SimpleReaderArena(ByteBuffer[] segmentSlices) {
        this.segments = new ArrayList<>();
        for (int ii = 0; ii < segmentSlices.length; ++ii) {
            this.segments.add(new SegmentReader(segmentSlices[ii], this));
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
