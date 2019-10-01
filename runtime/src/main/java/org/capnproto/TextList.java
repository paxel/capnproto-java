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

public final class TextList {

    public static final class Factory extends ListFactory<Builder, Reader> {

        Factory() {
            super(ElementSize.POINTER);
        }

        @Override
        public final Reader constructReader(SegmentDataContainer segment,
                int ptr,
                int elementCount, int step,
                int structDataSize, short structPointerCount,
                int nestingLimit) {
            final Reader reader = new Reader();
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

    public static final class Reader extends ListReader implements Collection<String> {

        public Reader() {
        }

        public Text.Reader get(int index) {
            return _getPointerElement(Text.factory, index);
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
        public boolean add(String e) {
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
        public boolean addAll(Collection<? extends String> c) {
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
        public Stream<String> stream() {
            return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                    Spliterator.SIZED & Spliterator.IMMUTABLE), false);
        }

        @Override
        public String toString() {
            return stream().map(String::valueOf).collect(Collectors.joining(", "));
        }

        public final class Iterator implements java.util.Iterator<String> {

            public Reader list;
            public int idx = 0;

            public Iterator(Reader list) {
                this.list = list;
            }

            @Override
            public String next() {
                return this.list._getPointerElement(Text.factory, idx++).toString();
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
        public java.util.Iterator<String> iterator() {
            return new Iterator(this);
        }

    }

    public static final class Builder extends ListBuilder implements Collection<String> {

        public Builder(GenericSegmentBuilder segment, int ptr,
                int elementCount, int step,
                int structDataSize, short structPointerCount) {
            super(segment, ptr, elementCount, step, structDataSize, structPointerCount);
        }

        public final Text.Builder get(int index) {
            return _getPointerElement(Text.factory, index);
        }

        public final void set(int index, Text.Reader value) {
            _setPointerElement(Text.factory, index, value);
        }

        public final Reader asReader() {
            final Reader reader = new Reader();
            reader.init(this.segment, this.ptr, this.elementCount, this.step,
                    this.structDataSize, this.structPointerCount,
                    java.lang.Integer.MAX_VALUE);
            return reader;
        }

        public final class Iterator implements java.util.Iterator<String> {

            public Builder list;
            public int idx = 0;

            public Iterator(Builder list) {
                this.list = list;
            }

            @Override
            public String next() {
                return this.list._getPointerElement(Text.factory, idx++).toString();
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

        public java.util.Iterator<String> iterator() {
            return new Iterator(this);
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
        public boolean add(String e) {
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
        public boolean addAll(Collection<? extends String> c) {
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
        public Stream<String> stream() {
            return StreamSupport.stream(Spliterators.spliterator(this.iterator(), elementCount,
                    Spliterator.SIZED & Spliterator.IMMUTABLE), false);
        }

        @Override
        public String toString() {
            return stream().map(String::valueOf).collect(Collectors.joining(", "));
        }

    }
}
