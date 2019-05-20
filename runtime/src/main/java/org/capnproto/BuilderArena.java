// Copyright (c) 2013-2014 Sandstorm Development Group, Inc. and contributors
// Licensed under the MIT License:
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package org.capnproto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public final class BuilderArena implements AllocatingArena {

    public enum AllocationStrategy {
        FIXED_SIZE,
        GROW_HEURISTICALLY
    }

    public static final int SUGGESTED_FIRST_SEGMENT_WORDS = 1_024;
    public static final AllocationStrategy SUGGESTED_ALLOCATION_STRATEGY
            = AllocationStrategy.GROW_HEURISTICALLY;

    public final ArrayList<GenericSegmentBuilder> segments;
    private final Allocator allocator;

    /**
     * Constructs a BuilderArena with a default Allocator, that acts according
     * to the {@link AllocationStrategy}.
     *
     * @param firstSegmentSizeWords The size of the first segment. (allocated on
     * demand)
     * @param allocationStrategy The allocation strategy.
     */
    public BuilderArena(int firstSegmentSizeWords, AllocationStrategy allocationStrategy) {
        this.segments = new ArrayList<>();
        allocator = new DefaultAllocator(allocationStrategy);
        ((DefaultAllocator) allocator).setNextAllocationSizeBytes(firstSegmentSizeWords * Constants.BYTES_PER_WORD);
    }

    /**
     * Constructs a BuilderArena with an Allocator.
     *
     * @param allocator is used to allocate memory for each segment.
     */
    public BuilderArena(Allocator allocator) {
        this.segments = new ArrayList<>();
        this.allocator = allocator;
    }

    /**
     * Constructs a BuilderArena with an immediately allocated first segment of
     * given size. The Allocator is not used for the first segment.
     *
     * @param allocator The allocator for other segments.
     * @param firstSegment The first segment.
     */
    public BuilderArena(Allocator allocator, ByteBuffer firstSegment) {
        this.segments = new ArrayList<>();
        SegmentBuilder newSegment = new SegmentBuilder(
                firstSegment,
                this);
        newSegment.buffer.order(ByteOrder.LITTLE_ENDIAN);
        newSegment.id = 0;
        this.segments.add(newSegment);

        this.allocator = allocator;
    }

    @Override
    public List<GenericSegmentBuilder> getSegments() {
        return segments;
    }

    @Override
    public final GenericSegmentBuilder tryGetSegment(int id) {
        return this.segments.get(id);
    }

    @Override
    public final void checkReadLimit(int numBytes) {
    }

    public static class AllocateResult {

        public final GenericSegmentBuilder segment;

        // offset to the beginning the of allocated memory
        public final int offset;

        public AllocateResult(GenericSegmentBuilder segment, int offset) {
            this.segment = segment;
            this.offset = offset;
        }
    }

    @Override
    public AllocateResult allocate(int amount) {
        int len = this.segments.size();

        // we allocate the first segment in the constructor.
        if (len > 0) {
            int result = this.segments.get(len - 1).allocate(amount);
            if (result != SegmentBuilder.FAILED_ALLOCATION) {
                return new AllocateResult(this.segments.get(len - 1), result);
            }
        }
        SegmentBuilder newSegment = new SegmentBuilder(
                this.allocator.allocateSegment(amount * Constants.BYTES_PER_WORD),
                this);

        newSegment.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
        newSegment.setId(len);
        this.segments.add(newSegment);

        return new AllocateResult(newSegment, newSegment.allocate(amount));
    }

    @Override
    public final ByteBuffer[] getSegmentsForOutput() {
        ByteBuffer[] result = new ByteBuffer[this.segments.size()];
        for (int ii = 0; ii < this.segments.size(); ++ii) {
            GenericSegmentBuilder segment = segments.get(ii);
            result[ii] = segment.getSegmentForOutput();
        }
        return result;
    }
}
