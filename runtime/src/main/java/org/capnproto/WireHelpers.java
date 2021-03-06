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

import java.util.function.Consumer;

final class WireHelpers {

    private final static ThreadLocal<org.capnproto.Recycler<FollowFarsResult>> FOLLOW_FARS_RESULT_RECYLCER = new ThreadLocal<org.capnproto.Recycler<FollowFarsResult>>() {
        @Override
        protected org.capnproto.Recycler<FollowFarsResult> initialValue() {
            return new org.capnproto.Recycler<>(FollowFarsResult::new);
        }
    };

    private final static ThreadLocal<org.capnproto.Recycler<AllocateResult>> ALLOCATE_RESULT_RECYLCER = new ThreadLocal<org.capnproto.Recycler<AllocateResult>>() {
        @Override
        protected org.capnproto.Recycler<AllocateResult> initialValue() {
            return new org.capnproto.Recycler<>(AllocateResult::new);
        }
    };

    static int roundBytesUpToWords(int bytes) {
        return (bytes + 7) / 8;
    }

    static int roundBitsUpToBytes(int bits) {
        return (bits + 7) / Constants.BITS_PER_BYTE;
    }

    static int roundBitsUpToWords(long bits) {
        //# This code assumes 64-bit words.
        return (int) ((bits + 63) / ((long) Constants.BITS_PER_WORD));
    }

    static class AllocateResult implements Recyclable<AllocateResult> {

        public AllocateResult() {
        }

        public int ptr;
        public int refOffset;
        public GenericSegmentBuilder segment;

        private Recycler<AllocateResult> recycler;
        private boolean recycled = true;

        @Override
        public void init(Recycler<AllocateResult> recycler) {
            this.recycler = recycler;
        }

        @Override
        public void recycle() {
            if (recycled) {
                throw new IllegalStateException("Already recycled");
            }
            recycled = true;
            ptr = 0;
            refOffset = 0;
            segment = null;
            recycler.recycle(this);
        }

        void init(int ptr, int refOffset, GenericSegmentBuilder segment) {
            if (!recycled) {
                throw new IllegalStateException("Not recycled yet");
            }
            recycled = false;
            this.ptr = ptr;
            this.refOffset = refOffset;
            this.segment = segment;
        }
    }

    /**
     *
     * @param refOffset
     * @param segment
     * @param amount    in words
     * @param kind
     *
     * @return
     */
    static AllocateResult allocate(int refOffset, GenericSegmentBuilder segment, int amount, byte kind) {

        long ref = segment.get(refOffset);
        if (!WirePointer.isNull(ref)) {
            zeroObject(segment, refOffset);
        }

        if (amount == 0 && kind == WirePointer.STRUCT) {
            WirePointer.setKindAndTargetForEmptyStruct(segment.getBuffer(), refOffset);
            AllocateResult result = ALLOCATE_RESULT_RECYLCER.get().getOrCreate();
            result.init(refOffset, refOffset, segment);
            return result;
        }

        int ptr = segment.allocate(amount);
        if (ptr == GenericSegmentBuilder.FAILED_ALLOCATION) {
            //# Need to allocate in a new segment. We'll need to
            //# allocate an extra pointer worth of space to act as
            //# the landing pad for a far pointer.

            int amountPlusRef = amount + Constants.POINTER_SIZE_IN_WORDS;
            BuilderArena.AllocateResult allocation = segment.getArena().allocate(amountPlusRef);

            //# Set up the original pointer to be a far pointer to
            //# the new segment.
            FarPointer.set(segment.getBuffer(), refOffset, false, allocation.offset);
            FarPointer.setSegmentId(segment.getBuffer(), refOffset, allocation.segment.getId());

            //# Initialize the landing pad to indicate that the
            //# data immediately follows the pad.
            int resultRefOffset = allocation.offset;
            int ptr1 = allocation.offset + Constants.POINTER_SIZE_IN_WORDS;

            WirePointer.setKindAndTarget(allocation.segment.getBuffer(), resultRefOffset, kind,
                    ptr1);
            AllocateResult result = ALLOCATE_RESULT_RECYLCER.get().getOrCreate();
            result.init(ptr1, resultRefOffset, allocation.segment);
            return result;

        } else {
            WirePointer.setKindAndTarget(segment.getBuffer(), refOffset, kind, ptr);
            AllocateResult result = ALLOCATE_RESULT_RECYLCER.get().getOrCreate();
            result.init(ptr, refOffset, segment);
            return result;
        }
    }

    static class FollowBuilderFarsResult {

        public final int ptr;
        public final long ref;
        public final GenericSegmentBuilder segment;

        FollowBuilderFarsResult(int ptr, long ref, GenericSegmentBuilder segment) {
            this.ptr = ptr;
            this.ref = ref;
            this.segment = segment;
        }
    }

    static FollowBuilderFarsResult followBuilderFars(long ref, int refTarget,
            GenericSegmentBuilder segment) {
        //# If `ref` is a far pointer, follow it. On return, `ref` will
        //# have been updated to point at a WirePointer that contains
        //# the type information about the target object, and a pointer
        //# to the object contents is returned. The caller must NOT use
        //# `ref->target()` as this may or may not actually return a
        //# valid pointer. `segment` is also updated to point at the
        //# segment which actually contains the object.
        //#
        //# If `ref` is not a far pointer, this simply returns
        //# `refTarget`. Usually, `refTarget` should be the same as
        //# `ref->target()`, but may not be in cases where `ref` is
        //# only a tag.

        if (WirePointer.kind(ref) == WirePointer.FAR) {
            GenericSegmentBuilder resultSegment = segment.getArena().tryGetSegment(FarPointer.getSegmentId(ref));

            int padOffset = FarPointer.positionInSegment(ref);
            long pad = resultSegment.get(padOffset);
            if (!FarPointer.isDoubleFar(ref)) {
                return new FollowBuilderFarsResult(WirePointer.target(padOffset, pad), pad, resultSegment);
            }

            //# Landing pad is another far pointer. It is followed by a
            //# tag describing the pointed-to object.
            int refOffset = padOffset + 1;
            ref = resultSegment.get(refOffset);

            resultSegment = resultSegment.getArena().tryGetSegment(FarPointer.getSegmentId(pad));
            return new FollowBuilderFarsResult(FarPointer.positionInSegment(pad), ref, resultSegment);
        } else {
            return new FollowBuilderFarsResult(refTarget, ref, segment);
        }
    }

    static class FollowFarsResult implements Recyclable<FollowFarsResult> {

        public int ptr;
        public long ref;
        public SegmentDataContainer segment;
        private boolean recycled;
        private Recycler<FollowFarsResult> recycler;

        @Override
        public void init(Recycler<FollowFarsResult> recycler) {
            this.recycler = recycler;
        }

        @Override
        public void recycle() {
            if (recycled) {
                throw new IllegalStateException("Already recycled");
            }
            recycled = true;
            ptr = 0;
            ref = 0;
            segment = null;
            recycler.recycle(this);
        }

        private void init(int ptr, long ref, SegmentDataContainer segment) {
            recycled = false;
            this.ptr = ptr;
            this.ref = ref;
            this.segment = segment;
        }
    }

