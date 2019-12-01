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

public final class StructList {

    public static final class Factory<B extends StructBuilder, R extends StructReader>
            extends ListFactory<Builder<B>, Reader<R>> {

        public final StructFactory<B, R> factory;

        private final static ThreadLocal<org.capnproto.Recycler> READER_RECYCLER = new ThreadLocal<org.capnproto.Recycler>() {
            @Override
            protected org.capnproto.Recycler initialValue() {
                return new org.capnproto.Recycler<>(Reader::new);
            }
        };
        private final static ThreadLocal<org.capnproto.Recycler> BUILDER_RECYCLER = new ThreadLocal<org.capnproto.Recycler>() {
            @Override
            protected org.capnproto.Recycler initialValue() {
                return new org.capnproto.Recycler<>(Builder::new);
            }
        };

        public Factory(StructFactory<B, R> factory) {
            super(ElementSize.INLINE_COMPOSITE);
            this.factory = factory;
        }

        @Override
        public final Reader<R> constructReader(SegmentDataContainer segment,
                int ptr,
                int elementCount, int step,
                int structDataSize, short structPointerCount,
                int nestingLimit) {
            final Reader<R> reader = (Reader<R>) READER_RECYCLER.get().getOrCreate();
            reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
            reader.factory = this.factory;
            return reader;
        }

        @Override
        public final Builder<B> constructBuilder(GenericSegmentBuilder segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount) {
            final Builder<B> builder = (Builder<B>) BUILDER_RECYCLER.get().getOrCreate();
            builder.init(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            builder.factory = this.factory;
            return builder;
        }

        @Override
        public final Builder<B> fromPointerBuilderRefDefault(GenericSegmentBuilder segment, int pointer,
                SegmentDataContainer defaultSegment, int defaultOffset) {
            return WireHelpers.getWritableStructListPointer(this,
                    pointer,
                    segment,
                    factory.structSize(),
                    defaultSegment,
                    defaultOffset);
        }

        @Override
        public final Builder<B> fromPointerBuilder(GenericSegmentBuilder segment, int pointer) {
            return WireHelpers.getWritableStructListPointer(this,
                    pointer,
                    segment,
                    factory.structSize(),
                    null, 0);
        }

        @Override
        public final Builder<B> initFromPointerBuilder(GenericSegmentBuilder segment, int pointer,
                int elementCount) {
            return WireHelpers.initStructListPointer(this, pointer, segment, elementCount, factory.structSize());
        }
    }

    public static final class Reader<T> extends ListReader implements Collection<T>, Recyclable<Reader<T>> {

        public StructReader.Factory<T> factory;
        private Recycler<Reader<T>> recycler;
        private boolean recycled;

        @Override
        protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
            super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
            recycled = false;
        }

        @Override
        public Stream<T> stream() {
            checkRecycled();
            return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                    Spliterator.SIZED & Spliterator.IMMUTABLE
            ), false);
        }

        public T get(int index) {
            checkRecycled();
            return _getStructElement(factory, index);
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
        public boolean add(T e) {
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
        public boolean addAll(Collection<? extends T> c) {
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

        public final class Iterator implements java.util.Iterator<T> {

            public Reader<T> list;
            public int idx = 0;

            public Iterator(Reader<T> list) {
                this.list = list;
            }

            @Override
            public T next() {
                return list._getStructElement(factory, idx++);
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
        public java.util.Iterator<T> iterator() {
            checkRecycled();
            return new Iterator(this);
        }

        @Override
        public String toString() {
            checkRecycled();
            return stream().map(String::valueOf).collect(Collectors.joining(","));
        }

        @Override
        public void init(Recycler<Reader<T>> recycler) {
            this.recycler = recycler;
        }

        @Override
        public void recycle() {
            if (recycled) {
                throw new IllegalArgumentException("This reader is already recycled");
            }
            recycled = true;
            if (recycler != null) {
                recycler.recycle(this);
            }
        }

        private void checkRecycled() throws IllegalStateException {
            if (recycled) {
                throw new IllegalStateException("Reader is recycled.");
            }
        }

    }

    public static final class Builder<T extends StructBuilder> extends ListBuilder implements Collection<T>, Recyclable<Builder<T>> {

        public StructBuilder.Factory<T> factory;
        private boolean recycled;
        private Recycler<Builder<T>> recycler;

        public Builder() {
        }

        public final T get(int index) {
            checkRecycled();
            return _getStructElement(factory, index);
        }

        /**
         * Sets the list element, with the following limitation based on the
         * fact that structs in a struct list are allocated inline: if the
         * source struct is larger than the target struct (as can happen if it
         * was created with a newer version of the schema), then it will be
         * truncated, losing fields.
         * <p>
         * TODO: rework generics, so that we don't need this factory parameter
         *
         * @param <U>     The type of the generic
         * @param factory The factory for creating the generic struct
         * @param index   The index of the list.
         * @param value   The source value.
         */
        public final <U extends StructReader> void setWithCaveats(StructFactory<T, U> factory, int index, U value) {
            checkRecycled();
            this._getStructElement(this.factory, index)._copyContentFrom(value);
        }

        //TODO: rework generics, so that we don't need this factory parameter
        public final <U extends StructReader> Reader<U> asReader(StructFactory<T, U> factory) {
            checkRecycled();
            final Reader reader = new Reader();
            reader.init(this.segment, this.ptr, this.elementCount, this.step,
                    this.structDataSize, this.structPointerCount,
                    java.lang.Integer.MAX_VALUE);
            reader.factory = factory;
            return reader;
        }

        public final class Iterator implements java.util.Iterator<T> {

            public Builder<T> list;
            public int idx = 0;

            public Iterator(Builder<T> list) {
                this.list = list;
            }

            @Override
            public T next() {
                return list._getStructElement(factory, idx++);
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
        public java.util.Iterator<T> iterator() {
            checkRecycled();
            return new Iterator(this);
        }

        @Override
        public Stream<T> stream() {
            checkRecycled();
            return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                    Spliterator.SIZED & Spliterator.IMMUTABLE
            ), false);
        }

        @Override
        public String toString() {
            checkRecycled();
            return stream().map(String::valueOf).collect(Collectors.joining(","));
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
        public boolean add(T e) {
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
        public boolean addAll(Collection<? extends T> c) {
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
        protected void init(GenericSegmentBuilder segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount) {
            super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount);
        }

        @Override
        public void init(Recycler<Builder<T>> recycler) {
            this.recycler = recycler;
        }

        @Override
        public void recycle() {
            if (recycled) {
                throw new IllegalArgumentException("This reader is already recycled");
            }
            recycled = true;
            if (recycler != null) {
                recycler.recycle(this);
            }
        }

        private void checkRecycled() throws IllegalStateException {
            if (recycled) {
                throw new IllegalStateException("Reader is recycled.");
            }
        }

    }
}
