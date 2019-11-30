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

public class PrimitiveList {

    public static class Void {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.VOID);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<org.capnproto.Void>, Recyclable<Reader> {

            private Recycler<Reader> recycler;
            private boolean recycled;

            public Reader() {
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }

            public org.capnproto.Void get(int index) {
                checkRecycled();
                return org.capnproto.Void.VOID;
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
            public boolean add(org.capnproto.Void e) {
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
            public boolean addAll(Collection<? extends org.capnproto.Void> c) {
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
            public Stream<org.capnproto.Void> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            public final class Iterator implements java.util.Iterator<org.capnproto.Void> {

                public Void.Reader list;
                public int idx = 0;

                public Iterator(Void.Reader list) {
                    this.list = list;
                }

                @Override
                public org.capnproto.Void next() {
                    return get(idx++);
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
            public java.util.Iterator<org.capnproto.Void> iterator() {
                return new Iterator(this);
            }

        }

        public static final class Builder extends ListBuilder implements Collection<org.capnproto.Void> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public final Reader asReader() {
                final Reader reader = Void.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            public org.capnproto.Void get(int index) {
                return org.capnproto.Void.VOID;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(org.capnproto.Void e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends org.capnproto.Void> c) {
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
            public Stream<org.capnproto.Void> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE), false);
            }

            public final class Iterator implements java.util.Iterator<org.capnproto.Void> {

                public Void.Builder list;
                public int idx = 0;

                public Iterator(Void.Builder list) {
                    this.list = list;
                }

                @Override
                public org.capnproto.Void next() {
                    return get(idx++);
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
            public java.util.Iterator<org.capnproto.Void> iterator() {
                return new Iterator(this);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }
        }
    }

    public static class Boolean {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.BIT);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }
        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<java.lang.Boolean>, Recyclable<Reader> {

            private boolean recycled;
            private Recycler<Reader> recycler;

            public Reader() {
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

            public final boolean get(int index) {
                checkRecycled();
                return _getBooleanElement(index);
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
            public boolean add(java.lang.Boolean e) {
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
            public boolean addAll(Collection<? extends java.lang.Boolean> c) {
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
            public Stream<java.lang.Boolean> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }

            public final class Iterator implements java.util.Iterator<java.lang.Boolean> {

                public Reader list;
                public int idx = 0;

                public Iterator(Reader list) {
                    this.list = list;
                }

                @Override
                public java.lang.Boolean next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Boolean> iterator() {
                checkRecycled();
                return new Iterator(this);
            }

        }

        public static final class Builder extends ListBuilder implements Collection<java.lang.Boolean> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public boolean get(int index) {
                return _getBooleanElement(index);
            }

            public void set(int index, boolean value) {
                _setBooleanElement(index, value);
            }

            public final Reader asReader() {
                final Reader reader = Boolean.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(java.lang.Boolean e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Boolean> c) {
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

            public Stream<java.lang.Boolean> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }

            public final class Iterator implements java.util.Iterator<java.lang.Boolean> {

                public Builder list;
                public int idx = 0;

                public Iterator(Builder list) {
                    this.list = list;
                }

                @Override
                public java.lang.Boolean next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Boolean> iterator() {
                return new Iterator(this);
            }

        }
    }

    public static class Byte {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.BYTE);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }
        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<java.lang.Byte>, Recyclable<Reader> {

            private Recycler<Reader> recycler;
            private boolean recycled;

            public byte get(int index) {
                checkRecycled();
                return _getByteElement(index);
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
            public boolean add(java.lang.Byte e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Byte> c) {
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
            public Stream<java.lang.Byte> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            public final class Iterator implements java.util.Iterator<java.lang.Byte> {

                public Reader list;
                public int idx = 0;

                public Iterator(Reader list) {
                    this.list = list;
                }

                @Override
                public java.lang.Byte next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Byte> iterator() {
                checkRecycled();
                return new Iterator(this);
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(","));
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

        }

        public static final class Builder extends ListBuilder implements Collection<java.lang.Byte> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public byte get(int index) {
                return _getByteElement(index);
            }

            public void set(int index, byte value) {
                _setByteElement(index, value);
            }

            public final Reader asReader() {
                final Reader reader = Byte.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(java.lang.Byte e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Byte> c) {
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

            public Stream<java.lang.Byte> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            public final class Iterator implements java.util.Iterator<java.lang.Byte> {

                public Builder list;
                public int idx = 0;

                public Iterator(Builder list) {
                    this.list = list;
                }

                @Override
                public java.lang.Byte next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Byte> iterator() {
                return new Iterator(this);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(","));
            }

        }
    }

    public static class Short {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.TWO_BYTES);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<java.lang.Short>, Recyclable<Reader> {

            private Recycler<Reader> recycler;
            private boolean recycled;

            public short get(int index) {
                checkRecycled();
                return _getShortElement(index);
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
            public boolean add(java.lang.Short e) {
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
            public boolean addAll(Collection<? extends java.lang.Short> c) {
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

            public Stream<java.lang.Short> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            public final class Iterator implements java.util.Iterator<java.lang.Short> {

                public Short.Reader list;
                public int idx = 0;

                public Iterator(Short.Reader list) {
                    this.list = list;
                }

                @Override
                public java.lang.Short next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Short> iterator() {
                checkRecycled();
                return new Iterator(this);
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(","));
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

        }

        public static final class Builder extends ListBuilder implements Collection<java.lang.Short> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public short get(int index) {
                return _getShortElement(index);
            }

            public void set(int index, short value) {
                _setShortElement(index, value);
            }

            public final Reader asReader() {
                final Reader reader = Short.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(java.lang.Short e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Short> c) {
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
            public Stream<java.lang.Short> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            public final class Iterator implements java.util.Iterator<java.lang.Short> {

                public Short.Builder list;
                public int idx = 0;

                public Iterator(Short.Builder list) {
                    this.list = list;
                }

                @Override
                public java.lang.Short next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Short> iterator() {
                return new Iterator(this);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(","));
            }

        }
    }

    public static class Int {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.FOUR_BYTES);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<Integer>, Recyclable<Reader> {

            private boolean recycled;
            private Recycler<Reader> recycler;

            public Reader() {
            }

            public int get(int index) {
                checkRecycled();
                return _getIntElement(index);
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
            public boolean add(Integer e) {
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
            public boolean addAll(Collection<? extends Integer> c) {
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
            public Stream<Integer> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            public final class Iterator implements java.util.Iterator<Integer> {

                public Reader list;
                public int idx = 0;

                public Iterator(Reader list) {
                    this.list = list;
                }

                @Override
                public Integer next() {
                    return get(idx++);
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
            public java.util.Iterator<Integer> iterator() {
                checkRecycled();
                return new Iterator(this);
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(","));
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

        }

        public static final class Builder extends ListBuilder implements Collection<Integer> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public int get(int index) {
                return _getIntElement(index);
            }

            public void set(int index, int value) {
                _setIntElement(index, value);
            }

            public final Reader asReader() {
                final Reader reader = Int.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(Integer e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends Integer> c) {
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
            public Stream<Integer> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            public final class Iterator implements java.util.Iterator<Integer> {

                public Builder list;
                public int idx = 0;

                public Iterator(Builder list) {
                    this.list = list;
                }

                @Override
                public Integer next() {
                    return get(idx++);
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
            public java.util.Iterator<Integer> iterator() {
                return new Iterator(this);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(","));
            }

        }
    }

    public static class Float {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.FOUR_BYTES);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }
        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<java.lang.Float>, Recyclable<Reader> {

            private boolean recycled;
            private Recycler<Reader> recycler;

            public Reader() {
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

            public float get(int index) {
                checkRecycled();
                return _getFloatElement(index);
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
            public boolean add(java.lang.Float e) {
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
            public boolean addAll(Collection<? extends java.lang.Float> c) {
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
            public Stream<java.lang.Float> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }

            public final class Iterator implements java.util.Iterator<java.lang.Float> {

                public Reader list;
                public int idx = 0;

                public Iterator(Reader list) {
                    this.list = list;
                }

                @Override
                public java.lang.Float next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Float> iterator() {
                checkRecycled();
                return new Iterator(this);
            }

        }

        public static final class Builder extends ListBuilder implements Collection<java.lang.Float> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public float get(int index) {
                return _getFloatElement(index);
            }

            public void set(int index, float value) {
                _setFloatElement(index, value);
            }

            public final Reader asReader() {
                final Reader reader = Float.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(java.lang.Float e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Float> c) {
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
            public Stream<java.lang.Float> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }

            public final class Iterator implements java.util.Iterator<java.lang.Float> {

                public Builder list;
                public int idx = 0;

                public Iterator(Builder list) {
                    this.list = list;
                }

                @Override
                public java.lang.Float next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Float> iterator() {
                return new Iterator(this);
            }

        }
    }

    public static class Long {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.EIGHT_BYTES);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }
        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<java.lang.Long>, Recyclable<Reader> {

            private boolean recycled;
            private Recycler<Reader> recycler;

            public long get(int index) {
                checkRecycled();
                return _getLongElement(index);
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
            public boolean add(java.lang.Long e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Long> c) {
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
            public Stream<java.lang.Long> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE), false);
            }

            public final class Iterator implements java.util.Iterator<java.lang.Long> {

                public Reader list;
                public int idx = 0;

                public Iterator(Reader list) {
                    this.list = list;
                }

                @Override
                public java.lang.Long next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Long> iterator() {
                checkRecycled();
                return new Iterator(this);
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(","));
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

        }

        public static final class Builder extends ListBuilder implements Collection<java.lang.Long> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public long get(int index) {
                return _getLongElement(index);
            }

            public void set(int index, long value) {
                _setLongElement(index, value);
            }

            public final Reader asReader() {
                final Reader reader = Long.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(java.lang.Long e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Long> c) {
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
            public Stream<java.lang.Long> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE), false);
            }

            public final class Iterator implements java.util.Iterator<java.lang.Long> {

                public Builder list;
                public int idx = 0;

                public Iterator(Builder list) {
                    this.list = list;
                }

                @Override
                public java.lang.Long next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Long> iterator() {
                return new Iterator(this);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(","));
            }
        }
    }

    public static class Double {

        public static final class Factory extends ListFactory<Builder, Reader> {

            Factory() {
                super(ElementSize.EIGHT_BYTES);
            }
            private final static ThreadLocal<org.capnproto.Recycler<Reader>> RECYCLER = new ThreadLocal<org.capnproto.Recycler<Reader>>() {
                @Override
                protected org.capnproto.Recycler<Reader> initialValue() {
                    return new org.capnproto.Recycler<>(Reader::new);
                }
            };

            @Override
            public final Reader constructReader(SegmentDataContainer segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount,
                    int nestingLimit) {
                final Reader reader = RECYCLER.get().getOrCreate();
                reader.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit);
                return reader;
            }

            @Override
            public final Builder constructBuilder(GenericSegmentBuilder segment,
                    int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                return new Builder(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }
        }
        public static final Factory factory = new Factory();

        public static final class Reader extends ListReader implements Collection<java.lang.Double>, Recyclable<Reader> {

            private boolean recycled;
            private Recycler<Reader> recycler;

            public Reader() {
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

            @Override
            protected void init(SegmentDataContainer segment, int ptr, int elementCount, int step, int structDataSize, short structPointerCount, int nestingLimit) {
                super.init(segment, ptr, elementCount, step, structDataSize, structPointerCount, nestingLimit); //To change body of generated methods, choose Tools | Templates.
                recycled = false;
            }

            public double get(int index) {
                checkRecycled();
                return _getDoubleElement(index);
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
            public boolean add(java.lang.Double e) {
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
            public boolean addAll(Collection<? extends java.lang.Double> c) {
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
            public Stream<java.lang.Double> stream() {
                checkRecycled();
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            @Override
            public String toString() {
                checkRecycled();
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }

            public final class Iterator implements java.util.Iterator<java.lang.Double> {

                public Reader list;
                public int idx = 0;

                public Iterator(Reader list) {
                    this.list = list;
                }

                @Override
                public java.lang.Double next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Double> iterator() {
                checkRecycled();
                return new Iterator(this);
            }

        }

        public static final class Builder extends ListBuilder implements Collection<java.lang.Double> {

            public Builder(GenericSegmentBuilder segment, int ptr,
                    int elementCount, int step,
                    int structDataSize, short structPointerCount) {
                super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
            }

            public double get(int index) {
                return _getDoubleElement(index);
            }

            public void set(int index, double value) {
                _setDoubleElement(index, value);
            }

            public final Reader asReader() {
                final Reader reader = Double.Factory.RECYCLER.get().getOrCreate();
                reader.init(this.segment, this.ptr, this.elementCount, this.step,
                        this.structDataSize, this.structPointerCount,
                        java.lang.Integer.MAX_VALUE);
                return reader;
            }

            @Override
            public boolean isEmpty() {
                return elementCount == 0;
            }

            @Override
            public boolean contains(Object o) {
                return stream().anyMatch(o::equals);
            }

            @Override
            public Object[] toArray() {
                return stream().collect(Collectors.toList()).toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return stream().collect(Collectors.toList()).toArray(a);
            }

            @Override
            public boolean add(java.lang.Double e) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Unsupported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return stream().collect(Collectors.toList()).containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends java.lang.Double> c) {
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
            public Stream<java.lang.Double> stream() {
                return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                        Spliterator.SIZED & Spliterator.IMMUTABLE
                ), false);
            }

            @Override
            public String toString() {
                return stream().map(String::valueOf).collect(Collectors.joining(", "));
            }

            public final class Iterator implements java.util.Iterator<java.lang.Double> {

                public Builder list;
                public int idx = 0;

                public Iterator(Builder list) {
                    this.list = list;
                }

                @Override
                public java.lang.Double next() {
                    return get(idx++);
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
            public java.util.Iterator<java.lang.Double> iterator() {
                return new Iterator(this);
            }

        }
    }
}