    static FollowFarsResult followFars(long ref, int refTarget, SegmentDataContainer segment) {
        //# If the segment is null, this is an unchecked message,
        //# so there are no FAR pointers.
        if (segment != null && WirePointer.kind(ref) == WirePointer.FAR) {
            SegmentDataContainer resultSegment = segment.getArena().tryGetSegment(FarPointer.getSegmentId(ref));
            int padOffset = FarPointer.positionInSegment(ref);
            long pad = resultSegment.get(padOffset);

            int padWords = FarPointer.isDoubleFar(ref) ? 2 : 1;
            // TODO read limiting

            if (!FarPointer.isDoubleFar(ref)) {

                FollowFarsResult result = FOLLOW_FARS_RESULT_RECYLCER.get().getOrCreate();
                result.init(WirePointer.target(padOffset, pad), pad, resultSegment);
                return result;
            } else {
                //# Landing pad is another far pointer. It is
                //# followed by a tag describing the pointed-to
                //# object.

                long tag = resultSegment.get(padOffset + 1);
                resultSegment = resultSegment.getArena().tryGetSegment(FarPointer.getSegmentId(pad));
                FollowFarsResult result = FOLLOW_FARS_RESULT_RECYLCER.get().getOrCreate();
                result.init(FarPointer.positionInSegment(pad), tag, resultSegment);
                return result;
            }
        } else {
            FollowFarsResult result = FOLLOW_FARS_RESULT_RECYLCER.get().getOrCreate();
            result.init(refTarget, ref, segment);
            return result;
        }
    }

    static void zeroObject(GenericSegmentBuilder segment, int refOffset) {
        //# Zero out the pointed-to object. Use when the pointer is
        //# about to be overwritten making the target object no longer
        //# reachable.

        //# We shouldn't zero out external data linked into the message.
        if (!segment.isWritable()) {
            return;
        }

        long ref = segment.get(refOffset);

        switch (WirePointer.kind(ref)) {
            case WirePointer.STRUCT:
            case WirePointer.LIST:
                zeroObject(segment, ref, WirePointer.target(refOffset, ref));
                break;
            case WirePointer.FAR: {
                segment = segment.getArena().tryGetSegment(FarPointer.getSegmentId(ref));
                if (segment.isWritable()) { //# Don't zero external data.
                    int padOffset = FarPointer.positionInSegment(ref);
                    long pad = segment.get(padOffset);
                    if (FarPointer.isDoubleFar(ref)) {
                        GenericSegmentBuilder otherSegment = segment.getArena().tryGetSegment(FarPointer.getSegmentId(ref));
                        if (otherSegment.isWritable()) {
                            zeroObject(otherSegment, padOffset + 1, FarPointer.positionInSegment(pad));
                        }
                        segment.getBuffer().putLong(padOffset * 8, 0L);
                        segment.getBuffer().putLong((padOffset + 1) * 8, 0L);

                    } else {
                        zeroObject(segment, padOffset);
                        segment.getBuffer().putLong(padOffset * 8, 0L);
                    }
                }

                break;
            }
            case WirePointer.OTHER: {
                // TODO
            }
        }
    }

    static void zeroObject(GenericSegmentBuilder segment, long tag, int ptr) {
        //# We shouldn't zero out external data linked into the message.
        if (!segment.isWritable()) {
            return;
        }

        switch (WirePointer.kind(tag)) {
            case WirePointer.STRUCT: {
                int pointerSection = ptr + StructPointer.dataSize(tag);
                int count = StructPointer.ptrCount(tag);
                for (int ii = 0; ii < count; ++ii) {
                    zeroObject(segment, pointerSection + ii);
                }
                memClear(segment.getBuffer(), ptr * Constants.BYTES_PER_WORD,
                        StructPointer.wordSize(tag) * Constants.BYTES_PER_WORD);
                break;
            }
            case WirePointer.LIST: {
                switch (ListPointer.elementSize(tag)) {
                    case ElementSize.VOID:
                        break;
                    case ElementSize.BIT:
                    case ElementSize.BYTE:
                    case ElementSize.TWO_BYTES:
                    case ElementSize.FOUR_BYTES:
                    case ElementSize.EIGHT_BYTES: {
                        memClear(segment.getBuffer(), ptr * Constants.BYTES_PER_WORD,
                                roundBitsUpToWords(
                                        ListPointer.elementCount(tag)
                                        * ElementSize.dataBitsPerElement(ListPointer.elementSize(tag))) * Constants.BYTES_PER_WORD);
                        break;
                    }
                    case ElementSize.POINTER: {
                        int count = ListPointer.elementCount(tag);
                        for (int ii = 0; ii < count; ++ii) {
                            zeroObject(segment, ptr + ii);
                        }
                        memClear(segment.getBuffer(), ptr * Constants.BYTES_PER_WORD,
                                count * Constants.BYTES_PER_WORD);
                        break;
                    }
                    case ElementSize.INLINE_COMPOSITE: {
                        long elementTag = segment.get(ptr);
                        if (WirePointer.kind(elementTag) != WirePointer.STRUCT) {
                            throw new CapnProtoException("Don't know how to handle non-STRUCT inline composite.");
                        }
                        int dataSize = StructPointer.dataSize(elementTag);
                        int pointerCount = StructPointer.ptrCount(elementTag);

                        int pos = ptr + Constants.POINTER_SIZE_IN_WORDS;
                        int count = WirePointer.inlineCompositeListElementCount(elementTag);
                        for (int ii = 0; ii < count; ++ii) {
                            pos += dataSize;
                            for (int jj = 0; jj < pointerCount; ++jj) {
                                zeroObject(segment, pos);
                                pos += Constants.POINTER_SIZE_IN_WORDS;
                            }
                        }

                        memClear(segment.getBuffer(), ptr * Constants.BYTES_PER_WORD,
                                (StructPointer.wordSize(elementTag) * count + Constants.POINTER_SIZE_IN_WORDS) * Constants.BYTES_PER_WORD);
                        break;
                    }
                }
                break;
            }
            case WirePointer.FAR:
                throw new CapnProtoException("Unexpected FAR pointer.");
            case WirePointer.OTHER:
                throw new CapnProtoException("Unexpected OTHER pointer.");
        }
    }

    static void zeroPointerAndFars(GenericSegmentBuilder segment, int refOffset) {
        //# Zero out the pointer itself and, if it is a far pointer, zero the landing pad as well,
        //# but do not zero the object body. Used when upgrading.

        long ref = segment.get(refOffset);
        if (WirePointer.kind(ref) == WirePointer.FAR) {
            GenericSegmentBuilder padSegment = segment.getArena().tryGetSegment(FarPointer.getSegmentId(ref));
            if (padSegment.isWritable()) { //# Don't zero external data.
                int padOffset = FarPointer.positionInSegment(ref);
                padSegment.getBuffer().putLong(padOffset * Constants.BYTES_PER_WORD, 0L);
                if (FarPointer.isDoubleFar(ref)) {
                    padSegment.getBuffer().putLong(padOffset * Constants.BYTES_PER_WORD + 1, 0L);
                }
            }
        }
        segment.put(refOffset, 0L);
    }

    static void transferPointer(GenericSegmentBuilder dstSegment, int dstOffset,
            GenericSegmentBuilder srcSegment, int srcOffset) {
        //# Make *dst point to the same object as *src. Both must reside in the same message, but can
        //# be in different segments.
        //#
        //# Caller MUST zero out the source pointer after calling this, to make sure no later code
        //# mistakenly thinks the source location still owns the object.  transferPointer() doesn't do
        //# this zeroing itself because many callers transfer several pointers in a loop then zero out
        //# the whole section.

        long src = srcSegment.get(srcOffset);
        if (WirePointer.isNull(src)) {
            dstSegment.put(dstOffset, 0L);
        } else if (WirePointer.kind(src) == WirePointer.FAR) {
            //# Far pointers are position-independent, so we can just copy.
            dstSegment.put(dstOffset, srcSegment.get(srcOffset));
        } else {
            transferPointer(dstSegment, dstOffset, srcSegment, srcOffset,
                    WirePointer.target(srcOffset, src));
        }
    }

