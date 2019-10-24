package org.capnproto;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class LayoutSuiteJavaTest {

    private static class BareStructReader implements StructReader.Factory<StructReader> {

        @Override
        public StructReader constructReader(org.capnproto.SegmentDataContainer segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) {
            return new StructReader(segment, data, pointers, dataSize, pointerCount, nestingLimit);
        }
    }

    private static class BareStructBuilder implements StructBuilder.Factory<StructBuilder> {

        private StructSize structSize;

        @Override
        public StructBuilder constructBuilder(org.capnproto.GenericSegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) {
            return new StructBuilder(segment, data, pointers, dataSize, pointerCount);
        }

        public BareStructBuilder(StructSize structSize) {
            this.structSize = structSize;
        }

        @Override
        public StructSize structSize() {
            return structSize;
        }
    }

    @Test
    public void testSimpleRawDataStruct() {
        byte[] data = new byte[]{0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab,
            (byte) 0xcd, (byte) 0xef};

        ByteBufferDataView buffer = ByteBufferDataView.wrap(data);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        ReaderArena arena = new ReaderArena(new DataView[]{buffer}, 0x7fffffffffffffffL);

        StructReader reader = WireHelpers.readStructPointer(new BareStructReader(), arena.tryGetSegment(0), 0, null, 0, 0x7fffffff);

        assertThat(reader._getLongField(0), is(0xefcdab8967452301L));
        assertThat(reader._getLongField(1), is(0L));

        assertThat(reader._getIntField(0), is(0x67452301));
        assertThat(reader._getIntField(1), is(0xefcdab89));
        assertThat(reader._getIntField(2), is(0));
        assertThat(reader._getShortField(0), is((short) 0x2301));
        assertThat(reader._getShortField(1), is((short) 0x6745));
        assertThat(reader._getShortField(2), is((short) 0xab89));
        assertThat(reader._getShortField(3), is((short) 0xefcd));
        assertThat(reader._getShortField(4), is((short) 0));

        // TODO masking
        assertThat(reader._getBooleanField(0), is(true));
        assertThat(reader._getBooleanField(1), is(false));
        assertThat(reader._getBooleanField(2), is(false));

        assertThat(reader._getBooleanField(3), is(false));
        assertThat(reader._getBooleanField(4), is(false));
        assertThat(reader._getBooleanField(5), is(false));
        assertThat(reader._getBooleanField(6), is(false));
        assertThat(reader._getBooleanField(7), is(false));

        assertThat(reader._getBooleanField(8), is(true));
        assertThat(reader._getBooleanField(9), is(true));
        assertThat(reader._getBooleanField(10), is(false));
        assertThat(reader._getBooleanField(11), is(false));
        assertThat(reader._getBooleanField(12), is(false));
        assertThat(reader._getBooleanField(13), is(true));
        assertThat(reader._getBooleanField(14), is(false));
        assertThat(reader._getBooleanField(15), is(false));

        assertThat(reader._getBooleanField(63), is(true));
        assertThat(reader._getBooleanField(64), is(false));

        // TODO masking
    }

    public void setupStruct(StructBuilder builder) {
        builder._setLongField(0, 0x1011121314151617L);
        builder._setIntField(2, 0x20212223);
        builder._setShortField(6, (short) 0x3031);
        builder._setByteField(14, (byte) 0x40);
        builder._setBooleanField(120, false);
        builder._setBooleanField(121, false);
        builder._setBooleanField(122, true);
        builder._setBooleanField(123, false);
        builder._setBooleanField(124, true);
        builder._setBooleanField(125, true);
        builder._setBooleanField(126, true);
        builder._setBooleanField(127, false);
    }

    public void checkStruct(StructBuilder builder) {
        assertThat(0x1011121314151617L, is(builder._getLongField(0)));
        assertThat(0x20212223, is(builder._getIntField(2)));
        assertThat((short)0x3031, is(builder._getShortField(6)));
        assertThat((byte)0x40, is(builder._getByteField(14)));
        assertThat(false, is(builder._getBooleanField(120)));
        assertThat(false, is(builder._getBooleanField(121)));
        assertThat(true, is(builder._getBooleanField(122)));
        assertThat(false, is(builder._getBooleanField(123)));
        assertThat(true, is(builder._getBooleanField(124)));
        assertThat(true, is(builder._getBooleanField(125)));
        assertThat(true, is(builder._getBooleanField(126)));
        assertThat(false, is(builder._getBooleanField(127)));
    }

    @Test
    public void testStructRoundTrip_OneSegment() {
        ByteBufferDataView buffer = ByteBufferDataView.allocate(1024 * 8);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        SegmentBuilder segment = new SegmentBuilder(buffer, new BuilderArena(BuilderArena.SUGGESTED_FIRST_SEGMENT_WORDS, BuilderArena.SUGGESTED_ALLOCATION_STRATEGY));
        BareStructBuilder factory = new BareStructBuilder(new StructSize((short) 2, (short) 4));
        StructBuilder builder = WireHelpers.initStructPointer(factory, 0, segment, factory.structSize);
        setupStruct(builder);
        checkStruct(builder);
    }

}
