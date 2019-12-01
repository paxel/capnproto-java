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

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class DataList {

    public static final class Factory extends ListFactory<Builder, Reader> {

        Factory() {
            super(ElementSize.POINTER);
        }
        private final static ThreadLocal<org.capnproto.Recycler<Reader>> READER_RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
            @Override
            protected org.capnproto.Recycler<Reader> initialValue() {
                return new org.capnproto.Recycler<>(Reader::new);
            }
        };
        private final static ThreadLocal<org.capnproto.Recycler<Builder>> BUILDER_RECYCLER = new ThreadLocal<org.capnproto.Recycler<Builder>>() {
            @Override
            protected org.capnproto.Recycler initialValue() {
                return new org.capnproto.Recycler<>(Builder::new);
            }
        };

        @Override
        public final Reader constructReader(SegmentDataContainer segment,
                int ptr,
                int elementCount, int step,
                int structDataSize, short structPointerCount,
                int nestingLimit) {
            final Reader reader = READER_RECYCLER.get().getOrCreate();
            reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
            return reader;
        }

        @Override
        public final Builder constructBuilder(GenericSegmentBuilder segment,
                int ptr,
                int elementCount, int step,
                int structDataSize, short structPointerCount) {
            Builder builder = BUILDER_RECYCLER.get().getOrCreate();
            builder.init(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            return builder;
        }
    }
    public static final Factory factory = new Factory();

    public static final class Reader extends ListReader implements Collection<Data.Reader>, Recyclable<Reader> {

        private boolean recycled;
        private Recycler<Reader> recycler;

        public Stream<Data.Reader> stream() {
            checkRecycled();
            return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                    Spliterator.SIZED & Spliterator.IMMUTABLE
            ), false);
        }

        public Data.Reader get(int index) {
            checkRecycled();
            return _getPointerElement(Data.factory, index);
        }

        @Override
        public boolean isEmpty() {
            checkRecycled();
            return elementCount == 0;
        }

        @Override
        public boolean contains(Object o) {
            checkRecycled();
            return stream().anyMatch(o::equals);
        }

        @Override
        public Object[] toArray() {
            checkRecycled();
            return stream().collect(Collectors.toList()).toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            checkRecycled();
            return stream().collect(Collectors.toList()).toArray(a);
        }

        @Override
        public boolean add(Data.Reader e) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            checkRecycled();
            return stream().collect(Collectors.toList()).containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Data.Reader> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public void init(Recycler<Reader> recycler) {
            this.recycler = recycler;
        }

        @Override
        public void recycle() {
            if (recycled) {
                throw new IllegalArgumentException("This reader is already recycled");
            }
            super.deinit();
            if (recycler != null) {
                recycler.recycle(this);
            }
        }

        private void checkRecycled() throws IllegalStateException {
            if (recycled) {
                throw new IllegalStateException("Reader is recycled.");
            }
        }

        @Override
        protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
            super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
            recycled = false;
        }

        public final class Iterator implements java.util.Iterator<Data.Reader> {

            public Reader list;
            public int idx = 0;

            public Iterator(Reader list) {
                this.list = list;
            }

            @Override
            public Data.Reader next() {
                return this.list._getPointerElement(Data.factory, idx++);
            }

            @Override
            public boolean hasNext() {
                return idx < list.size();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public java.util.Iterator<Data.Reader> iterator() {
            checkRecycled();
            return new Iterator(this);
        }

        @Override
        public String toString() {
            checkRecycled();
            return stream().map(String::valueOf).collect(Collectors.joining(","));
        }

    }

    public static final class Builder extends ListBuilder implements Collection<Data.Builder>, Recyclable<Builder> {

        private boolean recycled;
        private Recycler<Builder> recycler;

        public Builder() {
        }

        protected void init(GenericSegmentBuilder segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount) {
            super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            recycled = false;
        }

        @Override
        public void init(Recycler<Builder> recycler) {
            this.recycler = recycler;
        }

        @Override
        public void recycle() {
            if (recycled) {
                throw new IllegalArgumentException("This reader is already recycled");
            }
            super.deinit();
            if (recycler != null) {
                recycler.recycle(this);
            }
        }

        private void checkRecycled() throws IllegalStateException {
            if (recycled) {
                throw new IllegalStateException("Reader is recycled.");
            }
        }

        public final Data.Builder get(int index) {
            checkRecycled();
            return _getPointerElement(Data.factory, index);
        }

        public final void set(int index, Data.Reader value) {
            checkRecycled();
            _setPointerElement(Data.factory, index, value);
        }

        public final Reader asReader() {
            checkRecycled();
            final Reader reader = DataList.Factory.READER_RECYCLER.get().getOrCreate();
            reader.init(this.segment, this.ptr, this.elementCount, this.step,
                    this.structDataSize, this.structPointerCount,
                    java.lang.Integer.MAX_VALUE);
            return reader;
        }

        public final class Iterator implements java.util.Iterator<Data.Builder> {

            public Builder list;
            public int idx = 0;

            public Iterator(Builder list) {
                this.list = list;
            }

            @Override
            public Data.Builder next() {
                return this.list._getPointerElement(Data.factory, idx++);
            }

            @Override
            public boolean hasNext() {
                return this.idx < this.list.size();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public java.util.Iterator<Data.Builder> iterator() {
            checkRecycled();
            return new Iterator(this);
        }

        @Override
        public boolean isEmpty() {
            checkRecycled();
            return elementCount == 0;
        }

        @Override
        public boolean contains(Object o) {
            checkRecycled();
            return stream().anyMatch(o::equals);
        }

        @Override
        public Object[] toArray() {
            checkRecycled();
            return stream().collect(Collectors.toList()).toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            checkRecycled();
            return stream().collect(Collectors.toList()).toArray(a);
        }

        @Override
        public boolean add(Data.Builder e) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            checkRecycled();
            return stream().collect(Collectors.toList()).containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Data.Builder> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public String toString() {
            checkRecycled();
            return stream().map(String::valueOf).collect(Collectors.joining(","));
        }

    }

}