    static void transferPointer(GenericSegmentBuilder dstSegment, int dstOffset,
            GenericSegmentBuilder srcSegment, int srcOffset, int srcTargetOffset) {
        //# Like the other overload, but splits src into a tag and a target. Particularly useful for
        //# OrphanBuilder.

        long src = srcSegment.get(srcOffset);
        long srcTarget = srcSegment.get(srcTargetOffset);

        if (dstSegment == srcSegment) {
            //# Same segment, so create a direct pointer.

            if (WirePointer.kind(src) == WirePointer.STRUCT && StructPointer.wordSize(src) == 0) {
                WirePointer.setKindAndTargetForEmptyStruct(dstSegment.getBuffer(), dstOffset);
            } else {
                WirePointer.setKindAndTarget(dstSegment.getBuffer(), dstOffset,
                        WirePointer.kind(src), srcTargetOffset);
            }
            // We can just copy the upper 32 bits.
            dstSegment.getBuffer().putInt(dstOffset * Constants.BYTES_PER_WORD + 4,
                    srcSegment.getBuffer().getInt(srcOffset * Constants.BYTES_PER_WORD + 4));

        } else {
            //# Need to create a far pointer. Try to allocate it in the same segment as the source,
            //# so that it doesn't need to be a double-far.

            int landingPadOffset = srcSegment.allocate(1);
            if (landingPadOffset == GenericSegmentBuilder.FAILED_ALLOCATION) {
                //# Darn, need a double-far.

                BuilderArena.AllocateResult allocation = srcSegment.getArena().allocate(2);
                GenericSegmentBuilder farSegment = allocation.segment;
                landingPadOffset = allocation.offset;

                FarPointer.set(farSegment.getBuffer(), landingPadOffset, false, srcTargetOffset);
                FarPointer.setSegmentId(farSegment.getBuffer(), landingPadOffset, srcSegment.getId());

                WirePointer.setKindWithZeroOffset(farSegment.getBuffer(), landingPadOffset + 1,
                        WirePointer.kind(src));

                farSegment.getBuffer().putInt((landingPadOffset + 1) * Constants.BYTES_PER_WORD + 4,
                        srcSegment.getBuffer().getInt(srcOffset * Constants.BYTES_PER_WORD + 4));

                FarPointer.set(dstSegment.getBuffer(), dstOffset,
                        true, landingPadOffset);
                FarPointer.setSegmentId(dstSegment.getBuffer(), dstOffset,
                        farSegment.getId());
            } else {
                //# Simple landing pad is just a pointer.
                WirePointer.setKindAndTarget(srcSegment.getBuffer(), landingPadOffset,
                        WirePointer.kind(srcTarget), srcTargetOffset);
                srcSegment.getBuffer().putInt(landingPadOffset * Constants.BYTES_PER_WORD + 4,
                        srcSegment.getBuffer().getInt(srcOffset * Constants.BYTES_PER_WORD + 4));

                FarPointer.set(dstSegment.getBuffer(), dstOffset,
                        false, landingPadOffset);
                FarPointer.setSegmentId(dstSegment.getBuffer(), dstOffset,
                        srcSegment.getId());
            }
        }

    }

    static <T> T initStructPointer(StructBuilder.Factory<T> factory, int refOffset, GenericSegmentBuilder segment, StructSize size) {

        AllocateResult ownedAllocationResult = allocate(refOffset, segment, size.total(), WirePointer.STRUCT);
        final GenericSegmentBuilder allocSegment = ownedAllocationResult.segment;
        final int allocRefOffset = ownedAllocationResult.refOffset;
        final int allocPtr = ownedAllocationResult.ptr;
        ownedAllocationResult.recycle();
        // ownedAllocationResult is invalid after the call to recycle()
        ownedAllocationResult = null;

        StructPointer.setFromStructSize(allocSegment.getBuffer(), allocRefOffset, size);
        final T result = factory.constructBuilder(allocSegment, allocPtr * Constants.BYTES_PER_WORD,
                allocPtr + size.data,
                size.data * 64, size.pointers);
        return result;
    }

    static <T> T getWritableStructPointer(StructBuilder.Factory<T> factory,
            int refOffset,
            GenericSegmentBuilder segment,
            StructSize size,
            SegmentDataContainer defaultSegment,
            int defaultOffset) {
        long ref = segment.get(refOffset);
        int target = WirePointer.target(refOffset, ref);
        if (WirePointer.isNull(ref)) {
            if (defaultSegment == null) {
                return initStructPointer(factory, refOffset, segment, size);
            } else {
                throw new CapnProtoException("unimplemented");
            }
        }
        FollowBuilderFarsResult resolved = followBuilderFars(ref, target, segment);

        short oldDataSize = StructPointer.dataSize(resolved.ref);
        short oldPointerCount = StructPointer.ptrCount(resolved.ref);
        int oldPointerSection = resolved.ptr + oldDataSize;

        if (oldDataSize < size.data || oldPointerCount < size.pointers) {
            //# The space allocated for this struct is too small. Unlike with readers, we can't just
            //# run with it and do bounds checks at access time, because how would we handle writes?
            //# Instead, we have to copy the struct to a new space now.

            short newDataSize = (short) Math.max(oldDataSize, size.data);
            short newPointerCount = (short) Math.max(oldPointerCount, size.pointers);
            int totalSize = newDataSize + newPointerCount * Constants.WORDS_PER_POINTER;

            //# Don't let allocate() zero out the object just yet.
            zeroPointerAndFars(segment, refOffset);

            AllocateResult ownedAllocationResult = allocate(refOffset, segment, totalSize, WirePointer.STRUCT);
            final GenericSegmentBuilder allocSegment = ownedAllocationResult.segment;
            final int allocRefOffset = ownedAllocationResult.refOffset;
            final int allocPtr = ownedAllocationResult.ptr;
            ownedAllocationResult.recycle();
            // ownedAllocationResult is invalid after the call to recycle()
            ownedAllocationResult = null;

            StructPointer.set(allocSegment.getBuffer(), allocRefOffset, newDataSize, newPointerCount);

            //# Copy data section.
            memcpy(allocSegment.getBuffer(), allocPtr * Constants.BYTES_PER_WORD,
                    resolved.segment.getBuffer(), resolved.ptr * Constants.BYTES_PER_WORD,
                    oldDataSize * Constants.BYTES_PER_WORD);

            //# Copy pointer section.
            int newPointerSection = allocPtr + newDataSize;
            for (int ii = 0; ii < oldPointerCount; ++ii) {
                transferPointer(allocSegment, newPointerSection + ii,
                        resolved.segment, oldPointerSection + ii);
            }

            //# Zero out old location.  This has two purposes:
            //# 1) We don't want to leak the original contents of the struct when the message is written
            //#    out as it may contain secrets that the caller intends to remove from the new copy.
            //# 2) Zeros will be deflated by packing, making this dead memory almost-free if it ever
            //#    hits the wire.
            memClear(resolved.segment.getBuffer(), resolved.ptr * Constants.BYTES_PER_WORD,
                    (oldDataSize + oldPointerCount * Constants.WORDS_PER_POINTER) * Constants.BYTES_PER_WORD);

            return factory.constructBuilder(allocSegment, allocPtr * Constants.BYTES_PER_WORD,
                    newPointerSection, newDataSize * Constants.BITS_PER_WORD,
                    newPointerCount);
        } else {
            return factory.constructBuilder(resolved.segment, resolved.ptr * Constants.BYTES_PER_WORD,
                    oldPointerSection, oldDataSize * Constants.BITS_PER_WORD,
                    oldPointerCount);
        }

    }

    static <T> T initListPointer(ListBuilder.Factory<T> factory,
            int refOffset,
            GenericSegmentBuilder segment,
            int elementCount,
            byte elementSize) {
        assert elementSize != ElementSize.INLINE_COMPOSITE : "Should have called initStructListPointer instead";

        int dataSize = ElementSize.dataBitsPerElement(elementSize);
        int pointerCount = ElementSize.pointersPerElement(elementSize);
        int step = dataSize + pointerCount * Constants.BITS_PER_POINTER;
        int wordCount = roundBitsUpToWords((long) elementCount * (long) step);
        AllocateResult ownedAllocationResult = allocate(refOffset, segment, wordCount, WirePointer.LIST);
        final GenericSegmentBuilder allocatedSegment = ownedAllocationResult.segment;

        ListPointer.set(allocatedSegment.getBuffer(), ownedAllocationResult.refOffset, elementSize, elementCount);
        final int allocatedPointer = ownedAllocationResult.ptr;
        ownedAllocationResult.recycle();
        // ownedAllocationResult is invalid after the call to recycle()
        ownedAllocationResult = null;
        return factory.constructBuilder(allocatedSegment,
                allocatedPointer * Constants.BYTES_PER_WORD,
                elementCount, step, dataSize, (short) pointerCount);
    }

    static <T> T initStructListPointer(ListBuilder.Factory<T> factory,
            int refOffset,
            GenericSegmentBuilder segment,
            int elementCount,
            StructSize elementSize) {
        int wordsPerElement = elementSize.total();

        //# Allocate the list, prefixed by a single WirePointer.
        int wordCount = elementCount * wordsPerElement;
        AllocateResult ownedAllocationResult = allocate(refOffset, segment, Constants.POINTER_SIZE_IN_WORDS + wordCount, WirePointer.LIST);
        final GenericSegmentBuilder allocSegment = ownedAllocationResult.segment;
        final int allocPtr = ownedAllocationResult.ptr;
        final int allocRefOffset = ownedAllocationResult.refOffset;
        ownedAllocationResult.recycle();
        // ownedAllocationResult is invalid after the call to recycle()
        ownedAllocationResult = null;

        //# Initialize the pointer.
        ListPointer.setInlineComposite(allocSegment.getBuffer(), allocRefOffset, wordCount);
        WirePointer.setKindAndInlineCompositeListElementCount(allocSegment.getBuffer(), allocPtr,
                WirePointer.STRUCT, elementCount);
        StructPointer.setFromStructSize(allocSegment.getBuffer(), allocPtr, elementSize);

        return factory.constructBuilder(allocSegment,
                (allocPtr + 1) * Constants.BYTES_PER_WORD,
                elementCount, wordsPerElement * Constants.BITS_PER_WORD,
                elementSize.data * Constants.BITS_PER_WORD, elementSize.pointers);
    }

    static <T> T getWritableListPointer(ListBuilder.Factory<T> factory,
            int origRefOffset,
            GenericSegmentBuilder origSegment,
            byte elementSize,
            SegmentDataContainer defaultSegment,
            int defaultOffset) {
        assert elementSize != ElementSize.INLINE_COMPOSITE : "Use getWritableStructListPointer() for struct lists";

        long origRef = origSegment.get(origRefOffset);
        int origRefTarget = WirePointer.target(origRefOffset, origRef);

        if (WirePointer.isNull(origRef)) {
            throw new CapnProtoException("unimplemented");
        }

        //# We must verify that the pointer has the right size. Unlike
        //# in getWritableStructListPointer(), we never need to
        //# "upgrade" the data, because this method is called only for
        //# non-struct lists, and there is no allowed upgrade path *to*
        //# a non-struct list, only *from* them.
        FollowBuilderFarsResult resolved = followBuilderFars(origRef, origRefTarget, origSegment);

        if (WirePointer.kind(resolved.ref) != WirePointer.LIST) {
            throw new DecodeException("Called getList{Field,Element}() but existing pointer is not a list");
        }

        byte oldSize = ListPointer.elementSize(resolved.ref);

        if (oldSize == ElementSize.INLINE_COMPOSITE) {
            //# The existing element size is InlineComposite, which
            //# means that it is at least two words, which makes it
            //# bigger than the expected element size. Since fields can
            //# only grow when upgraded, the existing data must have
            //# been written with a newer version of the protocol. We
            //# therefore never need to upgrade the data in this case,
            //# but we do need to validate that it is a valid upgrade
            //# from what we expected.
            throw new CapnProtoException("unimplemented");
        } else {
            int dataSize = ElementSize.dataBitsPerElement(oldSize);
            int pointerCount = ElementSize.pointersPerElement(oldSize);

            if (dataSize < ElementSize.dataBitsPerElement(elementSize)) {
                throw new DecodeException("Existing list value is incompatible with expected type.");
            }
            if (pointerCount < ElementSize.pointersPerElement(elementSize)) {
                throw new DecodeException("Existing list value is incompatible with expected type.");
            }

            int step = dataSize + pointerCount * Constants.BITS_PER_POINTER;

            return factory.constructBuilder(resolved.segment, resolved.ptr * Constants.BYTES_PER_WORD,
                    ListPointer.elementCount(resolved.ref),
                    step, dataSize, (short) pointerCount);
        }
    }

    static <T> T getWritableStructListPointer(ListBuilder.Factory<T> factory,
            int origRefOffset,
            GenericSegmentBuilder origSegment,
            StructSize elementSize,
            SegmentDataContainer defaultSegment,
            int defaultOffset) {
        long origRef = origSegment.get(origRefOffset);
        int origRefTarget = WirePointer.target(origRefOffset, origRef);

        if (WirePointer.isNull(origRef)) {
            throw new CapnProtoException("unimplemented");
        }

        //# We must verify that the pointer has the right size and potentially upgrade it if not.
        FollowBuilderFarsResult resolved = followBuilderFars(origRef, origRefTarget, origSegment);
        if (WirePointer.kind(resolved.ref) != WirePointer.LIST) {
            throw new DecodeException("Called getList{Field,Element}() but existing pointer is not a list");
        }

        byte oldSize = ListPointer.elementSize(resolved.ref);

        if (oldSize == ElementSize.INLINE_COMPOSITE) {
            //# Existing list is INLINE_COMPOSITE, but we need to verify that the sizes match.
            long oldTag = resolved.segment.get(resolved.ptr);
            int oldPtr = resolved.ptr + Constants.POINTER_SIZE_IN_WORDS;
            if (WirePointer.kind(oldTag) != WirePointer.STRUCT) {
                throw new DecodeException("INLINE_COMPOSITE list with non-STRUCT elements not supported.");
            }
            int oldDataSize = StructPointer.dataSize(oldTag);
            short oldPointerCount = StructPointer.ptrCount(oldTag);
            int oldStep = (oldDataSize + oldPointerCount * Constants.POINTER_SIZE_IN_WORDS);
            int elementCount = WirePointer.inlineCompositeListElementCount(oldTag);

            if (oldDataSize >= elementSize.data && oldPointerCount >= elementSize.pointers) {
                //# Old size is at least as large as we need. Ship it.
                return factory.constructBuilder(resolved.segment, oldPtr * Constants.BYTES_PER_WORD,
                        elementCount,
                        oldStep * Constants.BITS_PER_WORD,
                        oldDataSize * Constants.BITS_PER_WORD, oldPointerCount);
            }

            //# The structs in this list are smaller than expected, probably written using an older
            //# version of the protocol. We need to make a copy and expand them.
            short newDataSize = (short) Math.max(oldDataSize, elementSize.data);
            short newPointerCount = (short) Math.max(oldPointerCount, elementSize.pointers);
            int newStep = newDataSize + newPointerCount * Constants.WORDS_PER_POINTER;
            int totalSize = newStep * elementCount;

            //# Don't let allocate() zero out the object just yet.
            zeroPointerAndFars(origSegment, origRefOffset);

            AllocateResult ownedAllocationResult = allocate(origRefOffset, origSegment,
                    totalSize + Constants.POINTER_SIZE_IN_WORDS,
                    WirePointer.LIST);
            final GenericSegmentBuilder allocatedSegment = ownedAllocationResult.segment;

            ListPointer.setInlineComposite(allocatedSegment.getBuffer(), ownedAllocationResult.refOffset, totalSize);
            final int allocationPtr = ownedAllocationResult.ptr;
            ownedAllocationResult.recycle();
            // ownedAllocationResult is invalid after the call to recycle()
            ownedAllocationResult = null;

            long tag = allocatedSegment.get(allocationPtr);
            WirePointer.setKindAndInlineCompositeListElementCount(allocatedSegment.getBuffer(), allocationPtr,
                    WirePointer.STRUCT, elementCount);
            StructPointer.set(allocatedSegment.getBuffer(), allocationPtr,
                    newDataSize, newPointerCount);
            int newPtr = allocationPtr + Constants.POINTER_SIZE_IN_WORDS;

            int src = oldPtr;
            int dst = newPtr;
            for (int ii = 0; ii < elementCount; ++ii) {
                //# Copy data section.
                memcpy(allocatedSegment.getBuffer(), dst * Constants.BYTES_PER_WORD,
                        resolved.segment.getBuffer(), src * Constants.BYTES_PER_WORD,
                        oldDataSize * Constants.BYTES_PER_WORD);

                //# Copy pointer section.
                int newPointerSection = dst + newDataSize;
                int oldPointerSection = src + oldDataSize;
                for (int jj = 0; jj < oldPointerCount; ++jj) {
                    transferPointer(allocatedSegment, newPointerSection + jj,
                            resolved.segment, oldPointerSection + jj);
                }

                dst += newStep;
                src += oldStep;
            }

            //# Zero out old location. See explanation in getWritableStructPointer().
            //# Make sure to include the tag word.
            memClear(resolved.segment.getBuffer(), resolved.ptr * Constants.BYTES_PER_WORD, (1 + oldStep * elementCount) * Constants.BYTES_PER_WORD);

            return factory.constructBuilder(allocatedSegment, newPtr * Constants.BYTES_PER_WORD,
                    elementCount,
                    newStep * Constants.BITS_PER_WORD,
                    newDataSize * Constants.BITS_PER_WORD,
                    newPointerCount);
        } else {
            //# We're upgrading from a non-struct list.

            int oldDataSize = ElementSize.dataBitsPerElement(oldSize);
            int oldPointerCount = ElementSize.pointersPerElement(oldSize);
            int oldStep = oldDataSize + oldPointerCount * Constants.BITS_PER_POINTER;
            int elementCount = ListPointer.elementCount(origRef);

            if (oldSize == ElementSize.VOID) {
                //# Nothing to copy, just allocate a new list.
                return initStructListPointer(factory, origRefOffset, origSegment,
                        elementCount, elementSize);
            } else {
                //# Upgrading to an inline composite list.

                if (oldSize == ElementSize.BIT) {
                    throw new CapnProtoException("Found bit list where struct list was expected; "
                            + "upgrading boolean lists to struct is no longer supported.");
                }

                short newDataSize = elementSize.data;
                short newPointerCount = elementSize.pointers;

                if (oldSize == ElementSize.POINTER) {
                    newPointerCount = (short) Math.max(newPointerCount, 1);
                } else {
                    //# Old list contains data elements, so we need at least 1 word of data.
                    newDataSize = (short) Math.max(newDataSize, 1);
                }

                int newStep = (newDataSize + newPointerCount * Constants.WORDS_PER_POINTER);
                int totalWords = elementCount * newStep;

                //# Don't let allocate() zero out the object just yet.
                zeroPointerAndFars(origSegment, origRefOffset);

                AllocateResult ownedAllocationResult = allocate(origRefOffset, origSegment,
                        totalWords + Constants.POINTER_SIZE_IN_WORDS,
                        WirePointer.LIST);
                final GenericSegmentBuilder allocationSegment = ownedAllocationResult.segment;
                final int allocationRefOffset = ownedAllocationResult.refOffset;

                ListPointer.setInlineComposite(allocationSegment.getBuffer(), allocationRefOffset, totalWords);
                final int allocationPtr = ownedAllocationResult.ptr;
                ownedAllocationResult.recycle();
                // ownedAllocationResult is invalid after the call to recycle()
                ownedAllocationResult = null;

                long tag = allocationSegment.get(allocationPtr);
                WirePointer.setKindAndInlineCompositeListElementCount(allocationSegment.getBuffer(), allocationPtr,
                        WirePointer.STRUCT, elementCount);
                StructPointer.set(allocationSegment.getBuffer(), allocationPtr,
                        newDataSize, newPointerCount);
                int newPtr = allocationPtr + Constants.POINTER_SIZE_IN_WORDS;

                if (oldSize == ElementSize.POINTER) {
                    int dst = newPtr + newDataSize;
                    int src = resolved.ptr;
                    for (int ii = 0; ii < elementCount; ++ii) {
                        transferPointer(origSegment, dst, resolved.segment, src);
                        dst += newStep / Constants.WORDS_PER_POINTER;
                        src += 1;
                    }
                } else {
                    int dst = newPtr;
                    int srcByteOffset = resolved.ptr * Constants.BYTES_PER_WORD;
                    int oldByteStep = oldDataSize / Constants.BITS_PER_BYTE;
                    for (int ii = 0; ii < elementCount; ++ii) {
                        memcpy(allocationSegment.getBuffer(), dst * Constants.BYTES_PER_WORD,
                                resolved.segment.getBuffer(), srcByteOffset, oldByteStep);
                        srcByteOffset += oldByteStep;
                        dst += newStep;
                    }
                }

                //# Zero out old location. See explanation in getWritableStructPointer().
                memClear(resolved.segment.getBuffer(), resolved.ptr * Constants.BYTES_PER_WORD, roundBitsUpToBytes(oldStep * elementCount));

                return factory.constructBuilder(allocationSegment, newPtr * Constants.BYTES_PER_WORD,
                        elementCount,
                        newStep * Constants.BITS_PER_WORD,
                        newDataSize * Constants.BITS_PER_WORD,
                        newPointerCount);
            }
        }
    }

    // size is in bytes
    static Text.Builder initTextPointer(int refOffset, GenericSegmentBuilder segment, int size) {
        //# The byte list must include a NUL terminator.
        int byteSize = size + 1;

        //# Allocate the space.
        AllocateResult ownedAllocationResult = allocate(refOffset, segment, roundBytesUpToWords(byteSize), WirePointer.LIST);
        final GenericSegmentBuilder allocatedSegment = ownedAllocationResult.segment;

        //# Initialize the pointer.
        ListPointer.set(allocatedSegment.getBuffer(), ownedAllocationResult.refOffset, ElementSize.BYTE, byteSize);
        final int ptr = ownedAllocationResult.ptr;
        ownedAllocationResult.recycle();
        // ownedAllocationResult is invalid after the call to recycle()
        ownedAllocationResult = null;

        return new Text.Builder(allocatedSegment.getBuffer(), ptr * Constants.BYTES_PER_WORD, size);
    }

    static Text.Builder setTextPointer(int refOffset, GenericSegmentBuilder segment, Text.Reader value) {
        Text.Builder builder = initTextPointer(refOffset, segment, value.getSize());

        builder.copy(value);
        return builder;
    }

    static Text.Builder getWritableTextPointer(int refOffset,
            GenericSegmentBuilder segment,
            DataView defaultBuffer,
            int defaultOffset,
            int defaultSize) {
        long ref = segment.get(refOffset);

        if (WirePointer.isNull(ref)) {
            if (defaultBuffer == null) {
                return new Text.Builder();
            } else {
                Text.Builder builder = initTextPointer(refOffset, segment, defaultSize);
                // TODO is there a way to do this with bulk methods?
                for (int i = 0; i < builder.size; ++i) {
                    builder.getBuffer().put(builder.offset + i, defaultBuffer.get(defaultOffset * 8 + i));
                }
                return builder;
            }
        }

        int refTarget = WirePointer.target(refOffset, ref);
        FollowBuilderFarsResult resolved = followBuilderFars(ref, refTarget, segment);

        if (WirePointer.kind(resolved.ref) != WirePointer.LIST) {
            throw new DecodeException("Called getText{Field,Element} but existing pointer is not a list.");
        }
        if (ListPointer.elementSize(resolved.ref) != ElementSize.BYTE) {
            throw new DecodeException(
                    "Called getText{Field,Element} but existing list pointer is not byte-sized.");
        }

        int size = ListPointer.elementCount(resolved.ref);
        if (size == 0
                || resolved.segment.getBuffer().get(resolved.ptr * Constants.BYTES_PER_WORD + size - 1) != 0) {
            throw new DecodeException("Text blob missing NUL terminator.");
        }
        return new Text.Builder(resolved.segment.getBuffer(), resolved.ptr * Constants.BYTES_PER_WORD,
                size - 1);

    }

    // size is in bytes
    static Data.Builder initDataPointer(int refOffset, GenericSegmentBuilder segment, int size) {
        //# Allocate the space.
        AllocateResult ownedAllocationResult = allocate(refOffset, segment, roundBytesUpToWords(size),
                WirePointer.LIST);
        final GenericSegmentBuilder allocationSegment = ownedAllocationResult.segment;
        final int allocationRefOffset = ownedAllocationResult.refOffset;

        //# Initialize the pointer.
        ListPointer.set(allocationSegment.getBuffer(), allocationRefOffset, ElementSize.BYTE, size);
        final int allocationPtr = ownedAllocationResult.ptr;
        ownedAllocationResult.recycle();
        // ownedAllocationResult is invalid after the call to recycle()
        ownedAllocationResult = null;

        return new Data.Builder(allocationSegment.getBuffer(), allocationPtr * Constants.BYTES_PER_WORD, size);
    }

    static Data.Builder setDataPointer(int refOffset, GenericSegmentBuilder segment, Data.Reader src) {
        Data.Builder builder = initDataPointer(refOffset, segment, src.getSize());
        src.writeData(src.getOffset(), src.getSize(), builder.getDataView(), builder.getOffset());
        return builder;
    }

    static Data.Builder getWritableDataPointer(int refOffset,
            GenericSegmentBuilder segment,
            DataView defaultBuffer,
            int defaultOffset,
            int defaultSize) {
        long ref = segment.get(refOffset);

        if (WirePointer.isNull(ref)) {
            if (defaultBuffer == null) {
                return new Data.Builder();
            } else {
                Data.Builder builder = initDataPointer(refOffset, segment, defaultSize);
                // TODO is there a way to do this with bulk methods?
                for (int i = 0; i < builder.getSize(); ++i) {
                    builder.getBuffer().put(builder.getOffset() + i, defaultBuffer.get(defaultOffset * 8 + i));
                }
                return builder;
            }
        }

        int refTarget = WirePointer.target(refOffset, ref);
        FollowBuilderFarsResult resolved = followBuilderFars(ref, refTarget, segment);
        final long ref1 = resolved.ref;
        final int ptr = resolved.ptr;
        final GenericSegmentBuilder segment1 = resolved.segment;

        if (WirePointer.kind(ref1) != WirePointer.LIST) {
            throw new DecodeException("Called getData{Field,Element} but existing pointer is not a list.");
        }
        if (ListPointer.elementSize(ref1) != ElementSize.BYTE) {
            throw new DecodeException("Called getData{Field,Element} but existing list pointer is not byte-sized.");
        }

        return new Data.Builder(segment1.getBuffer(), ptr * Constants.BYTES_PER_WORD, ListPointer.elementCount(ref1));
    }

    static <T> T readStructPointer(StructReader.Factory<T> factory,
            SegmentDataContainer segment,
            int refOffset,
            SegmentDataContainer defaultSegment,
            int defaultOffset,
            int nestingLimit) {
        long ref = segment.get(refOffset);
        if (WirePointer.isNull(ref)) {
            if (defaultSegment == null) {
                return factory.constructReader(GenericSegmentReader.EMPTY, 0, 0, 0, (short) 0, 0x7fff_ffff);
            } else {
                segment = defaultSegment;
                refOffset = defaultOffset;
                ref = segment.get(refOffset);
            }
        }

        if (nestingLimit <= 0) {
            throw new DecodeException("Message is too deeply nested or contains cycles.");
        }

        int refTarget = WirePointer.target(refOffset, ref);
        // use a Threadlocal recycler for the far results to safely avoid generating billions of instances
        FollowFarsResult ownedFollowFarsResult = followFars(ref, refTarget, segment);
        final long farRef = ownedFollowFarsResult.ref;
        final SegmentDataContainer farSegment = ownedFollowFarsResult.segment;
        final int farPtr = ownedFollowFarsResult.ptr;
        ownedFollowFarsResult.recycle();
        ownedFollowFarsResult = null;

        int dataSizeWords = StructPointer.dataSize(farRef);

        if (WirePointer.kind(farRef) != WirePointer.STRUCT) {
            throw new DecodeException("Message contains non-struct pointer where struct pointer was expected.");
        }

        farSegment.getArena().checkReadLimit(StructPointer.wordSize(farRef));

        return factory.constructReader(farSegment,
                farPtr * Constants.BYTES_PER_WORD,
                (farPtr + dataSizeWords),
                dataSizeWords * Constants.BITS_PER_WORD,
                StructPointer.ptrCount(farRef),
                nestingLimit - 1);

    }

    static GenericSegmentBuilder setStructPointer(GenericSegmentBuilder segment, int refOffset, StructReader value) {
        short dataSize = (short) roundBitsUpToWords(value.dataSize);
        int totalSize = dataSize + value.pointerCount * Constants.POINTER_SIZE_IN_WORDS;

        AllocateResult ownedAllocationResult = allocate(refOffset, segment, totalSize, WirePointer.STRUCT);
        final GenericSegmentBuilder allocationSegment = ownedAllocationResult.segment;
        final int allocationRefOffset = ownedAllocationResult.refOffset;
        StructPointer.set(allocationSegment.getBuffer(), allocationRefOffset, dataSize, value.pointerCount);
        final int ptr = ownedAllocationResult.ptr;
        ownedAllocationResult.recycle();
        // ownedAllocationResult is invalid after the call to recycle()
        ownedAllocationResult = null;

        if (value.dataSize == 1) {
            throw new CapnProtoException("single bit case not handled");
        } else {
            memcpy(allocationSegment.getBuffer(), ptr * Constants.BYTES_PER_WORD,
                    value.segment.getBuffer(), value.data, value.dataSize / Constants.BITS_PER_BYTE);
        }

        int pointerSection = ptr + dataSize;
        for (int i = 0; i < value.pointerCount; ++i) {
            copyPointer(allocationSegment, pointerSection + i, value.segment, value.pointers + i, value.nestingLimit);
        }
        return allocationSegment;
    }

    static GenericSegmentBuilder setListPointer(GenericSegmentBuilder segment, int refOffset, ListReader value) {
        int totalSize = roundBitsUpToWords(value.elementCount * value.step);

        if (value.step <= Constants.BITS_PER_WORD) {
            //# List of non-structs.
            AllocateResult ownedAllocationResult = allocate(refOffset, segment, totalSize, WirePointer.LIST);
            final GenericSegmentBuilder allocationSegment = ownedAllocationResult.segment;
            final int allocationRefOffset = ownedAllocationResult.refOffset;
            final int ptr = ownedAllocationResult.ptr;
            ownedAllocationResult.recycle();
            // ownedAllocationResult is invalid after the call to recycle()
            ownedAllocationResult = null;

            if (value.structPointerCount == 1) {
                //# List of pointers.
                ListPointer.set(allocationSegment.getBuffer(), allocationRefOffset, ElementSize.POINTER, value.elementCount);
                for (int i = 0; i < value.elementCount; ++i) {
                    copyPointer(allocationSegment, ptr + i, value.segment, value.ptr / Constants.BYTES_PER_WORD + i, value.nestingLimit);
                }
            } else {
                //# List of data.
                byte elementSize = ElementSize.VOID;
                switch (value.step) {
                    case 0:
                        elementSize = ElementSize.VOID;
                        break;
                    case 1:
                        elementSize = ElementSize.BIT;
                        break;
                    case 8:
                        elementSize = ElementSize.BYTE;
                        break;
                    case 16:
                        elementSize = ElementSize.TWO_BYTES;
                        break;
                    case 32:
                        elementSize = ElementSize.FOUR_BYTES;
                        break;
                    case 64:
                        elementSize = ElementSize.EIGHT_BYTES;
                        break;
                    default:
                        throw new CapnProtoException("invalid list step size: " + value.step);
                }

                ListPointer.set(allocationSegment.getBuffer(), allocationRefOffset, elementSize, value.elementCount);
                memcpy(allocationSegment.getBuffer(), ptr * Constants.BYTES_PER_WORD,
                        value.segment.getBuffer(), value.ptr, totalSize * Constants.BYTES_PER_WORD);
            }
            return allocationSegment;
        } else {
            //# List of structs.
            AllocateResult ownedAllocationResult = allocate(refOffset, segment, totalSize + Constants.POINTER_SIZE_IN_WORDS, WirePointer.LIST);
            final GenericSegmentBuilder allocationSegment = ownedAllocationResult.segment;
            final int ptr = ownedAllocationResult.ptr;
            final int allocationRefOffset = ownedAllocationResult.refOffset;
            ownedAllocationResult.recycle();
            // ownedAllocationResult is invalid after the call to recycle()
            ownedAllocationResult = null;

            ListPointer.setInlineComposite(allocationSegment.getBuffer(), allocationRefOffset, totalSize);
            short dataSize = (short) roundBitsUpToWords(value.structDataSize);
            short pointerCount = value.structPointerCount;

            WirePointer.setKindAndInlineCompositeListElementCount(allocationSegment.getBuffer(), ptr,
                    WirePointer.STRUCT, value.elementCount);
            StructPointer.set(allocationSegment.getBuffer(), ptr,
                    dataSize, pointerCount);

            int dstOffset = ptr + Constants.POINTER_SIZE_IN_WORDS;
            int srcOffset = value.ptr / Constants.BYTES_PER_WORD;

            for (int i = 0; i < value.elementCount; ++i) {
                memcpy(allocationSegment.getBuffer(), dstOffset * Constants.BYTES_PER_WORD,
                        value.segment.getBuffer(), srcOffset * Constants.BYTES_PER_WORD,
                        value.structDataSize / Constants.BITS_PER_BYTE);
                dstOffset += dataSize;
                srcOffset += dataSize;

                for (int j = 0; j < pointerCount; ++j) {
                    copyPointer(allocationSegment, dstOffset, value.segment, srcOffset, value.nestingLimit);
                    dstOffset += Constants.POINTER_SIZE_IN_WORDS;
                    srcOffset += Constants.POINTER_SIZE_IN_WORDS;
                }
            }
            return allocationSegment;
        }
    }

    static void memClear(DataView dstBuffer, int dstByteOffset, int length) {

        dstBuffer.zero(dstByteOffset, length);
    }

    static void memcpy(DataView dstBuffer, int dstByteOffset, DataView srcBuffer, int srcByteOffset, int srcLength) {
        srcBuffer.writeTo(dstBuffer, dstByteOffset, srcByteOffset, srcLength);
    }

    static GenericSegmentBuilder copyPointer(GenericSegmentBuilder dstSegment, int dstOffset,
            SegmentDataContainer srcSegment, int srcOffset, int nestingLimit) {
        // Deep-copy the object pointed to by src into dst.  It turns out we can't reuse
        // readStructPointer(), etc. because they do type checking whereas here we want to accept any
        // valid pointer.

        long srcRef = srcSegment.get(srcOffset);

        if (WirePointer.isNull(srcRef)) {
            dstSegment.getBuffer().putLong(dstOffset * 8, 0L);
            return dstSegment;
        }

        int srcTarget = WirePointer.target(srcOffset, srcRef);

        // use a Threadlocal recycler for the far results to safely avoid generating billions of instances
        FollowFarsResult ownedFollowFarsResult = followFars(srcRef, srcTarget, srcSegment);
        final long farRef = ownedFollowFarsResult.ref;
        final SegmentDataContainer farSegment = ownedFollowFarsResult.segment;
        final int farPtr = ownedFollowFarsResult.ptr;
        ownedFollowFarsResult.recycle();
        ownedFollowFarsResult = null;

        switch (WirePointer.kind(farRef)) {
            case WirePointer.STRUCT:
                if (nestingLimit <= 0) {
                    throw new DecodeException("Message is too deeply nested or contains cycles. See org.capnproto.ReaderOptions.");
                }
                farSegment.getArena().checkReadLimit(StructPointer.wordSize(farRef));
                return setStructPointer(dstSegment, dstOffset,
                        new StructReader(farSegment,
                                farPtr * Constants.BYTES_PER_WORD,
                                farPtr + StructPointer.dataSize(farRef),
                                StructPointer.dataSize(farRef) * Constants.BITS_PER_WORD,
                                StructPointer.ptrCount(farRef),
                                nestingLimit - 1));
            case WirePointer.LIST:
                byte elementSize = ListPointer.elementSize(farRef);
                if (nestingLimit <= 0) {
                    throw new DecodeException("Message is too deeply nested or contains cycles. See org.capnproto.ReaderOptions.");
                }
                if (elementSize == ElementSize.INLINE_COMPOSITE) {
                    int wordCount = ListPointer.inlineCompositeWordCount(farRef);
                    long tag = farSegment.get(farPtr);
                    int ptr = farPtr + 1;

                    farSegment.getArena().checkReadLimit(wordCount + 1);

                    if (WirePointer.kind(tag) != WirePointer.STRUCT) {
                        throw new DecodeException("INLINE_COMPOSITE lists of non-STRUCT type are not supported.");
                    }

                    int elementCount = WirePointer.inlineCompositeListElementCount(tag);
                    int wordsPerElement = StructPointer.wordSize(tag);
                    if ((long) wordsPerElement * elementCount > wordCount) {
                        throw new DecodeException("INLINE_COMPOSITE list's elements overrun its word count.");
                    }

                    if (wordsPerElement == 0) {
                        // Watch out for lists of zero-sized structs, which can claim to be arbitrarily
                        // large without having sent actual data.
                        farSegment.getArena().checkReadLimit(elementCount);
                    }

                    ListReader listReader = new ListReader();
                    listReader.init(farSegment,
                            ptr * Constants.BYTES_PER_WORD,
                            elementCount,
                            wordsPerElement * Constants.BITS_PER_WORD,
                            StructPointer.dataSize(tag) * Constants.BITS_PER_WORD,
                            StructPointer.ptrCount(tag),
                            nestingLimit - 1);
                    return setListPointer(dstSegment, dstOffset, listReader);
                } else {
                    int dataSize = ElementSize.dataBitsPerElement(elementSize);
                    short pointerCount = ElementSize.pointersPerElement(elementSize);
                    int step = dataSize + pointerCount * Constants.BITS_PER_POINTER;
                    int elementCount = ListPointer.elementCount(farRef);
                    int wordCount = roundBitsUpToWords((long) elementCount * step);

                    farSegment.getArena().checkReadLimit(wordCount);

                    if (elementSize == ElementSize.VOID) {
                        // Watch out for lists of void, which can claim to be arbitrarily large without
                        // having sent actual data.
                        farSegment.getArena().checkReadLimit(elementCount);
                    }

                    ListReader listReader = new ListReader();
                    listReader.init(farSegment,
                            farPtr * Constants.BYTES_PER_WORD,
                            elementCount,
                            step,
                            dataSize,
                            pointerCount,
                            nestingLimit - 1);
                    return setListPointer(dstSegment, dstOffset, listReader);
                }

            case WirePointer.FAR:
                throw new DecodeException("Unexpected FAR pointer.");
            case WirePointer.OTHER:
                throw new CapnProtoException("copyPointer is unimplemented for OTHER pointers");
        }
        throw new CapnProtoException("unreachable");
    }

    static <T> T readListPointer(ListReader.Factory<T> factory,
            SegmentDataContainer segment,
            int refOffset,
            SegmentDataContainer defaultSegment,
            int defaultOffset,
            byte expectedElementSize,
            int nestingLimit) {

        long ref = segment.get(refOffset);

        if (WirePointer.isNull(ref)) {
            if (defaultSegment == null) {
                return factory.constructReader(GenericSegmentReader.EMPTY, 0, 0, 0, 0, (short) 0, 0x7fff_ffff);
            } else {
                segment = defaultSegment;
                refOffset = defaultOffset;
                ref = segment.get(refOffset);
            }
        }

        if (nestingLimit <= 0) {
            throw new CapnProtoException("nesting limit exceeded");
        }

        int refTarget = WirePointer.target(refOffset, ref);

        // use a Threadlocal recycler for the far results to safely avoid generating billions of instances
        FollowFarsResult ownedFollowFarsResult = followFars(ref, refTarget, segment);
        final long farRef = ownedFollowFarsResult.ref;
        final SegmentDataContainer farSegment = ownedFollowFarsResult.segment;
        final int farPtr = ownedFollowFarsResult.ptr;
        ownedFollowFarsResult.recycle();
        ownedFollowFarsResult = null;

        byte elementSize = ListPointer.elementSize(farRef);
        switch (elementSize) {
            case ElementSize.INLINE_COMPOSITE: {
                int wordCount = ListPointer.inlineCompositeWordCount(farRef);

                long tag = farSegment.get(farPtr);
                int ptr = farPtr + 1;

                farSegment.getArena().checkReadLimit(wordCount + 1);

                int size = WirePointer.inlineCompositeListElementCount(tag);

                int wordsPerElement = StructPointer.wordSize(tag);

                if ((long) size * wordsPerElement > wordCount) {
                    throw new DecodeException("INLINE_COMPOSITE list's elements overrun its word count.");
                }

                if (wordsPerElement == 0) {
                    // Watch out for lists of zero-sized structs, which can claim to be arbitrarily
                    // large without having sent actual data.
                    farSegment.getArena().checkReadLimit(size);
                }

                // TODO check whether the size is compatible
                return factory.constructReader(farSegment,
                        ptr * Constants.BYTES_PER_WORD,
                        size,
                        wordsPerElement * Constants.BITS_PER_WORD,
                        StructPointer.dataSize(tag) * Constants.BITS_PER_WORD,
                        StructPointer.ptrCount(tag),
                        nestingLimit - 1);
            }
            default: {
                //# This is a primitive or pointer list, but all such
                //# lists can also be interpreted as struct lists. We
                //# need to compute the data size and pointer count for
                //# such structs.
                int dataSize = ElementSize.dataBitsPerElement(ListPointer.elementSize(farRef));
                int pointerCount = ElementSize.pointersPerElement(ListPointer.elementSize(farRef));
                int elementCount = ListPointer.elementCount(farRef);
                int step = dataSize + pointerCount * Constants.BITS_PER_POINTER;

                farSegment.getArena().checkReadLimit(
                        roundBitsUpToWords(elementCount * step));

                if (elementSize == ElementSize.VOID) {
                    // Watch out for lists of void, which can claim to be arbitrarily large without
                    // having sent actual data.
                    farSegment.getArena().checkReadLimit(elementCount);
                }

                //# Verify that the elements are at least as large as
                //# the expected type. Note that if we expected
                //# InlineComposite, the expected sizes here will be
                //# zero, because bounds checking will be performed at
                //# field access time. So this check here is for the
                //# case where we expected a list of some primitive or
                //# pointer type.
                int expectedDataBitsPerElement = ElementSize.dataBitsPerElement(expectedElementSize);
                int expectedPointersPerElement = ElementSize.pointersPerElement(expectedElementSize);

                if (expectedDataBitsPerElement > dataSize) {
                    throw new DecodeException("Message contains list with incompatible element type.");
                }

                if (expectedPointersPerElement > pointerCount) {
                    throw new DecodeException("Message contains list with incompatible element type.");
                }
                final T result = factory.constructReader(farSegment,
                        farPtr * Constants.BYTES_PER_WORD,
                        ListPointer.elementCount(farRef),
                        step,
                        dataSize,
                        (short) pointerCount,
                        nestingLimit - 1);

                return result;
            }
        }
    }

    static Text.Reader readTextPointer(SegmentDataContainer segment,
            int refOffset,
            DataView defaultBuffer,
            int defaultOffset,
            int defaultSize) {
        long ref = segment.get(refOffset);

        if (WirePointer.isNull(ref)) {
            if (defaultBuffer == null) {
                return new Text.Reader();
            } else {
                return new Text.Reader(defaultBuffer, defaultOffset, defaultSize);
            }
        }

        int refTarget = WirePointer.target(refOffset, ref);

        // use a Threadlocal recycler for the far results to safely avoid generating billions of instances
        FollowFarsResult ownedFollowFarsResult = followFars(ref, refTarget, segment);
        final long farRef = ownedFollowFarsResult.ref;
        final SegmentDataContainer farSegment = ownedFollowFarsResult.segment;
        final int farPtr = ownedFollowFarsResult.ptr;
        ownedFollowFarsResult.recycle();
        ownedFollowFarsResult = null;

        int size = ListPointer.elementCount(farRef);

        if (WirePointer.kind(farRef) != WirePointer.LIST) {
            throw new DecodeException("Message contains non-list pointer where text was expected.");
        }

        if (ListPointer.elementSize(farRef) != ElementSize.BYTE) {
            throw new DecodeException("Message contains list pointer of non-bytes where text was expected.");
        }

        farSegment.getArena().checkReadLimit(roundBytesUpToWords(size));

        if (size == 0 || farSegment.getBuffer().get(8 * farPtr + size - 1) != 0) {
            throw new DecodeException("Message contains text that is not NUL-terminated.");
        }
        final Text.Reader reader = new Text.Reader(farSegment.getBuffer(), farPtr, size - 1);
        return reader;
    }

    static Data.Reader readDataPointer(SegmentDataContainer segment, int refOffset, Recycler<Data.Reader> recycler, Consumer<Data.Reader> fallBackInit) {
        long ref = segment.get(refOffset);

        if (WirePointer.isNull(ref)) {
            Data.Reader fallBack = recycler.getOrCreate();
            fallBackInit.accept(fallBack);
            return fallBack;
        }

        FollowFarsResult ownedFollowFarsResult = followFars(ref, WirePointer.target(refOffset, ref), segment);
        final Data.Reader result = readDataPointer(ownedFollowFarsResult, recycler);
        ownedFollowFarsResult.recycle();
        return result;
    }

    private static Data.Reader readDataPointer(FollowFarsResult borrowedFollowFarsResult, Recycler<Data.Reader> recycler) throws DecodeException {

        int size = ListPointer.elementCount(borrowedFollowFarsResult.ref);

        if (WirePointer.kind(borrowedFollowFarsResult.ref) != WirePointer.LIST) {
            throw new DecodeException("Message contains non-list pointer where data was expected.");
        }

        if (ListPointer.elementSize(borrowedFollowFarsResult.ref) != ElementSize.BYTE) {
            throw new DecodeException("Message contains list pointer of non-bytes where data was expected.");
        }

        borrowedFollowFarsResult.segment.getArena().checkReadLimit(roundBytesUpToWords(size));

        Data.Reader dataReader = recycler.getOrCreate();
        dataReader.init(borrowedFollowFarsResult.segment.getBuffer(), borrowedFollowFarsResult.ptr, size);
        return dataReader;
    }

}
